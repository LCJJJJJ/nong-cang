package com.nongcang.server.modules.storagecondition.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record StorageConditionListQueryRequest(
		@Size(max = 64, message = "条件编号长度不能超过64个字符")
		String conditionCode,
		@Size(max = 128, message = "条件名称长度不能超过128个字符")
		String conditionName,
		@Size(max = 64, message = "储存类型长度不能超过64个字符")
		String storageType,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
