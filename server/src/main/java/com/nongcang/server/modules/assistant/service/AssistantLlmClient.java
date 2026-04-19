package com.nongcang.server.modules.assistant.service;

import java.util.List;
import java.util.Map;

public interface AssistantLlmClient {

	AssistantLlmResponse chat(List<Map<String, Object>> messages, List<Map<String, Object>> tools);
}
