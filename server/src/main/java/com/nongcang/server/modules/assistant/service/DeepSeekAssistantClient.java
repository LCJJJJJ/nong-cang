package com.nongcang.server.modules.assistant.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	public DeepSeekAssistantClient(
			RestClient assistantRestClient,
			AssistantProperties assistantProperties,
			ObjectMapper objectMapper) {
		this.assistantRestClient = assistantRestClient;
		this.assistantProperties = assistantProperties;
		this.objectMapper = objectMapper;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(assistantProperties.getDeepseek().getConnectTimeoutMillis()))
				.build();
	}

	@Override
	public AssistantLlmResponse chat(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
		validateConfigured();

		Map<String, Object> payload = buildPayload(messages, tools, false);

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

	@Override
	public void streamChat(
			List<Map<String, Object>> messages,
			List<Map<String, Object>> tools,
			Consumer<String> deltaConsumer) {
		validateConfigured();

		try {
			String requestBody = objectMapper.writeValueAsString(buildPayload(messages, tools, true));
			HttpRequest request = HttpRequest.newBuilder()
					.uri(resolveStreamUri())
					.timeout(Duration.ofMillis(assistantProperties.getDeepseek().getReadTimeoutMillis()))
					.header("Authorization", "Bearer " + assistantProperties.getDeepseek().getApiKey())
					.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
					.header("Accept", MediaType.TEXT_EVENT_STREAM_VALUE)
					.POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
					.build();

			HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
			if (response.statusCode() >= 400) {
				throw new BusinessException(CommonErrorCode.ASSISTANT_PROVIDER_ERROR);
			}

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (!line.startsWith("data: ")) {
						continue;
					}

					String payload = line.substring(6).trim();
					if ("[DONE]".equals(payload)) {
						break;
					}

					JsonNode chunkNode = objectMapper.readTree(payload);
					for (JsonNode choiceNode : chunkNode.path("choices")) {
						JsonNode deltaNode = choiceNode.path("delta");
						if (deltaNode.has("content") && !deltaNode.path("content").isNull()) {
							String content = deltaNode.path("content").asText("");
							if (!content.isEmpty()) {
								deltaConsumer.accept(content);
							}
						}
					}
				}
			}
		} catch (BusinessException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_PROVIDER_ERROR);
		}
	}

	private void validateConfigured() {
		if (!assistantProperties.isEnabled()
				|| !StringUtils.hasText(assistantProperties.getDeepseek().getApiKey())) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_NOT_CONFIGURED);
		}
	}

	private Map<String, Object> buildPayload(
			List<Map<String, Object>> messages,
			List<Map<String, Object>> tools,
			boolean stream) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("model", assistantProperties.getDeepseek().getModel());
		payload.put("messages", messages);
		if (tools != null && !tools.isEmpty()) {
			payload.put("tools", tools);
		}
		payload.put("temperature", 0.2);
		payload.put("stream", stream);
		if (stream) {
			payload.put("stream_options", Map.of("include_usage", false));
		}
		return payload;
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

	private URI resolveStreamUri() {
		String baseUrl = assistantProperties.getDeepseek().getBaseUrl();
		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return URI.create(baseUrl + "/chat/completions");
	}
}
