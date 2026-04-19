package com.nongcang.server.modules.lossrecord.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LossRecordEntity(
		Long id,
		String lossCode,
		String sourceType,
		Long sourceId,
		Long productId,
		Long warehouseId,
		Long zoneId,
		Long locationId,
		BigDecimal quantity,
		String lossReason,
		String remarks,
		LocalDateTime createdAt) {
}
