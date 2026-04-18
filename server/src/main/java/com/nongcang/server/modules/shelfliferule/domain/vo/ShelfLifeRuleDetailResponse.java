package com.nongcang.server.modules.shelfliferule.domain.vo;

public record ShelfLifeRuleDetailResponse(
		Long id,
		String ruleCode,
		String ruleName,
		Long categoryId,
		String categoryName,
		Long storageConditionId,
		String storageConditionName,
		String storageType,
		Integer shelfLifeDays,
		Integer warningDays,
		Integer status,
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
