package com.nongcang.server.modules.putawaytask.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PutawayTaskEntity(
		Long id,
		String taskCode,
		Long inboundOrderId,
		String inboundOrderCode,
		Long inboundOrderItemId,
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
		BigDecimal quantity,
		Integer status,
		String remarks,
		LocalDateTime completedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
