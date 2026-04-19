package com.nongcang.server.modules.alertrecord.domain.vo;

public record AlertRecordListItemResponse(
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
		String statusLabel,
		String occurredAt,
		String resolvedAt,
		String createdAt,
		String updatedAt) {
}
