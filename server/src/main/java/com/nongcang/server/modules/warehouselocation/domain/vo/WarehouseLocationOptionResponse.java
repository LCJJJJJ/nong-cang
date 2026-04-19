package com.nongcang.server.modules.warehouselocation.domain.vo;

public record WarehouseLocationOptionResponse(
		Long id,
		Long warehouseId,
		Long zoneId,
		String label,
		String zoneName,
		Integer status) {
}
