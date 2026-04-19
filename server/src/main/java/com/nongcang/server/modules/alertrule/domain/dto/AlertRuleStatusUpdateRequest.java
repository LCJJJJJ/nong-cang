package com.nongcang.server.modules.alertrule.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AlertRuleStatusUpdateRequest(
		@NotNull(message = "启用状态不能为空")
		@Min(value = 0, message = "启用状态值不正确")
		@Max(value = 1, message = "启用状态值不正确")
		Integer enabled) {
}
