package com.nongcang.server.modules.category.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
		@NotBlank(message = "分类名称不能为空")
		@Size(max = 128, message = "分类名称长度不能超过128个字符")
		String categoryName,
		Long parentId,
		@NotNull(message = "排序值不能为空")
		Integer sortOrder,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		Long defaultStorageConditionId,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
