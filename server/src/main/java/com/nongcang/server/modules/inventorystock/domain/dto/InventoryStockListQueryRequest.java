package com.nongcang.server.modules.inventorystock.domain.dto;

public record InventoryStockListQueryRequest(
		Long productId,
		Long warehouseId,
		Long zoneId) {
}
