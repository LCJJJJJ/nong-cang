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
import com.nongcang.server.modules.systemuser.domain.entity.SystemUserEntity;
import com.nongcang.server.modules.systemuser.repository.SystemUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final AuthProperties authProperties;

	private final JwtTokenService jwtTokenService;

	private final SystemUserRepository systemUserRepository;

	private final PasswordEncoder passwordEncoder;

	public AuthService(
			AuthProperties authProperties,
			JwtTokenService jwtTokenService,
			SystemUserRepository systemUserRepository,
			PasswordEncoder passwordEncoder) {
		this.authProperties = authProperties;
		this.jwtTokenService = jwtTokenService;
		this.systemUserRepository = systemUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public AuthTokenResponse login(LoginRequest loginRequest) {
		SystemUserEntity user = systemUserRepository.findByAccount(loginRequest.account())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_CREDENTIALS));

		if (user.status() != 1 || !passwordEncoder.matches(loginRequest.password(), user.passwordHash())) {
			throw new BusinessException(CommonErrorCode.INVALID_CREDENTIALS);
		}

		return jwtTokenService.issueTokens(toAuthenticatedUser(user));
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

	private AuthenticatedUser toAuthenticatedUser(SystemUserEntity user) {
		return new AuthenticatedUser(
				user.id(),
				user.username(),
				user.displayName(),
				user.phone(),
				user.roleCode(),
				user.warehouseId(),
				user.warehouseName(),
				List.of(user.roleCode()));
	}
}
