package com.nongcang.server.modules.abnormalstock.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AbnormalStockBatchLockEntity(
		Long id,
		Long abnormalStockId,
		Long inventoryBatchId,
		BigDecimal lockedQuantity,
		LocalDateTime createdAt) {
}
