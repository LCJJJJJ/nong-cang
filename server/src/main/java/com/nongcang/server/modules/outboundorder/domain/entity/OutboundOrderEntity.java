package com.nongcang.server.modules.outboundorder.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OutboundOrderEntity(
		Long id,
		String orderCode,
		Long customerId,
		String customerName,
		Long warehouseId,
		String warehouseName,
		LocalDateTime expectedDeliveryAt,
		LocalDateTime actualOutboundAt,
		Integer totalItemCount,
		BigDecimal totalQuantity,
		Integer status,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
