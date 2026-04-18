package com.nongcang.server.modules.productunit.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProductUnitListQueryRequest(
		String unitCode,
		String unitName,
		String unitType,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
