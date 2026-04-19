package com.nongcang.server.modules.lossrecord.domain.dto;

public record LossRecordListQueryRequest(
		String lossCode,
		String sourceType,
		Long warehouseId,
		Long productId) {
}
