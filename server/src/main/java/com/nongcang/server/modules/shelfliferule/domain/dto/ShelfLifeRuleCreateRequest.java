package com.nongcang.server.modules.shelfliferule.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShelfLifeRuleCreateRequest(
		@NotBlank(message = "规则名称不能为空")
		@Size(max = 128, message = "规则名称长度不能超过128个字符")
		String ruleName,
		Long categoryId,
		Long storageConditionId,
		@NotNull(message = "保质期天数不能为空")
		@Min(value = 1, message = "保质期天数必须大于0")
		Integer shelfLifeDays,
		@NotNull(message = "预警提前天数不能为空")
		@Min(value = 0, message = "预警提前天数不能小于0")
		Integer warningDays,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		@NotNull(message = "排序值不能为空")
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
