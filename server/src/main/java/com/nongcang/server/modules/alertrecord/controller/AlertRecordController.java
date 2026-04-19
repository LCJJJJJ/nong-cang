package com.nongcang.server.modules.alertrecord.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.alertrecord.domain.dto.AlertRecordListQueryRequest;
import com.nongcang.server.modules.alertrecord.domain.vo.AlertRecordListItemResponse;
import com.nongcang.server.modules.alertrecord.domain.vo.AlertRefreshResponse;
import com.nongcang.server.modules.alertrecord.service.AlertRecordService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/alert-record")
public class AlertRecordController {

	private final AlertRecordService alertRecordService;

	public AlertRecordController(AlertRecordService alertRecordService) {
		this.alertRecordService = alertRecordService;
	}

	@GetMapping("/list")
	public ApiResponse<List<AlertRecordListItemResponse>> getAlertRecordList(
			@Valid @ModelAttribute AlertRecordListQueryRequest queryRequest) {
		return ApiResponse.success(alertRecordService.getAlertRecordList(queryRequest));
	}

	@PostMapping("/refresh")
	public ApiResponse<AlertRefreshResponse> refreshAlertRecords() {
		return ApiResponse.success("刷新成功", alertRecordService.refreshAlertRecords());
	}

	@PatchMapping("/{id}/ignore")
	public ApiResponse<Void> ignore(@PathVariable Long id) {
		alertRecordService.ignore(id);
		return ApiResponse.success("已忽略", null);
	}
}
