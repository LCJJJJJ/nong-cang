package com.nongcang.server.modules.lossrecord.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.lossrecord.domain.dto.LossRecordDirectCreateRequest;
import com.nongcang.server.modules.lossrecord.domain.dto.LossRecordListQueryRequest;
import com.nongcang.server.modules.lossrecord.domain.vo.LossRecordDetailResponse;
import com.nongcang.server.modules.lossrecord.domain.vo.LossRecordListItemResponse;
import com.nongcang.server.modules.lossrecord.service.LossRecordService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/loss-record")
public class LossRecordController {

	private final LossRecordService lossRecordService;

	public LossRecordController(LossRecordService lossRecordService) {
		this.lossRecordService = lossRecordService;
	}

	@GetMapping("/list")
	public ApiResponse<List<LossRecordListItemResponse>> getLossRecordList(
			@Valid @ModelAttribute LossRecordListQueryRequest queryRequest) {
		return ApiResponse.success(lossRecordService.getLossRecordList(queryRequest));
	}

	@PostMapping("/direct")
	public ApiResponse<LossRecordDetailResponse> createDirect(
			@Valid @RequestBody LossRecordDirectCreateRequest request) {
		return ApiResponse.success("新增成功", lossRecordService.createDirect(request));
	}
}
