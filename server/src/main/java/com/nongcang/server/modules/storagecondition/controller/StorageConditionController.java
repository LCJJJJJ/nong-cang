package com.nongcang.server.modules.storagecondition.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionCreateRequest;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionListQueryRequest;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionStatusUpdateRequest;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionUpdateRequest;
import com.nongcang.server.modules.storagecondition.domain.vo.StorageConditionDetailResponse;
import com.nongcang.server.modules.storagecondition.domain.vo.StorageConditionListItemResponse;
import com.nongcang.server.modules.storagecondition.domain.vo.StorageConditionOptionResponse;
import com.nongcang.server.modules.storagecondition.service.StorageConditionService;
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
@RequestMapping("/api/storage-condition")
public class StorageConditionController {

	private final StorageConditionService storageConditionService;

	public StorageConditionController(StorageConditionService storageConditionService) {
		this.storageConditionService = storageConditionService;
	}

	@GetMapping("/list")
	public ApiResponse<List<StorageConditionListItemResponse>> getStorageConditionList(
			@Valid @ModelAttribute StorageConditionListQueryRequest queryRequest) {
		return ApiResponse.success(storageConditionService.getStorageConditionList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<StorageConditionOptionResponse>> getStorageConditionOptions() {
		return ApiResponse.success(storageConditionService.getStorageConditionOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<StorageConditionDetailResponse> getStorageConditionDetail(@PathVariable Long id) {
		return ApiResponse.success(storageConditionService.getStorageConditionDetail(id));
	}

	@PostMapping
	public ApiResponse<StorageConditionDetailResponse> createStorageCondition(
			@Valid @RequestBody StorageConditionCreateRequest request) {
		return ApiResponse.success("新增成功", storageConditionService.createStorageCondition(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<StorageConditionDetailResponse> updateStorageCondition(
			@PathVariable Long id,
			@Valid @RequestBody StorageConditionUpdateRequest request) {
		return ApiResponse.success("更新成功", storageConditionService.updateStorageCondition(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateStorageConditionStatus(
			@PathVariable Long id,
			@Valid @RequestBody StorageConditionStatusUpdateRequest request) {
		storageConditionService.updateStorageConditionStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteStorageCondition(@PathVariable Long id) {
		storageConditionService.deleteStorageCondition(id);
		return ApiResponse.success("删除成功", null);
	}
}
