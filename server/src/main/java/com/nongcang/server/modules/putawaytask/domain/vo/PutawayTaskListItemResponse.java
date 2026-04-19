package com.nongcang.server.modules.putawaytask.domain.vo;

public record PutawayTaskListItemResponse(
		Long id,
		String taskCode,
		Long inboundOrderId,
		String inboundOrderCode,
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
		Integer status,
		String statusLabel,
		String remarks,
		String completedAt,
		String createdAt,
		String updatedAt) {
}
