package com.nongcang.server.modules.supplier.domain.vo;

public record SupplierDetailResponse(
		Long id,
		String supplierCode,
		String supplierName,
		String supplierType,
		String contactName,
		String contactPhone,
		String regionName,
		String address,
		Integer status,
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
