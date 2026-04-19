package com.nongcang.server.modules.inboundorder.domain.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InboundOrderUpdateRequest(
		@NotNull(message = "供应商不能为空")
		Long supplierId,
		@NotNull(message = "仓库不能为空")
		Long warehouseId,
		@NotBlank(message = "预计到货时间不能为空")
		String expectedArrivalAt,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks,
		@NotEmpty(message = "入库单明细不能为空")
		List<@Valid InboundOrderItemRequest> items) {
}
