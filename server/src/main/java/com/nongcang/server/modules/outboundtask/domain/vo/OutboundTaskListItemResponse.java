package com.nongcang.server.modules.outboundtask.domain.vo;

public record OutboundTaskListItemResponse(
		Long id,
		String taskCode,
		Long outboundOrderId,
		String outboundOrderCode,
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
		Integer status,
		String statusLabel,
		String remarks,
		String pickedAt,
		String completedAt,
		String createdAt,
		String updatedAt) {
}
