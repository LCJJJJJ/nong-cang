package com.nongcang.server.modules.assistant.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import org.springframework.stereotype.Component;

@Component
public class AssistantToolRegistry {

	private final ObjectMapper objectMapper;
	private final AssistantBasicQueryService assistantBasicQueryService;
	private final AssistantWarehouseQueryService assistantWarehouseQueryService;
	private final AssistantInboundQueryService assistantInboundQueryService;
	private final AssistantOutboundQueryService assistantOutboundQueryService;
	private final AssistantInventoryQueryService assistantInventoryQueryService;
	private final AssistantQualityQueryService assistantQualityQueryService;
	private final AssistantAlertQueryService assistantAlertQueryService;
	private final AssistantBasicInfoWriteToolService assistantBasicInfoWriteToolService;

	public AssistantToolRegistry(
			ObjectMapper objectMapper,
			AssistantBasicQueryService assistantBasicQueryService,
			AssistantWarehouseQueryService assistantWarehouseQueryService,
			AssistantInboundQueryService assistantInboundQueryService,
			AssistantOutboundQueryService assistantOutboundQueryService,
			AssistantInventoryQueryService assistantInventoryQueryService,
			AssistantQualityQueryService assistantQualityQueryService,
			AssistantAlertQueryService assistantAlertQueryService,
			AssistantBasicInfoWriteToolService assistantBasicInfoWriteToolService) {
		this.objectMapper = objectMapper;
		this.assistantBasicQueryService = assistantBasicQueryService;
		this.assistantWarehouseQueryService = assistantWarehouseQueryService;
		this.assistantInboundQueryService = assistantInboundQueryService;
		this.assistantOutboundQueryService = assistantOutboundQueryService;
		this.assistantInventoryQueryService = assistantInventoryQueryService;
		this.assistantQualityQueryService = assistantQualityQueryService;
		this.assistantAlertQueryService = assistantAlertQueryService;
		this.assistantBasicInfoWriteToolService = assistantBasicInfoWriteToolService;
	}

	public List<Map<String, Object>> getToolDefinitions() {
		return List.of(
				buildQueryTool(
						"query_basic_master_data",
						"查询基础主数据，可查询 entityType 为 product、category、unit、origin、storage_condition、quality_grade、supplier、customer。"),
				buildQueryTool(
						"query_warehouse_data",
						"查询仓储结构，可查询 entityType 为 warehouse、zone、location、structure。structure 用于某仓库的库区与库位结构。"),
				buildQueryTool(
						"query_inbound_data",
						"查询入库业务，可查询 entityType 为 inbound_order、putaway_task、inbound_record。"),
				buildQueryTool(
						"query_outbound_data",
						"查询出库业务，可查询 entityType 为 outbound_order、outbound_task、outbound_record。"),
				buildQueryTool(
						"query_inventory_data",
						"查询库存业务，可查询 entityType 为 stock、transaction、adjustment、stocktaking。"),
				buildQueryTool(
						"query_quality_loss_data",
						"查询质检与损耗业务，可查询 entityType 为 inspection、abnormal_stock、loss_record。"),
				buildQueryTool(
						"query_alert_message_data",
						"查询预警与消息业务，可查询 entityType 为 alert_rule、alert_record、message_notice。"),
				buildWritePrepareTool(),
				buildWriteExecuteTool(),
				Map.of(
						"type", "function",
						"function", Map.of(
								"name", "refresh_alerts",
								"description", "当用户明确要求刷新预警时调用，重新生成系统预警记录。",
								"parameters", Map.of(
										"type", "object",
										"properties", Map.of(),
										"additionalProperties", false))));
	}

	public AssistantToolExecutionResult execute(AssistantToolCall toolCall, int maxRows, Long sessionId, Long userId) {
		return switch (toolCall.name()) {
			case "query_basic_master_data" -> assistantBasicQueryService.execute(parseArguments(toolCall.argumentsJson()), maxRows);
			case "query_warehouse_data" -> assistantWarehouseQueryService.execute(parseArguments(toolCall.argumentsJson()), maxRows);
			case "query_inbound_data" -> assistantInboundQueryService.execute(parseArguments(toolCall.argumentsJson()), maxRows);
			case "query_outbound_data" -> assistantOutboundQueryService.execute(parseArguments(toolCall.argumentsJson()), maxRows);
			case "query_inventory_data" -> assistantInventoryQueryService.execute(parseArguments(toolCall.argumentsJson()), maxRows);
			case "query_quality_loss_data" -> assistantQualityQueryService.execute(parseArguments(toolCall.argumentsJson()), maxRows);
			case "query_alert_message_data" -> assistantAlertQueryService.execute(parseArguments(toolCall.argumentsJson()), maxRows);
			case "prepare_basic_info_write_action" -> assistantBasicInfoWriteToolService.prepare(sessionId, userId, toolCall.argumentsJson());
			case "execute_basic_info_write_action" -> assistantBasicInfoWriteToolService.execute(userId, toolCall.argumentsJson());
			case "refresh_alerts" -> assistantAlertQueryService.refreshAlerts();
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "智能助手工具不存在");
		};
	}

	private Map<String, Object> buildQueryTool(String name, String description) {
		return Map.of(
				"type", "function",
				"function", Map.of(
						"name", name,
						"description", description,
						"parameters", Map.of(
								"type", "object",
								"properties", Map.of(
										"entityType", Map.of(
												"type", "string",
												"description", "本次查询的实体类型"),
										"keyword", Map.of(
												"type", "string",
												"description", "主关键词，可为空"),
										"relatedKeyword", Map.of(
												"type", "string",
												"description", "补充筛选关键词，可为空"),
										"limit", Map.of(
												"type", "integer",
												"description", "返回条数，建议不超过10")),
								"required", List.of("entityType"),
								"additionalProperties", false)));
	}

	private AssistantToolArguments parseArguments(String argumentsJson) {
		try {
			return objectMapper.readValue(argumentsJson, AssistantToolArguments.class);
		} catch (Exception exception) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "智能助手工具参数解析失败");
		}
	}

	private Map<String, Object> buildWritePrepareTool() {
		return Map.of(
				"type", "function",
				"function", Map.of(
						"name", "prepare_basic_info_write_action",
						"description",
						"为农产品基础信息管理模块准备写操作计划。只要用户要新增、修改或删除，都必须优先调用此工具。resourceType 可选 category、product_archive、product_unit、product_origin、storage_condition、quality_grade；actionType 可选 CREATE、UPDATE、DELETE。fields 可以直接填写中文字段含义和值，引用字段允许直接传名称，例如分类名称、单位名称、产地名称、储存条件名称、品质等级名称，后端会自动解析。",
						"parameters", Map.of(
								"type", "object",
								"properties", Map.of(
										"actionCode", Map.of("type", "string"),
										"resourceType", Map.of("type", "string"),
										"actionType", Map.of("type", "string"),
										"target", Map.of("type", "string"),
										"fields", Map.of("type", "object")),
								"additionalProperties", false)));
	}

	private Map<String, Object> buildWriteExecuteTool() {
		return Map.of(
				"type", "function",
				"function", Map.of(
						"name", "execute_basic_info_write_action",
						"description", "在用户已经明确确认后执行农产品基础信息模块的写操作。",
						"parameters", Map.of(
								"type", "object",
								"properties", Map.of(
										"actionCode", Map.of("type", "string"),
										"confirmationText", Map.of("type", "string")),
								"required", List.of("actionCode"),
								"additionalProperties", false)));
	}
}
