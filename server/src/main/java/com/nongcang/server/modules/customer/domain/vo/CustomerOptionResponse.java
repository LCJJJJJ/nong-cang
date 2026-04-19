package com.nongcang.server.modules.customer.domain.vo;

public record CustomerOptionResponse(
		Long id,
		String label,
		String customerType,
		Integer status) {
}
