package com.nongcang.server.modules.qualityinspection.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QualityInspectionEntity(
		Long id,
		String inspectionCode,
		String sourceType,
		Long sourceId,
		String sourceCode,
		String sourceLabel,
		Long productId,
		String productCode,
		String productName,
		String unitName,
		String unitSymbol,
		Integer precisionDigits,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		BigDecimal inspectQuantity,
		BigDecimal qualifiedQuantity,
		BigDecimal unqualifiedQuantity,
		Integer resultStatus,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
