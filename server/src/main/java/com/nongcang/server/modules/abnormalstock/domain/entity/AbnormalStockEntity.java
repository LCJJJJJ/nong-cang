package com.nongcang.server.modules.abnormalstock.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AbnormalStockEntity(
		Long id,
		String abnormalCode,
		Long qualityInspectionId,
		String inspectionCode,
		Long productId,
		String productCode,
		String productName,
		String unitName,
		String unitSymbol,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		BigDecimal lockedQuantity,
		Integer status,
		String reason,
		String remarks,
		LocalDateTime processedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
