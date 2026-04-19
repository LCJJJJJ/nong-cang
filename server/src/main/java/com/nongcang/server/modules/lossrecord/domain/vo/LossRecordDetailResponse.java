package com.nongcang.server.modules.lossrecord.domain.vo;

public record LossRecordDetailResponse(
		Long id,
		String lossCode,
		String sourceType,
		Long sourceId,
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
		Double quantity,
		String lossReason,
		String remarks,
		String createdAt) {
}
