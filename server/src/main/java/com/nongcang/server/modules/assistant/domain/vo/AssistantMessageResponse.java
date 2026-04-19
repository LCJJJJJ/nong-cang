package com.nongcang.server.modules.assistant.domain.vo;

import java.util.List;
import java.util.Map;

public record AssistantMessageResponse(
		Long id,
		String role,
		String content,
		String messageType,
		List<AssistantResultBlockResponse> resultBlocks,
		String createdAt) {

	public record AssistantResultBlockResponse(
			String title,
			String summary,
			String routePath,
			String routeLabel,
			List<AssistantColumnResponse> columns,
			List<Map<String, String>> rows) {
	}
}
