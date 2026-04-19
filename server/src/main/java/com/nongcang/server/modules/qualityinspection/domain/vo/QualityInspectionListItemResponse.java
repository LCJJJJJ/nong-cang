package com.nongcang.server.modules.qualityinspection.domain.vo;

public record QualityInspectionListItemResponse(
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
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		Double inspectQuantity,
		Double qualifiedQuantity,
		Double unqualifiedQuantity,
		Integer resultStatus,
		String resultStatusLabel,
		String remarks,
		String createdAt,
		String updatedAt) {
}
