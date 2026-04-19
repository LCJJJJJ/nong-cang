package com.nongcang.server.modules.assistant.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantChatRequest(
		Long sessionId,
		@NotBlank(message = "消息内容不能为空")
		@Size(max = 4000, message = "消息内容长度不能超过4000个字符")
		String message,
		@Size(max = 128, message = "路由路径长度不能超过128个字符")
		String routePath,
		@Size(max = 128, message = "页面标题长度不能超过128个字符")
		String routeTitle) {
}
