package com.nongcang.server.modules.supplier.domain.entity;

import java.time.LocalDateTime;

public record SupplierEntity(
		Long id,
		String supplierCode,
		String supplierName,
		String supplierType,
		String contactName,
		String contactPhone,
		String regionName,
		String address,
		Integer status,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
