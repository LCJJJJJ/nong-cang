package com.nongcang.server.modules.qualityinspection.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QualityInspectionCreateRequest(
		@NotBlank(message = "质检来源不能为空")
		String sourceType,
		@NotNull(message = "质检来源记录不能为空")
		Long sourceId,
		@NotNull(message = "送检数量不能为空")
		BigDecimal inspectQuantity,
		@NotNull(message = "不合格数量不能为空")
		BigDecimal unqualifiedQuantity,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
