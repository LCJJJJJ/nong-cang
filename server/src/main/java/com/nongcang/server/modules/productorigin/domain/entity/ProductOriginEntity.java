package com.nongcang.server.modules.productorigin.domain.entity;

import java.time.LocalDateTime;

public record ProductOriginEntity(
		Long id,
		String originCode,
		String originName,
		String countryName,
		String provinceName,
		String cityName,
		Integer status,
		Integer sortOrder,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
