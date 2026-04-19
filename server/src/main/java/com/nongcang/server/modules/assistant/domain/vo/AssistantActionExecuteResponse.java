package com.nongcang.server.modules.assistant.domain.vo;

public record AssistantActionExecuteResponse(
		AssistantSessionListItemResponse session,
		AssistantMessageResponse assistantMessage) {
}
