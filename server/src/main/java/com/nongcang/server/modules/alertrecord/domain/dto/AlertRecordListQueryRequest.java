package com.nongcang.server.modules.alertrecord.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AlertRecordListQueryRequest(
		String alertCode,
		String alertType,
		String severity,
		@Min(value = 1, message = "状态值不正确")
		@Max(value = 3, message = "状态值不正确")
		Integer status) {
}
