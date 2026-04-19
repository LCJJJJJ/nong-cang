package com.nongcang.server.modules.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import org.springframework.stereotype.Service;

@Service
public class AssistantBasicInfoWriteToolService {

	private final ObjectMapper objectMapper;
	private final AssistantBasicInfoActionService assistantBasicInfoActionService;

	public AssistantBasicInfoWriteToolService(
			ObjectMapper objectMapper,
			AssistantBasicInfoActionService assistantBasicInfoActionService) {
		this.objectMapper = objectMapper;
		this.assistantBasicInfoActionService = assistantBasicInfoActionService;
	}

	public AssistantToolExecutionResult prepare(
			Long sessionId,
			Long userId,
			String argumentsJson) {
		AssistantWriteActionToolArguments arguments = parse(argumentsJson);
		AssistantActionPlanResult result = assistantBasicInfoActionService.prepareAction(sessionId, userId, arguments);
		return new AssistantToolExecutionResult(
				"prepare_basic_info_write_action",
				result.summary(),
				result.resultBlocks(),
				result.actionCard());
	}

	public AssistantToolExecutionResult execute(
			Long userId,
			String argumentsJson) {
		AssistantWriteActionExecuteToolArguments arguments = parseExecute(argumentsJson);
		AssistantActionExecutionResult result = assistantBasicInfoActionService.executeAction(
				arguments.actionCode(),
				userId,
				arguments.confirmationText());
		return new AssistantToolExecutionResult(
				"execute_basic_info_write_action",
				result.message(),
				java.util.List.of(),
				result.actionCard());
	}

	private AssistantWriteActionToolArguments parse(String argumentsJson) {
		try {
			return objectMapper.readValue(argumentsJson, AssistantWriteActionToolArguments.class);
		} catch (Exception exception) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "写操作工具参数解析失败");
		}
	}

	private AssistantWriteActionExecuteToolArguments parseExecute(String argumentsJson) {
		try {
			return objectMapper.readValue(argumentsJson, AssistantWriteActionExecuteToolArguments.class);
		} catch (Exception exception) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "执行工具参数解析失败");
		}
	}

	private record AssistantWriteActionExecuteToolArguments(
			String actionCode,
			String confirmationText) {
	}
}
