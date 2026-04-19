package com.nongcang.server.modules.alertrule.domain.vo;

public record AlertRuleListItemResponse(
		Long id,
		String ruleCode,
		String ruleName,
		String alertType,
		String severity,
		Double thresholdValue,
		String thresholdUnit,
		Integer enabled,
		String enabledLabel,
		String description,
		Integer sortOrder,
		String createdAt,
		String updatedAt) {
}
