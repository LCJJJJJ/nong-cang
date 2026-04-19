package com.nongcang.server.modules.messagenotice.domain.vo;

public record MessageNoticeListItemResponse(
		Long id,
		String noticeCode,
		Long alertRecordId,
		String noticeType,
		String severity,
		String title,
		String content,
		String sourceType,
		Long sourceId,
		String sourceCode,
		Integer status,
		String statusLabel,
		String createdAt,
		String readAt) {
}
