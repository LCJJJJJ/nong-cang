package com.nongcang.server.modules.supplier.domain.vo;

public record SupplierOptionResponse(
		Long id,
		String label,
		String supplierType,
		Integer status) {
}
