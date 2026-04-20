package com.nongcang.server.modules.category.domain.vo;

public record CategoryDetailResponse(
		Long id,
		String categoryCode,
		String categoryName,
		Long parentId,
		String parentName,
		Integer categoryLevel,
		String ancestorPath,
		Integer sortOrder,
		Integer status,
		String statusLabel,
		Long defaultStorageConditionId,
		String defaultStorageType,
		String defaultStorageCondition,
		String remarks,
		String createdAt,
		String updatedAt) {
}
