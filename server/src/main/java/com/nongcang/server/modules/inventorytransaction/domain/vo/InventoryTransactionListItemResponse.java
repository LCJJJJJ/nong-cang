package com.nongcang.server.modules.inventorytransaction.domain.vo;

public record InventoryTransactionListItemResponse(
		Long id,
		String transactionCode,
		String transactionType,
		Long productId,
		String productCode,
		String productName,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		Double quantity,
		String sourceType,
		Long sourceId,
		String occurredAt,
		String remarks,
		String createdAt) {
}
