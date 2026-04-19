package com.nongcang.server.modules.inventorystocktaking.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryStocktakingOrderEntity(
		Long id,
		String stocktakingCode,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Integer status,
		Integer itemCount,
		Integer countedItemCount,
		BigDecimal totalDifferenceQuantity,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
