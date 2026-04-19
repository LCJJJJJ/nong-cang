package com.nongcang.server.modules.assistant.service;

import com.nongcang.server.modules.assistant.domain.vo.AssistantChatResponse;
import com.nongcang.server.modules.assistant.domain.vo.AssistantSessionListItemResponse;

public interface AssistantStreamListener {

	default void onSession(AssistantSessionListItemResponse session) {
	}

	default void onStatus(String message) {
	}

	default void onDelta(String content) {
	}

	default void onDone(AssistantChatResponse response) {
	}

	default void onError(Exception exception) {
	}
}
