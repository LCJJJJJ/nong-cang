package com.nongcang.server.modules.storagecondition.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StorageConditionEntity(
		Long id,
		String conditionCode,
		String conditionName,
		String storageType,
		BigDecimal temperatureMin,
		BigDecimal temperatureMax,
		BigDecimal humidityMin,
		BigDecimal humidityMax,
		String lightRequirement,
		String ventilationRequirement,
		Integer status,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
