package com.nongcang.server.modules.abnormalstock.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AbnormalStockListQueryRequest(
		String abnormalCode,
		Long productId,
		Long warehouseId,
		@Min(value = 1, message = "状态值不正确")
		@Max(value = 3, message = "状态值不正确")
		Integer status) {
}
