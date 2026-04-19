package com.nongcang.server.modules.assistant.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.config.AssistantProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class DeepSeekAssistantClient implements AssistantLlmClient {

	private final RestClient assistantRestClient;
	private final AssistantProperties assistantProperties;

	public DeepSeekAssistantClient(RestClient assistantRestClient, AssistantProperties assistantProperties) {
		this.assistantRestClient = assistantRestClient;
		this.assistantProperties = assistantProperties;
	}

	@Override
	public AssistantLlmResponse chat(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
		if (!assistantProperties.isEnabled()
				|| !StringUtils.hasText(assistantProperties.getDeepseek().getApiKey())) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_NOT_CONFIGURED);
		}

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("model", assistantProperties.getDeepseek().getModel());
		payload.put("messages", messages);
		payload.put("tools", tools);
		payload.put("temperature", 0.2);
		payload.put("stream", false);

		try {
			JsonNode response = assistantRestClient.post()
					.uri("/chat/completions")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.headers(headers -> headers.setBearerAuth(assistantProperties.getDeepseek().getApiKey()))
					.body(payload)
					.retrieve()
					.body(JsonNode.class);
			return parseResponse(response);
		} catch (RestClientException exception) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_PROVIDER_ERROR);
		}
	}

	private AssistantLlmResponse parseResponse(JsonNode response) {
		JsonNode messageNode = response.path("choices").path(0).path("message");
		String content = messageNode.path("content").isMissingNode() || messageNode.path("content").isNull()
				? ""
				: messageNode.path("content").asText("");

		List<AssistantToolCall> toolCalls = new ArrayList<>();
		for (JsonNode toolCallNode : messageNode.path("tool_calls")) {
			String id = toolCallNode.path("id").asText("");
			String name = toolCallNode.path("function").path("name").asText("");
			String argumentsJson = toolCallNode.path("function").path("arguments").asText("{}");
			if (StringUtils.hasText(name)) {
				toolCalls.add(new AssistantToolCall(id, name, argumentsJson));
			}
		}

		return new AssistantLlmResponse(content, toolCalls);
	}
}
