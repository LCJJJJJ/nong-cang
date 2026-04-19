package com.nongcang.server.modules.warehouselocation.domain.vo;

public record WarehouseLocationListItemResponse(
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
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
