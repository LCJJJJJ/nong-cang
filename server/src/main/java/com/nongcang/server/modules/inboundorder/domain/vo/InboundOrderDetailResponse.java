package com.nongcang.server.modules.inboundorder.domain.vo;

import java.util.List;

public record InboundOrderDetailResponse(
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
		String updatedAt,
		List<InboundOrderItemResponse> items) {
}
