package com.nongcang.server.modules.productarchive.domain.entity;

import java.time.LocalDateTime;

public record ProductArchiveEntity(
		Long id,
		String productCode,
		String productName,
		String productSpecification,
		Long categoryId,
		String categoryName,
		Long unitId,
		String unitName,
		String unitSymbol,
		Long originId,
		String originName,
		Long storageConditionId,
		String storageConditionName,
		Integer shelfLifeDays,
		Integer warningDays,
		Long qualityGradeId,
		String qualityGradeName,
		Integer status,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
