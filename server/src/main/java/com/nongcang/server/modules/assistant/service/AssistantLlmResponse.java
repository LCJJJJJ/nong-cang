package com.nongcang.server.modules.assistant.service;

import java.util.List;

public record AssistantLlmResponse(
		String content,
		List<AssistantToolCall> toolCalls) {
}
