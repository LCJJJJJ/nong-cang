package com.nongcang.server.modules.warehousezone.domain.vo;

public record WarehouseZoneDetailResponse(
		Long id,
		String zoneCode,
		Long warehouseId,
		String warehouseName,
		String zoneName,
		String zoneType,
		Double temperatureMin,
		Double temperatureMax,
		Integer status,
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
