package com.nongcang.server.modules.outboundrecord.domain.dto;

public record OutboundRecordListQueryRequest(
		String recordCode,
		String orderCode,
		Long warehouseId,
		Long productId) {
}
