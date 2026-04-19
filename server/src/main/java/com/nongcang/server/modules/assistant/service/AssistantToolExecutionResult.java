package com.nongcang.server.modules.assistant.service;

import java.util.List;

public record AssistantToolExecutionResult(
		String toolName,
		String summary,
		List<AssistantResultBlock> resultBlocks) {
}
