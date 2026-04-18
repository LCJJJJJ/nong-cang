package com.nongcang.server.modules.productunit.domain.entity;

import java.time.LocalDateTime;

public record ProductUnitEntity(
		Long id,
		String unitCode,
		String unitName,
		String unitSymbol,
		String unitType,
		Integer precisionDigits,
		Integer status,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
