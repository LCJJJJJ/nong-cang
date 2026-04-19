package com.nongcang.server.modules.warehousezone.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WarehouseZoneUpdateRequest(
		@NotNull(message = "所属仓库不能为空")
		Long warehouseId,
		@NotBlank(message = "库区名称不能为空")
		@Size(max = 128, message = "库区名称长度不能超过128个字符")
		String zoneName,
		@NotBlank(message = "库区类型不能为空")
		@Size(max = 32, message = "库区类型长度不能超过32个字符")
		String zoneType,
		BigDecimal temperatureMin,
		BigDecimal temperatureMax,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		@NotNull(message = "排序值不能为空")
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
