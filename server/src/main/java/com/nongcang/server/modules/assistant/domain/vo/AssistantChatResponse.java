package com.nongcang.server.modules.assistant.domain.vo;

public record AssistantChatResponse(
		AssistantSessionListItemResponse session,
		AssistantMessageResponse userMessage,
		AssistantMessageResponse assistantMessage) {
}
