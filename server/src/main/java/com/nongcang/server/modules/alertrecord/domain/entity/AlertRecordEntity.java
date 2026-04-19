package com.nongcang.server.modules.alertrecord.domain.entity;

import java.time.LocalDateTime;

public record AlertRecordEntity(
		Long id,
		String alertCode,
		Long ruleId,
		String ruleCode,
		String alertType,
		String severity,
		String sourceType,
		Long sourceId,
		String sourceCode,
		String title,
		String content,
		Integer status,
		LocalDateTime occurredAt,
		LocalDateTime resolvedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
