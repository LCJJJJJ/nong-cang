package com.nongcang.server.modules.productarchive.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductArchiveUpdateRequest(
		@NotBlank(message = "产品名称不能为空")
		@Size(max = 128, message = "产品名称长度不能超过128个字符")
		String productName,
		@Size(max = 128, message = "产品规格长度不能超过128个字符")
		String productSpecification,
		@NotNull(message = "产品分类不能为空")
		Long categoryId,
		@NotNull(message = "产品单位不能为空")
		Long unitId,
		@NotNull(message = "产地信息不能为空")
		Long originId,
		@NotNull(message = "储存条件不能为空")
		Long storageConditionId,
		@NotNull(message = "保质期规则不能为空")
		Long shelfLifeRuleId,
		@NotNull(message = "品质等级不能为空")
		Long qualityGradeId,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		@NotNull(message = "排序值不能为空")
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
