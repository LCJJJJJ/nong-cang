package com.nongcang.server.modules.inboundrecord.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.inboundrecord.domain.dto.InboundRecordListQueryRequest;
import com.nongcang.server.modules.inboundrecord.domain.vo.InboundRecordListItemResponse;
import com.nongcang.server.modules.inboundrecord.service.InboundRecordService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/inbound-record")
public class InboundRecordController {

	private final InboundRecordService inboundRecordService;

	public InboundRecordController(InboundRecordService inboundRecordService) {
		this.inboundRecordService = inboundRecordService;
	}

	@GetMapping("/list")
	public ApiResponse<List<InboundRecordListItemResponse>> getInboundRecordList(
			@Valid @ModelAttribute InboundRecordListQueryRequest queryRequest) {
		return ApiResponse.success(inboundRecordService.getInboundRecordList(queryRequest));
	}
}
