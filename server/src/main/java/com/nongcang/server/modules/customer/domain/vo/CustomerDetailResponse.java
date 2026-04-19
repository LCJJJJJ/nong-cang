package com.nongcang.server.modules.customer.domain.vo;

public record CustomerDetailResponse(
		Long id,
		String customerCode,
		String customerName,
		String customerType,
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
