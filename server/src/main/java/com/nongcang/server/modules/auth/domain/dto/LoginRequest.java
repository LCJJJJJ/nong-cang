package com.nongcang.server.modules.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "账号不能为空")
		String account,
		@NotBlank(message = "密码不能为空")
		String password,
		Boolean rememberMe) {
}
