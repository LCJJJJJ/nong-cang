package com.nongcang.server.modules.inventoryadjustment.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryAdjustmentEntity(
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
		BigDecimal quantity,
		String reason,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
