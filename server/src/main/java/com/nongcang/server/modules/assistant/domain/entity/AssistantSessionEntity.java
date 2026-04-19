package com.nongcang.server.modules.assistant.domain.entity;

import java.time.LocalDateTime;

public record AssistantSessionEntity(
		Long id,
		String sessionCode,
		Long userId,
		String title,
		String routePath,
		String routeTitle,
		Integer status,
		String lastMessagePreview,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
