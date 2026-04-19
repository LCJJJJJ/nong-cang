package com.nongcang.server.modules.outboundrecord.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OutboundRecordEntity(
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
		BigDecimal quantity,
		LocalDateTime occurredAt,
		String remarks,
		LocalDateTime createdAt) {
}
