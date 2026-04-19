package com.nongcang.server.modules.inboundorder.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InboundOrderEntity(
		Long id,
		String orderCode,
		Long supplierId,
		String supplierName,
		Long warehouseId,
		String warehouseName,
		LocalDateTime expectedArrivalAt,
		LocalDateTime actualArrivalAt,
		Integer totalItemCount,
		BigDecimal totalQuantity,
		Integer status,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
