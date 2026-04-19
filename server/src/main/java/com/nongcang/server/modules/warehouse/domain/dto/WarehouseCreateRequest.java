package com.nongcang.server.modules.warehouse.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WarehouseCreateRequest(
		@NotBlank(message = "仓库名称不能为空")
		@Size(max = 128, message = "仓库名称长度不能超过128个字符")
		String warehouseName,
		@NotBlank(message = "仓库类型不能为空")
		@Size(max = 32, message = "仓库类型长度不能超过32个字符")
		String warehouseType,
		@Size(max = 64, message = "负责人长度不能超过64个字符")
		String managerName,
		@Size(max = 32, message = "联系电话长度不能超过32个字符")
		String contactPhone,
		@Size(max = 255, message = "仓库地址长度不能超过255个字符")
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
