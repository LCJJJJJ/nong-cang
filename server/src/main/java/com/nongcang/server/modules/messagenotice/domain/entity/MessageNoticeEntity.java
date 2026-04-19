package com.nongcang.server.modules.messagenotice.domain.entity;

import java.time.LocalDateTime;

public record MessageNoticeEntity(
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
		LocalDateTime createdAt,
		LocalDateTime readAt) {
}
