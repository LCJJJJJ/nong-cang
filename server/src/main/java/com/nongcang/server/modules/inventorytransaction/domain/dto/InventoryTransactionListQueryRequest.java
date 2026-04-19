package com.nongcang.server.modules.inventorytransaction.domain.dto;

public record InventoryTransactionListQueryRequest(
		String transactionCode,
		Long warehouseId,
		Long productId,
		String transactionType) {
}
