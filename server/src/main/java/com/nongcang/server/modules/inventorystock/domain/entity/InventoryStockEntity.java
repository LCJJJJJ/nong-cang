package com.nongcang.server.modules.inventorystock.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryStockEntity(
		Long id,
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
		BigDecimal stockQuantity,
		BigDecimal reservedQuantity,
		BigDecimal availableQuantity,
		LocalDateTime updatedAt) {
}
