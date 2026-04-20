package com.nongcang.server.modules.category.domain.entity;

import java.time.LocalDateTime;

public record CategoryEntity(
		Long id,
		String categoryCode,
		String categoryName,
		Long parentId,
		Integer categoryLevel,
		String ancestorPath,
		Integer sortOrder,
		Integer status,
		Long defaultStorageConditionId,
		String defaultStorageType,
		String defaultStorageCondition,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
