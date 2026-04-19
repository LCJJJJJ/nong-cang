package com.nongcang.server.modules.systemuser.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemUserResetPasswordRequest(
		@NotBlank(message = "新密码不能为空")
		@Size(min = 8, max = 64, message = "新密码长度必须在8到64个字符之间")
		String newPassword) {
}
