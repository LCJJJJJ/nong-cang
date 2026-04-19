package com.nongcang.server.modules.inboundrecord.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InboundRecordEntity(
		Long id,
		String recordCode,
		Long inboundOrderId,
		String inboundOrderCode,
		Long putawayTaskId,
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
		LocalDateTime occurredAt,
		String remarks,
		LocalDateTime createdAt) {
}
