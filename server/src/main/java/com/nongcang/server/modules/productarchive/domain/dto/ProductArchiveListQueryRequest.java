package com.nongcang.server.modules.productarchive.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProductArchiveListQueryRequest(
		String productCode,
		String productName,
		Long categoryId,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
