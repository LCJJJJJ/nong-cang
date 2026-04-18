package com.nongcang.server.modules.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemEchoRequest(
		@NotBlank(message = "内容不能为空")
		@Size(max = 64, message = "内容长度不能超过64个字符")
		String content) {
}
