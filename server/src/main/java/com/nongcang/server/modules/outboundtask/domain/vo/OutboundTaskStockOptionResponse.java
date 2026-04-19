package com.nongcang.server.modules.outboundtask.domain.vo;

public record OutboundTaskStockOptionResponse(
		Long warehouseId,
		Long zoneId,
		String zoneName,
		Long locationId,
		String locationName,
		Double stockQuantity,
		Double reservedQuantity,
		Double availableQuantity) {
}
