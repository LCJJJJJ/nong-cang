package com.nongcang.server.modules.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.config.AuthProperties;
import com.nongcang.server.modules.auth.domain.AuthenticatedUser;
import com.nongcang.server.modules.auth.domain.vo.AuthTokenResponse;
import com.nongcang.server.modules.auth.domain.vo.AuthUserResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

	private static final String TOKEN_TYPE_CLAIM = "tokenType";

	private static final String USER_ID_CLAIM = "userId";

	private static final String DISPLAY_NAME_CLAIM = "displayName";

	private static final String PHONE_CLAIM = "phone";

	private static final String ROLES_CLAIM = "roles";

	private final AuthProperties authProperties;

	private final SecretKey secretKey;

	public JwtTokenService(AuthProperties authProperties) {
		this.authProperties = authProperties;
		this.secretKey = Keys.hmacShaKeyFor(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
	}

	public AuthTokenResponse issueTokens(AuthenticatedUser authenticatedUser) {
		Instant now = Instant.now();
		Instant accessTokenExpiresAt = now.plusSeconds(authProperties.getAccessTokenExpireSeconds());
		Instant refreshTokenExpiresAt = now.plusSeconds(authProperties.getRefreshTokenExpireSeconds());

		String accessToken = buildToken(authenticatedUser, "access", accessTokenExpiresAt);
		String refreshToken = buildToken(authenticatedUser, "refresh", refreshTokenExpiresAt);

		return new AuthTokenResponse(
				"Bearer",
				accessToken,
				refreshToken,
				OffsetDateTime.ofInstant(accessTokenExpiresAt, ZoneOffset.ofHours(8)).toString(),
				OffsetDateTime.ofInstant(refreshTokenExpiresAt, ZoneOffset.ofHours(8)).toString(),
				toUserResponse(authenticatedUser));
	}

	public AuthenticatedUser parseAccessToken(String accessToken) {
		return parseToken(accessToken, "access", CommonErrorCode.INVALID_ACCESS_TOKEN);
	}

	public AuthenticatedUser parseRefreshToken(String refreshToken) {
		return parseToken(refreshToken, "refresh", CommonErrorCode.INVALID_REFRESH_TOKEN);
	}

	private String buildToken(AuthenticatedUser authenticatedUser, String tokenType, Instant expiresAt) {
		return Jwts.builder()
				.issuer(authProperties.getIssuer())
				.subject(authenticatedUser.username())
				.claim(TOKEN_TYPE_CLAIM, tokenType)
				.claim(USER_ID_CLAIM, String.valueOf(authenticatedUser.userId()))
				.claim(DISPLAY_NAME_CLAIM, authenticatedUser.displayName())
				.claim(PHONE_CLAIM, authenticatedUser.phone())
				.claim(ROLES_CLAIM, authenticatedUser.roles())
				.issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(expiresAt))
				.signWith(secretKey)
				.compact();
	}

	private AuthenticatedUser parseToken(String token, String expectedTokenType, CommonErrorCode invalidTokenErrorCode) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();

			String actualTokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
			if (!expectedTokenType.equals(actualTokenType)) {
				throw new BusinessException(invalidTokenErrorCode);
			}

			List<String> roles = claims.get(ROLES_CLAIM, List.class);
			String userId = claims.get(USER_ID_CLAIM, String.class);

			return new AuthenticatedUser(
					Long.valueOf(userId),
					claims.getSubject(),
					claims.get(DISPLAY_NAME_CLAIM, String.class),
					claims.get(PHONE_CLAIM, String.class),
					roles == null ? List.of() : roles.stream().map(String::valueOf).toList());
		}
		catch (BusinessException exception) {
			throw exception;
		}
		catch (JwtException | IllegalArgumentException exception) {
			throw new BusinessException(invalidTokenErrorCode);
		}
	}

	public AuthUserResponse toUserResponse(AuthenticatedUser authenticatedUser) {
		return new AuthUserResponse(
				authenticatedUser.userId(),
				authenticatedUser.username(),
				authenticatedUser.displayName(),
				authenticatedUser.phone(),
				authenticatedUser.roles());
	}
}
