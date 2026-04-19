package com.nongcang.server.modules.inventorystocktaking.domain.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record InventoryStocktakingItemSaveBatchRequest(
		@NotEmpty(message = "盘点明细不能为空")
		List<@Valid InventoryStocktakingItemSaveRequest> items) {
}
