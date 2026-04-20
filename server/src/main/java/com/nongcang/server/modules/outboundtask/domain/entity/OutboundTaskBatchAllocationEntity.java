package com.nongcang.server.modules.outboundtask.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OutboundTaskBatchAllocationEntity(
		Long id,
		Long outboundTaskId,
		Long inventoryBatchId,
		BigDecimal allocatedQuantity,
		LocalDateTime createdAt) {
}
