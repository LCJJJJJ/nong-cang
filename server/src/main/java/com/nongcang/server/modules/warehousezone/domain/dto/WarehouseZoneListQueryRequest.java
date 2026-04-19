package com.nongcang.server.modules.warehousezone.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record WarehouseZoneListQueryRequest(
		String zoneCode,
		String zoneName,
		Long warehouseId,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
