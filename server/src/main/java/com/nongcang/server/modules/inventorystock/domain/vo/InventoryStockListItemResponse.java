package com.nongcang.server.modules.inventorystock.domain.vo;

public record InventoryStockListItemResponse(
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
		Double stockQuantity,
		Double reservedQuantity,
		Double lockedQuantity,
		Double availableQuantity,
		String nearestExpireAt,
		Integer remainingShelfLifeDays,
		String updatedAt) {
}
