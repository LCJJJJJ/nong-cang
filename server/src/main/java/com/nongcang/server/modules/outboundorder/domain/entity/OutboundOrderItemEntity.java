package com.nongcang.server.modules.outboundorder.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OutboundOrderItemEntity(
		Long id,
		Long outboundOrderId,
		Long productId,
		String productCode,
		String productName,
		String productSpecification,
		Long unitId,
		String unitName,
		String unitSymbol,
		BigDecimal quantity,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
