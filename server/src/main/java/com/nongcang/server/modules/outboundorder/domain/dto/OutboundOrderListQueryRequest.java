package com.nongcang.server.modules.outboundorder.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record OutboundOrderListQueryRequest(
		String orderCode,
		Long customerId,
		Long warehouseId,
		@Min(value = 1, message = "状态值不正确")
		@Max(value = 5, message = "状态值不正确")
		Integer status) {
}
