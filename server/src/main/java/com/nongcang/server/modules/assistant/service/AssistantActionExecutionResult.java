package com.nongcang.server.modules.assistant.service;

public record AssistantActionExecutionResult(
		String status,
		String message,
		AssistantActionCard actionCard) {
}
