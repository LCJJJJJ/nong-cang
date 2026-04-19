package com.nongcang.server.modules.outboundorder.domain.vo;

public record OutboundOrderItemResponse(
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
