package com.nongcang.server.modules.assistant.service;

public record AssistantToolArguments(
		String entityType,
		String keyword,
		String relatedKeyword,
		Integer limit) {

	public int resolveLimit(int fallback, int maxLimit) {
		if (limit == null || limit <= 0) {
			return fallback;
		}
		return Math.min(limit, maxLimit);
	}
}
