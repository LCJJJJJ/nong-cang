package com.nongcang.server.modules.inventorytransaction.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryTransactionEntity(
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
		BigDecimal quantity,
		String sourceType,
		Long sourceId,
		LocalDateTime occurredAt,
		String remarks,
		LocalDateTime createdAt) {
}
