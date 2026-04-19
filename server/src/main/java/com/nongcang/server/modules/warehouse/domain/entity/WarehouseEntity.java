package com.nongcang.server.modules.warehouse.domain.entity;

import java.time.LocalDateTime;

public record WarehouseEntity(
		Long id,
		String warehouseCode,
		String warehouseName,
		String warehouseType,
		String managerName,
		String contactPhone,
		String address,
		Integer status,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
