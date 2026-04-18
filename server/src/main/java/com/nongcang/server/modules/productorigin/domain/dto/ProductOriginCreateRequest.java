package com.nongcang.server.modules.productorigin.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductOriginCreateRequest(
		@NotBlank(message = "产地名称不能为空")
		@Size(max = 128, message = "产地名称长度不能超过128个字符")
		String originName,
		@NotBlank(message = "国家名称不能为空")
		@Size(max = 64, message = "国家名称长度不能超过64个字符")
		String countryName,
		@NotBlank(message = "省份名称不能为空")
		@Size(max = 64, message = "省份名称长度不能超过64个字符")
		String provinceName,
		@Size(max = 64, message = "城市名称长度不能超过64个字符")
		String cityName,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		@NotNull(message = "排序值不能为空")
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
