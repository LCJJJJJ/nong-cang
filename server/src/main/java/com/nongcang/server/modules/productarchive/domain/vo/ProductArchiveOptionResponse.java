package com.nongcang.server.modules.productarchive.domain.vo;

public record ProductArchiveOptionResponse(
		Long id,
		String label,
		String unitName,
		String unitSymbol,
		Integer precisionDigits,
		Integer status) {
}
