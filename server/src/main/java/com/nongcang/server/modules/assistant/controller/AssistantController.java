package com.nongcang.server.modules.assistant.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.assistant.domain.dto.AssistantChatRequest;
import com.nongcang.server.modules.assistant.domain.vo.AssistantChatResponse;
import com.nongcang.server.modules.assistant.domain.vo.AssistantMessageResponse;
import com.nongcang.server.modules.assistant.domain.vo.AssistantSessionListItemResponse;
import com.nongcang.server.modules.assistant.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

	private final AssistantService assistantService;

	public AssistantController(AssistantService assistantService) {
		this.assistantService = assistantService;
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
}
