package com.nongcang.server.modules.abnormalstock.domain.vo;

public record AbnormalStockOptionResponse(
		Long id,
		String label,
		Double lockedQuantity,
		Integer status) {
}
