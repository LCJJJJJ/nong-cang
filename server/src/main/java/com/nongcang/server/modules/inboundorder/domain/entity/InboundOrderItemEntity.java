package com.nongcang.server.modules.inboundorder.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InboundOrderItemEntity(
		Long id,
		Long inboundOrderId,
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
