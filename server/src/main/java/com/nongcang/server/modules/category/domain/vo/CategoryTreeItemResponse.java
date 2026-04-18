package com.nongcang.server.modules.category.domain.vo;

import java.util.List;

public record CategoryTreeItemResponse(
		Long id,
		String categoryCode,
		String categoryName,
		Long parentId,
		Integer categoryLevel,
		String ancestorPath,
		Integer sortOrder,
		Integer status,
		String statusLabel,
		Long defaultStorageConditionId,
		String defaultStorageType,
		String defaultStorageCondition,
		Integer shelfLifeDays,
		Integer warningDays,
		String remarks,
		String createdAt,
		String updatedAt,
		List<CategoryTreeItemResponse> children) {
}
