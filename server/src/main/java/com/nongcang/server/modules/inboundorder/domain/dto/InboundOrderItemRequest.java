package com.nongcang.server.modules.inboundorder.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InboundOrderItemRequest(
		@NotNull(message = "产品不能为空")
		Long productId,
		@NotNull(message = "入库数量不能为空")
		BigDecimal quantity,
		Integer sortOrder,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
