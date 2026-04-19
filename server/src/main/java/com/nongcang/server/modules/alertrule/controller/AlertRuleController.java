package com.nongcang.server.modules.alertrule.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.alertrule.domain.dto.AlertRuleListQueryRequest;
import com.nongcang.server.modules.alertrule.domain.dto.AlertRuleStatusUpdateRequest;
import com.nongcang.server.modules.alertrule.domain.dto.AlertRuleUpdateRequest;
import com.nongcang.server.modules.alertrule.domain.vo.AlertRuleDetailResponse;
import com.nongcang.server.modules.alertrule.domain.vo.AlertRuleListItemResponse;
import com.nongcang.server.modules.alertrule.service.AlertRuleService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/alert-rule")
public class AlertRuleController {

	private final AlertRuleService alertRuleService;

	public AlertRuleController(AlertRuleService alertRuleService) {
		this.alertRuleService = alertRuleService;
	}

	@GetMapping("/list")
	public ApiResponse<List<AlertRuleListItemResponse>> getAlertRuleList(
			@Valid @ModelAttribute AlertRuleListQueryRequest queryRequest) {
		return ApiResponse.success(alertRuleService.getAlertRuleList(queryRequest));
	}

	@GetMapping("/{id}")
	public ApiResponse<AlertRuleDetailResponse> getAlertRuleDetail(@PathVariable Long id) {
		return ApiResponse.success(alertRuleService.getAlertRuleDetail(id));
	}

	@PutMapping("/{id}")
	public ApiResponse<AlertRuleDetailResponse> updateAlertRule(
			@PathVariable Long id,
			@Valid @RequestBody AlertRuleUpdateRequest request) {
		return ApiResponse.success("更新成功", alertRuleService.updateAlertRule(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateAlertRuleStatus(
			@PathVariable Long id,
			@Valid @RequestBody AlertRuleStatusUpdateRequest request) {
		alertRuleService.updateAlertRuleStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}
}
