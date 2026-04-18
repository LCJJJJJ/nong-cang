package com.nongcang.server.modules.productunit.domain.vo;

public record ProductUnitDetailResponse(
		Long id,
		String unitCode,
		String unitName,
		String unitSymbol,
		String unitType,
		Integer precisionDigits,
		Integer status,
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
