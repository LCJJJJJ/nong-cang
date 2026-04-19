package com.nongcang.server.modules.qualityinspection.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record QualityInspectionListQueryRequest(
		String inspectionCode,
		String sourceType,
		Long productId,
		@Min(value = 1, message = "结果状态值不正确")
		@Max(value = 3, message = "结果状态值不正确")
		Integer resultStatus) {
}
