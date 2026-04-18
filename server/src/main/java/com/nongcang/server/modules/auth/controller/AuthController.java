package com.nongcang.server.modules.auth.controller;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.auth.domain.dto.LoginRequest;
import com.nongcang.server.modules.auth.domain.dto.RefreshTokenRequest;
import com.nongcang.server.modules.auth.domain.vo.AuthTokenResponse;
import com.nongcang.server.modules.auth.domain.vo.AuthUserResponse;
import com.nongcang.server.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
		return ApiResponse.success("登录成功", authService.login(loginRequest));
	}

	@PostMapping("/refresh")
	public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
		return ApiResponse.success("刷新成功", authService.refresh(refreshTokenRequest));
	}

	@GetMapping("/me")
	public ApiResponse<AuthUserResponse> me(Authentication authentication) {
		return ApiResponse.success(authService.currentUser(authentication));
	}
}
