package com.nongcang.server.modules.assistant.service;

import java.util.Map;

public record AssistantWriteActionToolArguments(
		String actionCode,
		String resourceType,
		String actionType,
		String target,
		Map<String, Object> fields) {
}
