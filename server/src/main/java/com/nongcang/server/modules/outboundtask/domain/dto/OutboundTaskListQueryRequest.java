package com.nongcang.server.modules.outboundtask.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record OutboundTaskListQueryRequest(
		String taskCode,
		Long warehouseId,
		@Min(value = 1, message = "状态值不正确")
		@Max(value = 5, message = "状态值不正确")
		Integer status) {
}
