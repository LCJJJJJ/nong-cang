package com.nongcang.server.modules.auth.domain.vo;

public record AuthTokenResponse(
		String tokenType,
		String accessToken,
		String refreshToken,
		String accessTokenExpiresAt,
		String refreshTokenExpiresAt,
		AuthUserResponse user) {
}
