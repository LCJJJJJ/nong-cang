package com.nongcang.server.modules.assistant.service;

import java.util.List;

public record AssistantActionCard(
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
		List<AssistantActionFieldPrompt> missingFields,
		List<AssistantActionFieldValue> previewFields) {
}
