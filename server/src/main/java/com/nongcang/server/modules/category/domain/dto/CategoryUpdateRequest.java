package com.nongcang.server.modules.category.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
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
		@Min(value = 1, message = "保质期基准必须大于0")
		Integer shelfLifeDays,
		@Min(value = 0, message = "预警提前天数不能小于0")
		Integer warningDays,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
