package com.nongcang.server.modules.outboundorder.domain.vo;

import java.util.List;

public record OutboundOrderDetailResponse(
		Long id,
		String orderCode,
		Long customerId,
		String customerName,
		Long warehouseId,
		String warehouseName,
		String expectedDeliveryAt,
		String actualOutboundAt,
		Integer totalItemCount,
		Double totalQuantity,
		Integer status,
		String statusLabel,
		String remarks,
		String createdAt,
		String updatedAt,
		List<OutboundOrderItemResponse> items) {
}
