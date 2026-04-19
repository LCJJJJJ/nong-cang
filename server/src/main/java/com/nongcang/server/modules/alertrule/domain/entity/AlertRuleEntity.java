package com.nongcang.server.modules.alertrule.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertRuleEntity(
		Long id,
		String ruleCode,
		String ruleName,
		String alertType,
		String severity,
		BigDecimal thresholdValue,
		String thresholdUnit,
		Integer enabled,
		String description,
		Integer sortOrder,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
