package com.nongcang.server.modules.assistant.domain.vo;

import java.util.List;
import java.util.Map;

public record AssistantMessageResponse(
		Long id,
		String role,
		String content,
		String messageType,
		List<AssistantResultBlockResponse> resultBlocks,
		AssistantActionCardResponse actionCard,
		String createdAt) {

	public record AssistantResultBlockResponse(
			String title,
			String summary,
			String routePath,
			String routeLabel,
			List<AssistantColumnResponse> columns,
			List<Map<String, String>> rows) {
	}

	public record AssistantActionCardResponse(
			String actionCode,
			String status,
			String resourceType,
			String resourceLabel,
			String actionType,
			String actionLabel,
			String targetLabel,
			String summary,
			String riskLevel,
			String confirmationMode,
			String confirmationTextHint,
			List<AssistantActionFieldPromptResponse> missingFields,
			List<AssistantActionFieldValueResponse> previewFields) {
	}

	public record AssistantActionFieldPromptResponse(
			String field,
			String label,
			String hint) {
	}

	public record AssistantActionFieldValueResponse(
			String field,
			String label,
			String value) {
	}
}
