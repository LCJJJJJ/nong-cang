package com.nongcang.server.modules.category.domain.vo;

import java.util.List;

public record CategoryOptionResponse(
		Long id,
		String label,
		Integer categoryLevel,
		String ancestorPath,
		List<CategoryOptionResponse> children) {
}
