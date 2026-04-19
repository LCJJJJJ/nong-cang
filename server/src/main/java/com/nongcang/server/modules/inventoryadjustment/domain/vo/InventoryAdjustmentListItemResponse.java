package com.nongcang.server.modules.inventoryadjustment.domain.vo;

public record InventoryAdjustmentListItemResponse(
		Long id,
		String adjustmentCode,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		Long productId,
		String productCode,
		String productName,
		String adjustmentType,
		Double quantity,
		String reason,
		String remarks,
		String createdAt,
		String updatedAt) {
}
