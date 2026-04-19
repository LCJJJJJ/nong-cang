package com.nongcang.server.modules.warehouselocation.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record WarehouseLocationListQueryRequest(
		String locationCode,
		String locationName,
		Long warehouseId,
		Long zoneId,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
