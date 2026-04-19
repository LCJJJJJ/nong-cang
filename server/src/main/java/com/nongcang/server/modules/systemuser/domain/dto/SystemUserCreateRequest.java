package com.nongcang.server.modules.systemuser.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SystemUserCreateRequest(
		@NotBlank(message = "登录账号不能为空")
		@Size(max = 64, message = "登录账号长度不能超过64个字符")
		String username,
		@NotBlank(message = "姓名不能为空")
		@Size(max = 64, message = "姓名长度不能超过64个字符")
		String displayName,
		@NotBlank(message = "手机号不能为空")
		@Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
		String phone,
		@NotBlank(message = "角色不能为空")
		String roleCode,
		Long warehouseId,
		@NotNull(message = "状态不能为空")
		@Min(value = 0, message = "状态值不正确")
		@Max(value = 1, message = "状态值不正确")
		Integer status,
		@NotBlank(message = "初始密码不能为空")
		@Size(min = 8, max = 64, message = "初始密码长度必须在8到64个字符之间")
		String initialPassword,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
