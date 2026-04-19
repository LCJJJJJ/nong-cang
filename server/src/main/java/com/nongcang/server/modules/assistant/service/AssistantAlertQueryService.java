package com.nongcang.server.modules.assistant.service;

import java.util.List;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.alertrecord.domain.dto.AlertRecordListQueryRequest;
import com.nongcang.server.modules.alertrecord.service.AlertRecordService;
import com.nongcang.server.modules.alertrule.domain.dto.AlertRuleListQueryRequest;
import com.nongcang.server.modules.alertrule.service.AlertRuleService;
import com.nongcang.server.modules.messagenotice.domain.dto.MessageNoticeListQueryRequest;
import com.nongcang.server.modules.messagenotice.service.MessageNoticeService;
import org.springframework.stereotype.Service;

@Service
public class AssistantAlertQueryService {

	private final AlertRuleService alertRuleService;
	private final AlertRecordService alertRecordService;
	private final MessageNoticeService messageNoticeService;
	private final AssistantQuerySupport assistantQuerySupport;

	public AssistantAlertQueryService(
			AlertRuleService alertRuleService,
			AlertRecordService alertRecordService,
			MessageNoticeService messageNoticeService,
			AssistantQuerySupport assistantQuerySupport) {
		this.alertRuleService = alertRuleService;
		this.alertRecordService = alertRecordService;
		this.messageNoticeService = messageNoticeService;
		this.assistantQuerySupport = assistantQuerySupport;
	}

	public AssistantToolExecutionResult execute(AssistantToolArguments arguments, int maxRows) {
		int limit = arguments.resolveLimit(10, maxRows);
		return switch (normalizeEntityType(arguments.entityType())) {
			case "alert_rule" -> createResult(
					"预警规则查询",
					"/alert-rules",
					"预警规则管理",
					assistantQuerySupport.filterItems(
							alertRuleService.getAlertRuleList(new AlertRuleListQueryRequest(null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("ruleCode", "ruleName", "alertType", "severity", "thresholdValue", "enabledLabel"),
					limit);
			case "alert_record" -> createResult(
					"预警记录查询",
					"/alerts",
					"预警中心",
					assistantQuerySupport.filterItems(
							alertRecordService.getAlertRecordList(new AlertRecordListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("alertCode", "alertType", "severity", "sourceCode", "title", "statusLabel", "occurredAt"),
					limit);
			case "message_notice" -> createResult(
					"消息通知查询",
					"/message-notices",
					"消息中心",
					assistantQuerySupport.filterItems(
							messageNoticeService.getMessageNoticeList(new MessageNoticeListQueryRequest(null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("noticeCode", "severity", "title", "content", "statusLabel", "createdAt"),
					limit);
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "预警消息查询实体不正确");
		};
	}

	public AssistantToolExecutionResult refreshAlerts() {
		var refreshResponse = alertRecordService.refreshAlertRecords();
		AssistantResultBlock block = new AssistantResultBlock(
				"预警刷新结果",
				"预警刷新已完成",
				"/alerts",
				"预警中心",
				List.of(
						new AssistantColumn("createdCount", "新增数量"),
						new AssistantColumn("resolvedCount", "恢复数量"),
						new AssistantColumn("ignoredCount", "忽略数量")),
				List.of(java.util.Map.of(
						"createdCount", String.valueOf(refreshResponse.createdCount()),
						"resolvedCount", String.valueOf(refreshResponse.resolvedCount()),
						"ignoredCount", String.valueOf(refreshResponse.ignoredCount()))));
		return new AssistantToolExecutionResult("refresh_alerts", "预警刷新已完成", List.of(block));
	}

	private AssistantToolExecutionResult createResult(
			String title,
			String routePath,
			String routeLabel,
			List<?> items,
			List<String> preferredKeys,
			int limit) {
		AssistantResultBlock block = assistantQuerySupport.toTableBlock(
				title,
				routePath,
				routeLabel,
				items,
				preferredKeys,
				limit,
				title + "：");
		return new AssistantToolExecutionResult("query_alert_message_data", block.summary(), List.of(block));
	}

	private String normalizeEntityType(String entityType) {
		return entityType == null ? "" : entityType.trim().toLowerCase();
	}
}
