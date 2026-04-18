package com.nongcang.server.modules.storagecondition.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StorageConditionUpdateRequest(
		@NotBlank(message = "条件名称不能为空")
		@Size(max = 128, message = "条件名称长度不能超过128个字符")
		String conditionName,
		@NotBlank(message = "储存类型不能为空")
		@Size(max = 64, message = "储存类型长度不能超过64个字符")
		String storageType,
		BigDecimal temperatureMin,
		BigDecimal temperatureMax,
		BigDecimal humidityMin,
		BigDecimal humidityMax,
		@NotBlank(message = "避光要求不能为空")
		@Size(max = 32, message = "避光要求长度不能超过32个字符")
		String lightRequirement,
		@NotBlank(message = "通风要求不能为空")
		@Size(max = 32, message = "通风要求长度不能超过32个字符")
		String ventilationRequirement,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		@NotNull(message = "排序值不能为空")
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
