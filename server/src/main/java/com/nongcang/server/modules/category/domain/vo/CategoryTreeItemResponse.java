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
		String defaultStorageType,
		String defaultStorageCondition,
		Integer shelfLifeDays,
		Integer warningDays,
		Boolean requireQualityCheck,
		String remarks,
		String createdAt,
		String updatedAt,
		List<CategoryTreeItemResponse> children) {
}
