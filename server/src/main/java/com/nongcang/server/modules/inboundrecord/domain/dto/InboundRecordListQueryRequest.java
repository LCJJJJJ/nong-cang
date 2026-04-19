package com.nongcang.server.modules.inboundrecord.domain.dto;

public record InboundRecordListQueryRequest(
		String recordCode,
		String orderCode,
		Long warehouseId,
		Long productId) {
}
