package com.nongcang.server.modules.abnormalstock.domain.vo;

public record AbnormalStockDetailResponse(
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
		Double lockedQuantity,
		Integer status,
		String statusLabel,
		String reason,
		String remarks,
		String processedAt,
		String createdAt,
		String updatedAt) {
}
