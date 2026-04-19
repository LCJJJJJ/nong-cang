package com.nongcang.server.modules.assistant.domain.entity;

import java.time.LocalDateTime;

public record AssistantMessageEntity(
		Long id,
		Long sessionId,
		String role,
		String content,
		String messageType,
		String metadataJson,
		LocalDateTime createdAt) {
}
