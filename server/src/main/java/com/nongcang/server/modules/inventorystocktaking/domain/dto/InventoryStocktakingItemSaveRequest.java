package com.nongcang.server.modules.inventorystocktaking.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryStocktakingItemSaveRequest(
		@NotNull(message = "明细ID不能为空")
		Long itemId,
		@NotNull(message = "实盘数量不能为空")
		BigDecimal countedQuantity,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
