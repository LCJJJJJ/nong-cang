package com.nongcang.server.modules.inboundorder.domain.vo;

public record InboundOrderListItemResponse(
		Long id,
		String orderCode,
		Long supplierId,
		String supplierName,
		Long warehouseId,
		String warehouseName,
		String expectedArrivalAt,
		String actualArrivalAt,
		Integer totalItemCount,
		Double totalQuantity,
		Integer status,
		String statusLabel,
		String remarks,
		String createdAt,
		String updatedAt) {
}
