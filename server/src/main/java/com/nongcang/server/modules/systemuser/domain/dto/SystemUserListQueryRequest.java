package com.nongcang.server.modules.systemuser.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SystemUserListQueryRequest(
		String username,
		String displayName,
		String roleCode,
		Long warehouseId,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
