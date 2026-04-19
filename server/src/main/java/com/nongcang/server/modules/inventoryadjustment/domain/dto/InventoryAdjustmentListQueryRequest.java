package com.nongcang.server.modules.inventoryadjustment.domain.dto;

public record InventoryAdjustmentListQueryRequest(
		String adjustmentCode,
		Long warehouseId,
		Long productId,
		String adjustmentType) {
}
