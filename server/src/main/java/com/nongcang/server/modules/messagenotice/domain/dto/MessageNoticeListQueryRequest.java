package com.nongcang.server.modules.messagenotice.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MessageNoticeListQueryRequest(
		String noticeCode,
		String severity,
		@Min(value = 1, message = "状态值不正确")
		@Max(value = 2, message = "状态值不正确")
		Integer status) {
}
