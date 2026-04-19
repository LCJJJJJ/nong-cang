package com.nongcang.server.modules.assistant.service;

import java.util.List;

public record AssistantToolExecutionResult(
		String toolName,
		String summary,
		List<AssistantResultBlock> resultBlocks,
		AssistantActionCard actionCard) {

	public AssistantToolExecutionResult(String toolName, String summary, List<AssistantResultBlock> resultBlocks) {
		this(toolName, summary, resultBlocks, null);
	}
}
