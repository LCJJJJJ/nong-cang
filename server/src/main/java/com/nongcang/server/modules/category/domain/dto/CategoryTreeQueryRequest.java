package com.nongcang.server.modules.category.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CategoryTreeQueryRequest(
		@Size(max = 64, message = "分类编号长度不能超过64个字符")
		String categoryCode,
		@Size(max = 128, message = "分类名称长度不能超过128个字符")
		String categoryName,
		Long parentId,
		@Min(value = 1, message = "层级必须大于0")
		@Max(value = 3, message = "层级超过当前系统限制")
		Integer level,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
