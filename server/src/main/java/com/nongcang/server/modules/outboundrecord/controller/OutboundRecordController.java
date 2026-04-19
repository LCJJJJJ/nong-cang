package com.nongcang.server.modules.outboundrecord.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.outboundrecord.domain.dto.OutboundRecordListQueryRequest;
import com.nongcang.server.modules.outboundrecord.domain.vo.OutboundRecordListItemResponse;
import com.nongcang.server.modules.outboundrecord.service.OutboundRecordService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/outbound-record")
public class OutboundRecordController {

	private final OutboundRecordService outboundRecordService;

	public OutboundRecordController(OutboundRecordService outboundRecordService) {
		this.outboundRecordService = outboundRecordService;
	}

	@GetMapping("/list")
	public ApiResponse<List<OutboundRecordListItemResponse>> getOutboundRecordList(
			@Valid @ModelAttribute OutboundRecordListQueryRequest queryRequest) {
		return ApiResponse.success(outboundRecordService.getOutboundRecordList(queryRequest));
	}
}
