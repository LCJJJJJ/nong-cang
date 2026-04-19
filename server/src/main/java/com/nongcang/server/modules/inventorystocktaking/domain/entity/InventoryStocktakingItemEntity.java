package com.nongcang.server.modules.inventorystocktaking.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryStocktakingItemEntity(
		Long id,
		Long stocktakingOrderId,
		Long productId,
		String productCode,
		String productName,
		String productSpecification,
		Long unitId,
		String unitName,
		String unitSymbol,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		BigDecimal systemQuantity,
		BigDecimal countedQuantity,
		BigDecimal differenceQuantity,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
