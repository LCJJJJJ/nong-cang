package com.nongcang.server.modules.warehousezone.domain.vo;

public record WarehouseZoneOptionResponse(
		Long id,
		Long warehouseId,
		String label,
		String warehouseName,
		Integer status) {
}
