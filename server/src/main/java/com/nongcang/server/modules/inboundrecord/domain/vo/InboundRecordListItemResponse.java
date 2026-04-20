package com.nongcang.server.modules.inboundrecord.domain.vo;

public record InboundRecordListItemResponse(
		Long id,
		String recordCode,
		Long inboundOrderId,
		String inboundOrderCode,
		Long putawayTaskId,
		Long supplierId,
		String supplierName,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		Long productId,
		String productCode,
		String productName,
		Double quantity,
		Integer shelfLifeDaysSnapshot,
		Integer warningDaysSnapshot,
		String expectedExpireAt,
		String occurredAt,
		String remarks,
		String createdAt) {
}
