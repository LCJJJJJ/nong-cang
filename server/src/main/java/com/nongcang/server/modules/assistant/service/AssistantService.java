package com.nongcang.server.modules.assistant.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.config.AssistantProperties;
import com.nongcang.server.modules.assistant.domain.dto.AssistantChatRequest;
import com.nongcang.server.modules.assistant.domain.dto.AssistantActionExecuteRequest;
import com.nongcang.server.modules.assistant.domain.entity.AssistantActionPlanEntity;
import com.nongcang.server.modules.assistant.domain.entity.AssistantMessageEntity;
import com.nongcang.server.modules.assistant.domain.entity.AssistantSessionEntity;
import com.nongcang.server.modules.assistant.domain.entity.AssistantToolAuditEntity;
import com.nongcang.server.modules.assistant.domain.vo.AssistantActionExecuteResponse;
import com.nongcang.server.modules.assistant.domain.vo.AssistantChatResponse;
import com.nongcang.server.modules.assistant.domain.vo.AssistantColumnResponse;
import com.nongcang.server.modules.assistant.domain.vo.AssistantMessageResponse;
import com.nongcang.server.modules.assistant.domain.vo.AssistantSessionListItemResponse;
import com.nongcang.server.modules.assistant.repository.AssistantMessageRepository;
import com.nongcang.server.modules.assistant.repository.AssistantActionPlanRepository;
import com.nongcang.server.modules.assistant.repository.AssistantSessionRepository;
import com.nongcang.server.modules.assistant.repository.AssistantToolAuditRepository;
import com.nongcang.server.modules.auth.domain.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AssistantService {

	private static final int MAX_TOOL_ROUNDS = 4;

	private final AssistantSessionRepository assistantSessionRepository;
	private final AssistantMessageRepository assistantMessageRepository;
	private final AssistantToolAuditRepository assistantToolAuditRepository;
	private final AssistantLlmClient assistantLlmClient;
	private final AssistantToolRegistry assistantToolRegistry;
	private final AssistantActionPlanRepository assistantActionPlanRepository;
	private final AssistantBasicInfoActionService assistantBasicInfoActionService;
	private final AssistantProperties assistantProperties;
	private final ObjectMapper objectMapper;

	public AssistantService(
			AssistantSessionRepository assistantSessionRepository,
			AssistantMessageRepository assistantMessageRepository,
			AssistantToolAuditRepository assistantToolAuditRepository,
			AssistantLlmClient assistantLlmClient,
			AssistantToolRegistry assistantToolRegistry,
			AssistantActionPlanRepository assistantActionPlanRepository,
			AssistantBasicInfoActionService assistantBasicInfoActionService,
			AssistantProperties assistantProperties,
			ObjectMapper objectMapper) {
		this.assistantSessionRepository = assistantSessionRepository;
		this.assistantMessageRepository = assistantMessageRepository;
		this.assistantToolAuditRepository = assistantToolAuditRepository;
		this.assistantLlmClient = assistantLlmClient;
		this.assistantToolRegistry = assistantToolRegistry;
		this.assistantActionPlanRepository = assistantActionPlanRepository;
		this.assistantBasicInfoActionService = assistantBasicInfoActionService;
		this.assistantProperties = assistantProperties;
		this.objectMapper = objectMapper;
	}

	public List<AssistantSessionListItemResponse> getSessions(Authentication authentication) {
		AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
		return assistantSessionRepository.findRecentByUserId(authenticatedUser.userId(), 20)
				.stream()
				.map(this::toSessionListItemResponse)
				.toList();
	}

	public List<AssistantMessageResponse> getMessages(Long sessionId, Authentication authentication) {
		AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
		AssistantSessionEntity session = getExistingSession(sessionId, authenticatedUser.userId());
		return assistantMessageRepository.findAllBySessionId(session.id())
				.stream()
				.map(this::toMessageResponse)
				.toList();
	}

	@Transactional
	public AssistantChatResponse chat(AssistantChatRequest request, Authentication authentication) {
		AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
		AssistantSessionEntity session = resolveSession(request, authenticatedUser);

		long userMessageId = assistantMessageRepository.insert(new AssistantMessageEntity(
				null,
				session.id(),
				"user",
				request.message().trim(),
				"TEXT",
				null,
				null));

		List<Map<String, Object>> llmMessages = buildLlmMessages(session, authenticatedUser, request);
		List<AssistantToolExecutionResult> toolResults = new ArrayList<>();
		String assistantContent = "";

		for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
			AssistantLlmResponse llmResponse = assistantLlmClient.chat(
					llmMessages,
					assistantToolRegistry.getToolDefinitions());

			if (llmResponse.toolCalls() == null || llmResponse.toolCalls().isEmpty()) {
				assistantContent = StringUtils.hasText(llmResponse.content())
						? llmResponse.content().trim()
						: defaultAssistantContent(toolResults);
				break;
			}

				llmMessages.add(buildAssistantToolCallMessage(llmResponse));
			for (AssistantToolCall toolCall : llmResponse.toolCalls()) {
				AssistantToolExecutionResult toolResult = assistantToolRegistry.execute(
						toolCall,
						assistantProperties.getToolMaxRows(),
						session.id(),
						authenticatedUser.userId());
				toolResults.add(toolResult);
				assistantToolAuditRepository.insert(new AssistantToolAuditEntity(
						null,
						session.id(),
						userMessageId,
						toolCall.name(),
						toolCall.argumentsJson(),
						serialize(toolResult),
						1,
						null));
				llmMessages.add(Map.of(
						"role", "tool",
						"tool_call_id", toolCall.id(),
						"content", toToolContent(toolResult)));
			}
		}

		if (!StringUtils.hasText(assistantContent)) {
			assistantContent = defaultAssistantContent(toolResults);
		}

		String metadataJson = toolResults.isEmpty()
				? null
				: serialize(new AssistantMessageMetadata(
						resolveUserFacingResultBlocks(toolResults),
						resolveUserFacingActionCard(toolResults)));

		long assistantMessageId = assistantMessageRepository.insert(new AssistantMessageEntity(
				null,
				session.id(),
				"assistant",
				assistantContent,
				toolResults.isEmpty() ? "TEXT" : "RESULT",
				metadataJson,
				null));

		assistantSessionRepository.updateContext(session.id(), request.routePath(), request.routeTitle());

		AssistantMessageEntity userMessage = assistantMessageRepository.findAllBySessionId(session.id()).stream()
				.filter(message -> message.id().equals(userMessageId))
				.findFirst()
				.orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR));
		AssistantMessageEntity assistantMessage = assistantMessageRepository.findAllBySessionId(session.id()).stream()
				.filter(message -> message.id().equals(assistantMessageId))
				.findFirst()
				.orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR));

		AssistantSessionEntity latestSession = getExistingSession(session.id(), authenticatedUser.userId());
		return new AssistantChatResponse(
				toSessionListItemResponse(latestSession),
				toMessageResponse(userMessage),
				toMessageResponse(assistantMessage));
	}

	public void streamChat(
			AssistantChatRequest request,
			Authentication authentication,
			AssistantStreamListener streamListener) {
		AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
		AssistantSessionEntity session = resolveSession(request, authenticatedUser);
		AssistantSessionListItemResponse sessionResponse = toSessionListItemResponse(session);
		streamListener.onSession(sessionResponse);

		long userMessageId = assistantMessageRepository.insert(new AssistantMessageEntity(
				null,
				session.id(),
				"user",
				request.message().trim(),
				"TEXT",
				null,
				null));

		List<Map<String, Object>> llmMessages = buildLlmMessages(session, authenticatedUser, request);
		List<AssistantToolExecutionResult> toolResults = new ArrayList<>();
		StringBuilder assistantContentBuilder = new StringBuilder();

		try {
			boolean hasExecutedTool = false;
			boolean shouldUseRealStreaming = false;
			String fallbackDirectContent = null;

			for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
				streamListener.onStatus(hasExecutedTool ? "正在继续查询系统数据..." : "正在分析你的问题...");
				AssistantLlmResponse planningResponse = assistantLlmClient.chat(
						llmMessages,
						assistantToolRegistry.getToolDefinitions());

				if (planningResponse.toolCalls() == null || planningResponse.toolCalls().isEmpty()) {
					if (hasExecutedTool) {
						shouldUseRealStreaming = true;
					} else {
						fallbackDirectContent = StringUtils.hasText(planningResponse.content())
								? planningResponse.content().trim()
								: defaultAssistantContent(toolResults);
					}
					break;
				}

				hasExecutedTool = true;
				streamListener.onStatus("正在查询系统数据...");
				llmMessages.add(buildAssistantToolCallMessage(planningResponse));

				for (AssistantToolCall toolCall : planningResponse.toolCalls()) {
					AssistantToolExecutionResult toolResult = assistantToolRegistry.execute(
							toolCall,
							assistantProperties.getToolMaxRows(),
							session.id(),
							authenticatedUser.userId());
					toolResults.add(toolResult);
					assistantToolAuditRepository.insert(new AssistantToolAuditEntity(
							null,
							session.id(),
							userMessageId,
							toolCall.name(),
							toolCall.argumentsJson(),
							serialize(toolResult),
							1,
							null));
					llmMessages.add(Map.of(
							"role", "tool",
							"tool_call_id", toolCall.id(),
							"content", toToolContent(toolResult)));
				}
			}

			if (shouldUseRealStreaming) {
				streamListener.onStatus("正在生成回答...");
				assistantLlmClient.streamChat(llmMessages, List.of(), delta -> {
					assistantContentBuilder.append(delta);
					streamListener.onDelta(delta);
				});
			} else {
				emitSimulatedStream(
						StringUtils.hasText(fallbackDirectContent) ? fallbackDirectContent : defaultAssistantContent(toolResults),
						streamListener,
						assistantContentBuilder);
			}

		String metadataJson = toolResults.isEmpty()
					? null
					: serialize(new AssistantMessageMetadata(
							resolveUserFacingResultBlocks(toolResults),
							resolveUserFacingActionCard(toolResults)));

			long assistantMessageId = assistantMessageRepository.insert(new AssistantMessageEntity(
					null,
					session.id(),
					"assistant",
					assistantContentBuilder.toString(),
					toolResults.isEmpty() ? "TEXT" : "RESULT",
					metadataJson,
					null));

			assistantSessionRepository.updateContext(session.id(), request.routePath(), request.routeTitle());

			AssistantMessageEntity userMessage = assistantMessageRepository.findAllBySessionId(session.id()).stream()
					.filter(message -> message.id().equals(userMessageId))
					.findFirst()
					.orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR));
			AssistantMessageEntity assistantMessage = assistantMessageRepository.findAllBySessionId(session.id()).stream()
					.filter(message -> message.id().equals(assistantMessageId))
					.findFirst()
					.orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR));

			AssistantSessionEntity latestSession = getExistingSession(session.id(), authenticatedUser.userId());
			streamListener.onDone(new AssistantChatResponse(
					toSessionListItemResponse(latestSession),
					toMessageResponse(userMessage),
					toMessageResponse(assistantMessage)));
		} catch (Exception exception) {
			streamListener.onError(exception);
			throw exception;
		}
	}

	private AssistantSessionEntity resolveSession(AssistantChatRequest request, AuthenticatedUser authenticatedUser) {
		if (request.sessionId() != null) {
			return getExistingSession(request.sessionId(), authenticatedUser.userId());
		}

		String title = buildSessionTitle(request.message());
		long sessionId = assistantSessionRepository.insert(new AssistantSessionEntity(
				null,
				buildSessionCode(),
				authenticatedUser.userId(),
				title,
				request.routePath(),
				request.routeTitle(),
				1,
				null,
				null,
				null));
		return getExistingSession(sessionId, authenticatedUser.userId());
	}

	private AssistantSessionEntity getExistingSession(Long sessionId, Long userId) {
		return assistantSessionRepository.findByIdAndUserId(sessionId, userId)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.ASSISTANT_SESSION_NOT_FOUND));
	}

	private List<Map<String, Object>> buildLlmMessages(
			AssistantSessionEntity session,
			AuthenticatedUser authenticatedUser,
			AssistantChatRequest request) {
		List<Map<String, Object>> messages = new ArrayList<>();
		String pendingActionContext = assistantActionPlanRepository.findLatestOpenPlan(session.id(), authenticatedUser.userId())
				.map(this::buildPendingActionContext)
				.orElse("");
		messages.add(Map.of(
				"role", "system",
				"content", buildSystemPrompt(authenticatedUser, request.routePath(), request.routeTitle(), pendingActionContext)));
		assistantMessageRepository.findRecentBySessionId(session.id(), assistantProperties.getChatMaxHistory())
				.forEach(message -> messages.add(Map.of(
						"role", message.role(),
						"content", message.content())));
		return messages;
	}

	private String buildSystemPrompt(
			AuthenticatedUser authenticatedUser,
			String routePath,
			String routeTitle,
			String pendingActionContext) {
		String currentRoutePath = StringUtils.hasText(routePath) ? routePath : "未知页面";
		String currentRouteTitle = StringUtils.hasText(routeTitle) ? routeTitle : "未命名页面";
		return """
				你是农产品仓储管理系统的智能助手，必须使用中文回答。
				你擅长解释系统数据、查询业务记录、说明库存变化原因，并且优先使用工具来获取真实系统数据。
				你还支持农产品基础信息管理模块的新增、修改和删除，包括：产品分类、产品档案、产品单位、产地信息、储存条件、品质等级。
				当用户表达“新增、创建、添加、修改、更新、删除、移除、停用、启用”等写操作意图时，必须先调用 prepare_basic_info_write_action。
				不要先调用查询工具来猜测写操作参数；写操作里涉及的分类、单位、产地、储存条件、品质等级，都可以直接把名称传给 prepare_basic_info_write_action，由后端负责解析成系统ID。
				缺字段就继续追问；计划 READY 后，再让用户确认执行。删除操作必须要求用户明确确认删除。
				禁止编造不存在的记录；当用户询问系统数据时，必须优先调用合适的工具。
				如果结果为空，要明确告知未找到匹配数据；如果用户描述不清晰，可以简短追问。
				仅当用户明确要求“刷新预警”时，才调用 refresh_alerts 工具。
				当前登录用户：%s（角色：%s）
				当前页面：%s（%s）
				%s
				请在回答里先给出结论，再补充关键记录摘要，保持简洁。
				"""
				.formatted(
						authenticatedUser.displayName(),
						String.join("、", authenticatedUser.roles()),
						currentRouteTitle,
						currentRoutePath,
						pendingActionContext);
	}

	private Map<String, Object> buildAssistantToolCallMessage(AssistantLlmResponse llmResponse) {
		List<Map<String, Object>> toolCalls = llmResponse.toolCalls().stream()
				.map(toolCall -> Map.<String, Object>of(
						"id", toolCall.id(),
						"type", "function",
						"function", Map.of(
								"name", toolCall.name(),
								"arguments", toolCall.argumentsJson())))
				.toList();

		Map<String, Object> message = new LinkedHashMap<>();
		message.put("role", "assistant");
		message.put("content", llmResponse.content());
		message.put("tool_calls", toolCalls);
		return message;
	}

	private String toToolContent(AssistantToolExecutionResult toolResult) {
		return serialize(toolResult);
	}

	private String defaultAssistantContent(List<AssistantToolExecutionResult> toolResults) {
		if (toolResults.isEmpty()) {
			return "我还没有查到足够的系统数据，请换一种说法试试。";
		}
		return toolResults.get(toolResults.size() - 1).summary();
	}

	private void emitSimulatedStream(
			String content,
			AssistantStreamListener streamListener,
			StringBuilder assistantContentBuilder) {
		if (!StringUtils.hasText(content)) {
			return;
		}

		for (String chunk : splitIntoChunks(content)) {
			assistantContentBuilder.append(chunk);
			streamListener.onDelta(chunk);
		}
	}

	private AssistantSessionListItemResponse toSessionListItemResponse(AssistantSessionEntity entity) {
		return new AssistantSessionListItemResponse(
				entity.id(),
				entity.sessionCode(),
				entity.title(),
				entity.routePath(),
				entity.routeTitle(),
				entity.lastMessagePreview(),
				toIsoDateTime(entity.updatedAt()));
	}

	private AssistantMessageResponse toMessageResponse(AssistantMessageEntity entity) {
		return new AssistantMessageResponse(
				entity.id(),
				entity.role(),
				entity.content(),
				entity.messageType(),
				parseResultBlocks(entity.metadataJson()),
				parseActionCard(entity.metadataJson()),
				toIsoDateTime(entity.createdAt()));
	}

	private List<AssistantMessageResponse.AssistantResultBlockResponse> parseResultBlocks(String metadataJson) {
		if (!StringUtils.hasText(metadataJson)) {
			return null;
		}

		try {
			AssistantMessageMetadata metadata = objectMapper.readValue(metadataJson, AssistantMessageMetadata.class);
			if (metadata.resultBlocks() == null) {
				return null;
			}
			return metadata.resultBlocks()
					.stream()
					.map(block -> new AssistantMessageResponse.AssistantResultBlockResponse(
							block.title(),
							block.summary(),
							block.routePath(),
							block.routeLabel(),
							block.columns().stream()
									.map(column -> new AssistantColumnResponse(column.key(), column.label()))
									.toList(),
							block.rows()))
					.toList();
		} catch (Exception exception) {
			return null;
		}
	}

	private AssistantMessageResponse.AssistantActionCardResponse parseActionCard(String metadataJson) {
		if (!StringUtils.hasText(metadataJson)) {
			return null;
		}

		try {
			AssistantMessageMetadata metadata = objectMapper.readValue(metadataJson, AssistantMessageMetadata.class);
			if (metadata.actionCard() == null) {
				return null;
			}

			AssistantActionCard actionCard = metadata.actionCard();
			return new AssistantMessageResponse.AssistantActionCardResponse(
					actionCard.actionCode(),
					actionCard.status(),
					actionCard.resourceType(),
					actionCard.resourceLabel(),
					actionCard.actionType(),
					actionCard.actionLabel(),
					actionCard.targetLabel(),
					actionCard.summary(),
					actionCard.riskLevel(),
					actionCard.confirmationMode(),
					actionCard.confirmationTextHint(),
					actionCard.missingFields().stream()
							.map(field -> new AssistantMessageResponse.AssistantActionFieldPromptResponse(
									field.field(),
									field.label(),
									field.hint()))
							.toList(),
					actionCard.previewFields().stream()
							.map(field -> new AssistantMessageResponse.AssistantActionFieldValueResponse(
									field.field(),
									field.label(),
									field.value()))
							.toList());
		} catch (Exception exception) {
			return null;
		}
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
			throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
		}
		return authenticatedUser;
	}

	private String buildSessionCode() {
		return "AS-" + System.currentTimeMillis();
	}

	private String buildSessionTitle(String message) {
		String trimmed = message.trim();
		return trimmed.length() <= 24 ? trimmed : trimmed.substring(0, 24) + "...";
	}

	private String toIsoDateTime(LocalDateTime value) {
		return value == null ? null : value.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String serialize(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception exception) {
			return "{}";
		}
	}

	private List<String> splitIntoChunks(String content) {
		List<String> chunks = new ArrayList<>();
		int chunkSize = 18;
		for (int index = 0; index < content.length(); index += chunkSize) {
			chunks.add(content.substring(index, Math.min(content.length(), index + chunkSize)));
		}
		return chunks;
	}

	private List<AssistantResultBlock> resolveUserFacingResultBlocks(
			List<AssistantToolExecutionResult> toolResults) {
		if (toolResults.isEmpty()) {
			return List.of();
		}

		for (int index = toolResults.size() - 1; index >= 0; index--) {
			List<AssistantResultBlock> resultBlocks = toolResults.get(index).resultBlocks();
			if (resultBlocks != null && !resultBlocks.isEmpty()) {
				return resultBlocks;
			}
		}

		return List.of();
	}

	private AssistantActionCard resolveUserFacingActionCard(List<AssistantToolExecutionResult> toolResults) {
		for (int index = toolResults.size() - 1; index >= 0; index--) {
			AssistantActionCard actionCard = toolResults.get(index).actionCard();
			if (actionCard != null) {
				return actionCard;
			}
		}
		return null;
	}

	@Transactional
	public AssistantActionExecuteResponse executeActionPlan(
			String actionCode,
			AssistantActionExecuteRequest request,
			Authentication authentication) {
		AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
		AssistantActionExecutionResult executionResult = assistantBasicInfoActionService.executeAction(
				actionCode,
				authenticatedUser.userId(),
				request == null ? null : request.confirmationText());

		AssistantActionPlanEntity plan = assistantActionPlanRepository.findByActionCodeAndUserId(actionCode, authenticatedUser.userId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.ASSISTANT_ACTION_PLAN_NOT_FOUND));
		String metadataJson = serialize(new AssistantMessageMetadata(List.of(), executionResult.actionCard()));
		long assistantMessageId = assistantMessageRepository.insert(new AssistantMessageEntity(
				null,
				plan.sessionId(),
				"assistant",
				executionResult.message(),
				"ACTION",
				metadataJson,
				null));

		AssistantMessageEntity assistantMessage = assistantMessageRepository.findAllBySessionId(plan.sessionId()).stream()
				.filter(message -> message.id().equals(assistantMessageId))
				.findFirst()
				.orElseThrow(() -> new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR));
		AssistantSessionEntity session = getExistingSession(plan.sessionId(), authenticatedUser.userId());
		return new AssistantActionExecuteResponse(
				toSessionListItemResponse(session),
				toMessageResponse(assistantMessage));
	}

	private String buildPendingActionContext(AssistantActionPlanEntity plan) {
		return """
				当前存在一个未完成的写操作计划：
				actionCode=%s
				resourceType=%s
				actionType=%s
				status=%s
				summary=%s
				如果用户继续补充字段，请优先继续这个计划；如果用户明确说确认执行或确认删除，也应优先处理这个计划。
				"""
				.formatted(
						plan.actionCode(),
						plan.resourceType(),
						plan.actionType(),
						plan.status(),
						plan.summary());
	}
}
