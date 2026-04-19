package com.nongcang.server.modules.outboundrecord.domain.vo;

public record OutboundRecordListItemResponse(
		Long id,
		String recordCode,
		Long outboundOrderId,
		String outboundOrderCode,
		Long outboundTaskId,
		Long customerId,
		String customerName,
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
		String occurredAt,
		String remarks,
		String createdAt) {
}
