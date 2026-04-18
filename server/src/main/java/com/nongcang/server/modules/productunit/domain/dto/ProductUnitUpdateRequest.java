package com.nongcang.server.modules.productunit.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUnitUpdateRequest(
		@NotBlank(message = "单位名称不能为空")
		@Size(max = 64, message = "单位名称长度不能超过64个字符")
		String unitName,
		@NotBlank(message = "单位符号不能为空")
		@Size(max = 32, message = "单位符号长度不能超过32个字符")
		String unitSymbol,
		@NotBlank(message = "单位类型不能为空")
		@Size(max = 32, message = "单位类型长度不能超过32个字符")
		String unitType,
		@NotNull(message = "精度位数不能为空")
		@Min(value = 0, message = "精度位数不能小于0")
		Integer precisionDigits,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		@NotNull(message = "排序值不能为空")
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
