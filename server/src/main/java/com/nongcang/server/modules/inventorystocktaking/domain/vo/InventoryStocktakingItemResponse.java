package com.nongcang.server.modules.inventorystocktaking.domain.vo;

public record InventoryStocktakingItemResponse(
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
		Double systemQuantity,
		Double countedQuantity,
		Double differenceQuantity,
		String remarks) {
}
