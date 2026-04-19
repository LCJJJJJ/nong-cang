package com.nongcang.server.modules.auth.domain.vo;

import java.util.List;

public record AuthUserResponse(
		Long userId,
		String username,
		String displayName,
		String phone,
		String roleCode,
		Long warehouseId,
		String warehouseName,
		List<String> roles) {
}
