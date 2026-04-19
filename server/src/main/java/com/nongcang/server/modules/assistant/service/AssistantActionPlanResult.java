package com.nongcang.server.modules.assistant.service;

import java.util.List;

public record AssistantActionPlanResult(
		String actionCode,
		String status,
		String summary,
		AssistantActionCard actionCard,
		List<AssistantResultBlock> resultBlocks) {
}
