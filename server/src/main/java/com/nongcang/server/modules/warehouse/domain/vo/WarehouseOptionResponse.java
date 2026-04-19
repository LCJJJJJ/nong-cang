package com.nongcang.server.modules.warehouse.domain.vo;

public record WarehouseOptionResponse(
		Long id,
		String label,
		String warehouseType,
		Integer status) {
}
