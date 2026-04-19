package com.nongcang.server.modules.inboundorder.domain.vo;

public record InboundOrderItemResponse(
		Long id,
		Long productId,
		String productCode,
		String productName,
		String productSpecification,
		Long unitId,
		String unitName,
		String unitSymbol,
		Double quantity,
		Integer sortOrder,
		String remarks) {
}
