package com.nongcang.server.modules.supplier.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SupplierListQueryRequest(
		String supplierCode,
		String supplierName,
		String contactName,
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status) {
}
