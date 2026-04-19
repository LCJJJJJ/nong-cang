package com.nongcang.server.modules.warehouse.domain.vo;

public record WarehouseListItemResponse(
		Long id,
		String warehouseCode,
		String warehouseName,
		String warehouseType,
		String managerName,
		String contactPhone,
		String address,
		Integer status,
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
