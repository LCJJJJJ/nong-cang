package com.nongcang.server.modules.warehouse.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record WarehouseListQueryRequest(
		String warehouseCode,
		String warehouseName,
		String warehouseType,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
