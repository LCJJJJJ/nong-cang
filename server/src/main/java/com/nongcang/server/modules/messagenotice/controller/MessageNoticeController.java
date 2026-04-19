package com.nongcang.server.modules.messagenotice.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.messagenotice.domain.dto.MessageNoticeListQueryRequest;
import com.nongcang.server.modules.messagenotice.domain.vo.MessageNoticeListItemResponse;
import com.nongcang.server.modules.messagenotice.service.MessageNoticeService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/message-notice")
public class MessageNoticeController {

	private final MessageNoticeService messageNoticeService;

	public MessageNoticeController(MessageNoticeService messageNoticeService) {
		this.messageNoticeService = messageNoticeService;
	}

	@GetMapping("/list")
	public ApiResponse<List<MessageNoticeListItemResponse>> getMessageNoticeList(
			@Valid @ModelAttribute MessageNoticeListQueryRequest queryRequest) {
		return ApiResponse.success(messageNoticeService.getMessageNoticeList(queryRequest));
	}

	@PatchMapping("/{id}/read")
	public ApiResponse<Void> markRead(@PathVariable Long id) {
		messageNoticeService.markRead(id);
		return ApiResponse.success("已标记已读", null);
	}

	@PatchMapping("/read-all")
	public ApiResponse<Void> markAllRead() {
		messageNoticeService.markAllRead();
		return ApiResponse.success("全部消息已读", null);
	}
}
