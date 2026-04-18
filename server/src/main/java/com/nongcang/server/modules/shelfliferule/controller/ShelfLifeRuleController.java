package com.nongcang.server.modules.shelfliferule.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.shelfliferule.domain.dto.ShelfLifeRuleCreateRequest;
import com.nongcang.server.modules.shelfliferule.domain.dto.ShelfLifeRuleListQueryRequest;
import com.nongcang.server.modules.shelfliferule.domain.dto.ShelfLifeRuleStatusUpdateRequest;
import com.nongcang.server.modules.shelfliferule.domain.dto.ShelfLifeRuleUpdateRequest;
import com.nongcang.server.modules.shelfliferule.domain.vo.ShelfLifeRuleDetailResponse;
import com.nongcang.server.modules.shelfliferule.domain.vo.ShelfLifeRuleListItemResponse;
import com.nongcang.server.modules.shelfliferule.domain.vo.ShelfLifeRuleOptionResponse;
import com.nongcang.server.modules.shelfliferule.service.ShelfLifeRuleService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/shelf-life-rule")
public class ShelfLifeRuleController {

	private final ShelfLifeRuleService shelfLifeRuleService;

	public ShelfLifeRuleController(ShelfLifeRuleService shelfLifeRuleService) {
		this.shelfLifeRuleService = shelfLifeRuleService;
	}

	@GetMapping("/list")
	public ApiResponse<List<ShelfLifeRuleListItemResponse>> getShelfLifeRuleList(
			@Valid @ModelAttribute ShelfLifeRuleListQueryRequest queryRequest) {
		return ApiResponse.success(shelfLifeRuleService.getShelfLifeRuleList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<ShelfLifeRuleOptionResponse>> getShelfLifeRuleOptions() {
		return ApiResponse.success(shelfLifeRuleService.getShelfLifeRuleOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<ShelfLifeRuleDetailResponse> getShelfLifeRuleDetail(@PathVariable Long id) {
		return ApiResponse.success(shelfLifeRuleService.getShelfLifeRuleDetail(id));
	}

	@PostMapping
	public ApiResponse<ShelfLifeRuleDetailResponse> createShelfLifeRule(
			@Valid @RequestBody ShelfLifeRuleCreateRequest request) {
		return ApiResponse.success("新增成功", shelfLifeRuleService.createShelfLifeRule(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<ShelfLifeRuleDetailResponse> updateShelfLifeRule(
			@PathVariable Long id,
			@Valid @RequestBody ShelfLifeRuleUpdateRequest request) {
		return ApiResponse.success("更新成功", shelfLifeRuleService.updateShelfLifeRule(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateShelfLifeRuleStatus(
			@PathVariable Long id,
			@Valid @RequestBody ShelfLifeRuleStatusUpdateRequest request) {
		shelfLifeRuleService.updateShelfLifeRuleStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteShelfLifeRule(@PathVariable Long id) {
		shelfLifeRuleService.deleteShelfLifeRule(id);
		return ApiResponse.success("删除成功", null);
	}
}
