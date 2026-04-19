package com.nongcang.server.modules.assistant.service;

import java.util.List;
import java.util.Map;

public record AssistantResultBlock(
		String title,
		String summary,
		String routePath,
		String routeLabel,
		List<AssistantColumn> columns,
		List<Map<String, String>> rows) {
}
