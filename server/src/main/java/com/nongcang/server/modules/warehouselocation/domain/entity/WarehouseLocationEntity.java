package com.nongcang.server.modules.warehouselocation.domain.entity;

import java.time.LocalDateTime;

public record WarehouseLocationEntity(
		Long id,
		String locationCode,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		String locationName,
		String locationType,
		Integer capacity,
		Integer status,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
