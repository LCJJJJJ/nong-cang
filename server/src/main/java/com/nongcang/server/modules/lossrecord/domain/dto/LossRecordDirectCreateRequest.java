package com.nongcang.server.modules.lossrecord.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LossRecordDirectCreateRequest(
		@NotNull(message = "仓库不能为空")
		Long warehouseId,
		@NotNull(message = "库区不能为空")
		Long zoneId,
		@NotNull(message = "库位不能为空")
		Long locationId,
		@NotNull(message = "产品不能为空")
		Long productId,
		@NotNull(message = "损耗数量不能为空")
		BigDecimal quantity,
		@NotBlank(message = "损耗原因不能为空")
		@Size(max = 64, message = "损耗原因长度不能超过64个字符")
		String lossReason,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
