package com.nongcang.server.modules.assistant.service;

public record AssistantToolCall(
		String id,
		String name,
		String argumentsJson) {
}
