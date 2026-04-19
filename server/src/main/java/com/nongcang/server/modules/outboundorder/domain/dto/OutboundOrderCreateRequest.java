package com.nongcang.server.modules.outboundorder.domain.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OutboundOrderCreateRequest(
		@NotNull(message = "客户不能为空")
		Long customerId,
		@NotNull(message = "仓库不能为空")
		Long warehouseId,
		@NotBlank(message = "预计发货时间不能为空")
		String expectedDeliveryAt,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks,
		@NotEmpty(message = "出库单明细不能为空")
		List<@Valid OutboundOrderItemRequest> items) {
}
