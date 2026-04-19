package com.nongcang.server.modules.inventorystocktaking.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryStocktakingCreateRequest(
		@NotNull(message = "仓库不能为空")
		Long warehouseId,
		Long zoneId,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
