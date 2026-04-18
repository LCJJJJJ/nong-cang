package com.nongcang.server.modules.shelfliferule.domain.vo;

public record ShelfLifeRuleOptionResponse(
		Long id,
		String label,
		Integer shelfLifeDays,
		Integer warningDays,
		Integer status) {
}
