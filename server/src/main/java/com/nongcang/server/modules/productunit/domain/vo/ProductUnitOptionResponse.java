package com.nongcang.server.modules.productunit.domain.vo;

public record ProductUnitOptionResponse(
		Long id,
		String label,
		String unitSymbol,
		Integer status) {
}
