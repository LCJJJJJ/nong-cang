package com.nongcang.server.modules.assistant.domain.vo;

public record AssistantSessionListItemResponse(
		Long id,
		String sessionCode,
		String title,
		String routePath,
		String routeTitle,
		String lastMessagePreview,
		String updatedAt) {
}
