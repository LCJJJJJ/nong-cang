package com.nongcang.server.modules.alertrule.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlertRuleUpdateRequest(
		@NotBlank(message = "严重级别不能为空")
		String severity,
		@NotNull(message = "阈值不能为空")
		BigDecimal thresholdValue,
		@Size(max = 255, message = "规则说明长度不能超过255个字符")
		String description,
		Integer sortOrder) {
}
