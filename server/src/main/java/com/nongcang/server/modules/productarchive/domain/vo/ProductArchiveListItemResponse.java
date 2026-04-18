package com.nongcang.server.modules.productarchive.domain.vo;

public record ProductArchiveListItemResponse(
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
		Long shelfLifeRuleId,
		String shelfLifeRuleName,
		Long qualityGradeId,
		String qualityGradeName,
		Integer status,
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
