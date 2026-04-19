package com.nongcang.server.modules.assistant.controller;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.assistant.domain.dto.AssistantActionExecuteRequest;
import com.nongcang.server.modules.assistant.domain.vo.AssistantActionExecuteResponse;
import com.nongcang.server.modules.assistant.domain.dto.AssistantChatRequest;
import com.nongcang.server.modules.assistant.domain.vo.AssistantChatResponse;
import com.nongcang.server.modules.assistant.domain.vo.AssistantMessageResponse;
import com.nongcang.server.modules.assistant.domain.vo.AssistantSessionListItemResponse;
import com.nongcang.server.modules.assistant.service.AssistantStreamListener;
import com.nongcang.server.modules.assistant.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

	private final AssistantService assistantService;
	private final ObjectMapper objectMapper;

	public AssistantController(AssistantService assistantService, ObjectMapper objectMapper) {
		this.assistantService = assistantService;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/sessions")
	public ApiResponse<List<AssistantSessionListItemResponse>> getSessions(Authentication authentication) {
		return ApiResponse.success("查询成功", assistantService.getSessions(authentication));
	}

	@GetMapping("/sessions/{id}/messages")
	public ApiResponse<List<AssistantMessageResponse>> getMessages(
			@PathVariable("id") Long id,
			Authentication authentication) {
		return ApiResponse.success("查询成功", assistantService.getMessages(id, authentication));
	}

	@PostMapping("/chat")
	public ApiResponse<AssistantChatResponse> chat(
			@Valid @RequestBody AssistantChatRequest request,
			Authentication authentication) {
		return ApiResponse.success("对话成功", assistantService.chat(request, authentication));
	}

	@PatchMapping("/action-plans/{actionCode}/execute")
	public ApiResponse<AssistantActionExecuteResponse> executeActionPlan(
			@PathVariable String actionCode,
			@RequestBody(required = false) AssistantActionExecuteRequest request,
			Authentication authentication) {
		return ApiResponse.success("执行成功", assistantService.executeActionPlan(actionCode, request, authentication));
	}

	@PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<StreamingResponseBody> chatStream(
			@Valid @RequestBody AssistantChatRequest request,
			Authentication authentication) {
		StreamingResponseBody responseBody = outputStream -> {
			OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

			try {
				assistantService.streamChat(request, authentication, new AssistantStreamListener() {
					@Override
					public void onSession(AssistantSessionListItemResponse session) {
						writeEvent(writer, "session", session);
					}

					@Override
					public void onStatus(String message) {
						writeEvent(writer, "status", new StreamMessage(message));
					}

					@Override
					public void onDelta(String content) {
						writeEvent(writer, "delta", new StreamMessage(content));
					}

					@Override
					public void onDone(AssistantChatResponse response) {
						writeEvent(writer, "done", response);
					}

					@Override
					public void onError(Exception exception) {
						writeEvent(writer, "error", new StreamMessage(exception.getMessage()));
					}
				});
			} finally {
				writer.flush();
			}
		};
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_EVENT_STREAM)
				.body(responseBody);
	}

	private synchronized void writeEvent(OutputStreamWriter writer, String eventName, Object payload) {
		try {
			writer.write("event: " + eventName + "\n");
			writer.write("data: " + objectMapper.writeValueAsString(payload) + "\n\n");
			writer.flush();
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to write assistant stream event", exception);
		}
	}

	private record StreamMessage(String message) {
	}
}
