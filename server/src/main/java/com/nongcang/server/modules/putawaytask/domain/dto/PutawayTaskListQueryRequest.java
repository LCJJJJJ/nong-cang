package com.nongcang.server.modules.putawaytask.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PutawayTaskListQueryRequest(
		String taskCode,
		Long warehouseId,
		@Min(value = 1, message = "状态值不正确")
		@Max(value = 4, message = "状态值不正确")
		Integer status) {
}
