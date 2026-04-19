package com.nongcang.server.modules.warehouselocation.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WarehouseLocationUpdateRequest(
		@NotNull(message = "所属仓库不能为空")
		Long warehouseId,
		@NotNull(message = "所属库区不能为空")
		Long zoneId,
		@NotBlank(message = "库位名称不能为空")
		@Size(max = 128, message = "库位名称长度不能超过128个字符")
		String locationName,
		@NotBlank(message = "库位类型不能为空")
		@Size(max = 32, message = "库位类型长度不能超过32个字符")
		String locationType,
		Integer capacity,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		@NotNull(message = "排序值不能为空")
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
