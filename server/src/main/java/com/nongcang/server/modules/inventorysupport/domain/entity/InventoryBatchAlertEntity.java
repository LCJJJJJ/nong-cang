package com.nongcang.server.modules.inventorysupport.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryBatchAlertEntity(
		Long id,
		String batchCode,
		String sourceType,
		Long sourceId,
		Long productId,
		String productCode,
		String productName,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		LocalDateTime warningAt,
		LocalDateTime expectedExpireAt,
		BigDecimal remainingQuantity) {
}
