package com.nongcang.server.modules.shelfliferule.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ShelfLifeRuleListQueryRequest(
		String ruleCode,
		String ruleName,
		Long categoryId,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
