package com.nongcang.server.modules.qualitygrade.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QualityGradeEntity(
		Long id,
		String gradeCode,
		String gradeName,
		BigDecimal scoreMin,
		BigDecimal scoreMax,
		Integer status,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
