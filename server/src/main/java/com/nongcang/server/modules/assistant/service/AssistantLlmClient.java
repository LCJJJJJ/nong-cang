package com.nongcang.server.modules.assistant.service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface AssistantLlmClient {

	AssistantLlmResponse chat(List<Map<String, Object>> messages, List<Map<String, Object>> tools);

	void streamChat(List<Map<String, Object>> messages, List<Map<String, Object>> tools, Consumer<String> deltaConsumer);
}
