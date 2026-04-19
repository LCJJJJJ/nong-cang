package com.nongcang.server.modules.supplier.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupplierUpdateRequest(
		@NotBlank(message = "供应商名称不能为空")
		@Size(max = 128, message = "供应商名称长度不能超过128个字符")
		String supplierName,
		@NotBlank(message = "供应商类型不能为空")
		@Size(max = 32, message = "供应商类型长度不能超过32个字符")
		String supplierType,
		@Size(max = 64, message = "联系人长度不能超过64个字符")
		String contactName,
		@Size(max = 32, message = "联系电话长度不能超过32个字符")
		String contactPhone,
		@Size(max = 128, message = "所在地区长度不能超过128个字符")
		String regionName,
		@Size(max = 255, message = "详细地址长度不能超过255个字符")
		String address,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		@NotNull(message = "排序值不能为空")
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
