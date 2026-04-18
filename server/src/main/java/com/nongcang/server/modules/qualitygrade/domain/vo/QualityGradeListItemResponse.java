package com.nongcang.server.modules.qualitygrade.domain.vo;

public record QualityGradeListItemResponse(
		Long id,
		String gradeCode,
		String gradeName,
		Double scoreMin,
		Double scoreMax,
		Integer status,
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
