package com.nongcang.server.modules.inventorystocktaking.domain.vo;

import java.util.List;

public record InventoryStocktakingDetailResponse(
		Long id,
		String stocktakingCode,
		Long warehouseId,
		String warehouseName,
		Long zoneId,
		String zoneName,
		Integer status,
		String statusLabel,
		Integer itemCount,
		Integer countedItemCount,
		Double totalDifferenceQuantity,
		String remarks,
		String createdAt,
		String updatedAt,
		List<InventoryStocktakingItemResponse> items) {
}
