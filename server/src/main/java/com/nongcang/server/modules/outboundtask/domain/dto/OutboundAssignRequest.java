package com.nongcang.server.modules.outboundtask.domain.dto;

import jakarta.validation.constraints.NotNull;

public record OutboundAssignRequest(
		@NotNull(message = "库区不能为空")
		Long zoneId,
		@NotNull(message = "库位不能为空")
		Long locationId) {
}
