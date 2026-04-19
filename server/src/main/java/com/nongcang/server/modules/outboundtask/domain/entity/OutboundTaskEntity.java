package com.nongcang.server.modules.outboundtask.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OutboundTaskEntity(
		Long id,
		String taskCode,
		Long outboundOrderId,
		String outboundOrderCode,
		Long outboundOrderItemId,
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
		BigDecimal quantity,
		Integer status,
		String remarks,
		LocalDateTime pickedAt,
		LocalDateTime completedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
