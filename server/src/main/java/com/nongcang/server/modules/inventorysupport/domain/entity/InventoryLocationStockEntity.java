package com.nongcang.server.modules.inventorysupport.domain.entity;

import java.math.BigDecimal;

public record InventoryLocationStockEntity(
		Long warehouseId,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		BigDecimal stockQuantity,
		BigDecimal reservedQuantity,
		BigDecimal availableQuantity) {
}
