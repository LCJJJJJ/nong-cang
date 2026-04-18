package com.nongcang.server.modules.auth.domain.vo;

import java.util.List;

public record AuthUserResponse(
		Long userId,
		String username,
		String displayName,
		String phone,
		List<String> roles) {
}
