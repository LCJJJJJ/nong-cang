package com.nongcang.server.modules.inventorystocktaking.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record InventoryStocktakingListQueryRequest(
		String stocktakingCode,
		Long warehouseId,
		@Min(value = 1, message = "状态值不正确")
		@Max(value = 4, message = "状态值不正确")
		Integer status) {
}
