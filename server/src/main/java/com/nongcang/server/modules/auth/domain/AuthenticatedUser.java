package com.nongcang.server.modules.auth.domain;

import java.util.List;

public record AuthenticatedUser(
		Long userId,
		String username,
		String displayName,
		String phone,
		List<String> roles) {
}
