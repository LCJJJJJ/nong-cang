package com.nongcang.server.modules.auth.service;

import java.util.List;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.config.AuthProperties;
import com.nongcang.server.modules.auth.domain.AuthenticatedUser;
import com.nongcang.server.modules.auth.domain.dto.LoginRequest;
import com.nongcang.server.modules.auth.domain.dto.RefreshTokenRequest;
import com.nongcang.server.modules.auth.domain.vo.AuthTokenResponse;
import com.nongcang.server.modules.auth.domain.vo.AuthUserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final AuthProperties authProperties;

	private final JwtTokenService jwtTokenService;

	public AuthService(AuthProperties authProperties, JwtTokenService jwtTokenService) {
		this.authProperties = authProperties;
		this.jwtTokenService = jwtTokenService;
	}

	public AuthTokenResponse login(LoginRequest loginRequest) {
		if (!matchesAccount(loginRequest.account()) || !matchesPassword(loginRequest.password())) {
			throw new BusinessException(CommonErrorCode.INVALID_CREDENTIALS);
		}

		return jwtTokenService.issueTokens(buildDemoUser());
	}

	public AuthTokenResponse refresh(RefreshTokenRequest refreshTokenRequest) {
		AuthenticatedUser authenticatedUser = jwtTokenService.parseRefreshToken(refreshTokenRequest.refreshToken());
		return jwtTokenService.issueTokens(authenticatedUser);
	}

	public AuthUserResponse currentUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
			throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
		}

		return jwtTokenService.toUserResponse(authenticatedUser);
	}

	private boolean matchesAccount(String account) {
		return authProperties.getAdminUsername().equals(account) || authProperties.getAdminPhone().equals(account);
	}

	private boolean matchesPassword(String password) {
		return authProperties.getAdminPassword().equals(password);
	}

	private AuthenticatedUser buildDemoUser() {
		return new AuthenticatedUser(
				authProperties.getAdminUserId(),
				authProperties.getAdminUsername(),
				authProperties.getAdminDisplayName(),
				authProperties.getAdminPhone(),
				List.of("ADMIN"));
	}
}
