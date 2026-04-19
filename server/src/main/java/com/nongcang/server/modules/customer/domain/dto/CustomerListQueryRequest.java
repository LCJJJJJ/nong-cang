package com.nongcang.server.modules.customer.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CustomerListQueryRequest(
		String customerCode,
		String customerName,
		String contactName,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
