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
		String defaultStorageType,
		String defaultStorageCondition,
		Integer shelfLifeDays,
		Integer warningDays,
		Boolean requireQualityCheck,
		String remarks,
		String createdAt,
		String updatedAt) {
}
