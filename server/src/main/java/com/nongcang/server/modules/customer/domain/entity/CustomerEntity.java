package com.nongcang.server.modules.customer.domain.entity;

import java.time.LocalDateTime;

public record CustomerEntity(
		Long id,
		String customerCode,
		String customerName,
		String customerType,
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
