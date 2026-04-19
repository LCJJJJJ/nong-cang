package com.nongcang.server.modules.assistant.domain.dto;

import jakarta.validation.constraints.Size;

public record AssistantActionExecuteRequest(
		@Size(max = 16, message = "确认文本长度不能超过16个字符")
		String confirmationText) {
}
