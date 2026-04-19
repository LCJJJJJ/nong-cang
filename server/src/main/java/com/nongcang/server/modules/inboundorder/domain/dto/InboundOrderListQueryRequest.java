package com.nongcang.server.modules.inboundorder.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record InboundOrderListQueryRequest(
		String orderCode,
		Long supplierId,
		Long warehouseId,
		@Min(value = 1, message = "状态值不正确")
		@Max(value = 4, message = "状态值不正确")
		Integer status) {
}
