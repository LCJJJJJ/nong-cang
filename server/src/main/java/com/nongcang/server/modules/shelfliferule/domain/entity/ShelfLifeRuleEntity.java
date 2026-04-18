package com.nongcang.server.modules.shelfliferule.domain.entity;

import java.time.LocalDateTime;

public record ShelfLifeRuleEntity(
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
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
