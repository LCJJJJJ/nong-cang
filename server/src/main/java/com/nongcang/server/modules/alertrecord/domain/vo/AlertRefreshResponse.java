package com.nongcang.server.modules.alertrecord.domain.vo;

public record AlertRefreshResponse(
		Integer createdCount,
		Integer resolvedCount,
		Integer activeCount,
		Integer ignoredCount) {
}
