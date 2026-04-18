package com.nongcang.server.modules.productorigin.domain.vo;

public record ProductOriginDetailResponse(
		Long id,
		String originCode,
		String originName,
		String countryName,
		String provinceName,
		String cityName,
		Integer status,
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
