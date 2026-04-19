package com.nongcang.server.modules.assistant.domain.entity;

import java.time.LocalDateTime;

public record AssistantToolAuditEntity(
		Long id,
		Long sessionId,
		Long messageId,
		String toolName,
		String toolArgumentsJson,
		String toolResultJson,
		Integer success,
		LocalDateTime createdAt) {
}
