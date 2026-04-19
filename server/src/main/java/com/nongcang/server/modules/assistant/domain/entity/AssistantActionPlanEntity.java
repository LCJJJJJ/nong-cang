package com.nongcang.server.modules.assistant.domain.entity;

import java.time.LocalDateTime;

public record AssistantActionPlanEntity(
		Long id,
		String actionCode,
		Long sessionId,
		Long userId,
		String resourceType,
		String actionType,
		Long targetId,
		String targetLabel,
		String fieldsJson,
		String missingFieldsJson,
		String riskLevel,
		String confirmationMode,
		String status,
		String summary,
		String errorCode,
		String errorMessage,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime executedAt) {
}
