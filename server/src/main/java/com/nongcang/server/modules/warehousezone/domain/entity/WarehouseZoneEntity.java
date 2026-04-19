package com.nongcang.server.modules.warehousezone.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WarehouseZoneEntity(
		Long id,
		String zoneCode,
		Long warehouseId,
		String warehouseName,
		String zoneName,
		String zoneType,
		BigDecimal temperatureMin,
		BigDecimal temperatureMax,
		Integer status,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
