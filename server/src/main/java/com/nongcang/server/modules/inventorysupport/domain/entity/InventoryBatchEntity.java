package com.nongcang.server.modules.inventorysupport.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryBatchEntity(
		Long id,
		String batchCode,
		String sourceType,
		Long sourceId,
		Long productId,
		Long warehouseId,
		Long zoneId,
		Long locationId,
		LocalDateTime baseOccurredAt,
		Integer shelfLifeDaysSnapshot,
		Integer warningDaysSnapshot,
		LocalDateTime warningAt,
		LocalDateTime expectedExpireAt,
		BigDecimal initialQuantity,
		BigDecimal remainingQuantity,
		String status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
