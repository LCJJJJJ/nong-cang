package com.nongcang.server.modules.outboundorder.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OutboundOrderItemRequest(
		@NotNull(message = "产品不能为空")
		Long productId,
		@NotNull(message = "出库数量不能为空")
		BigDecimal quantity,
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
