package com.nongcang.server.modules.warehouselocation.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.warehouselocation.domain.dto.WarehouseLocationCreateRequest;
import com.nongcang.server.modules.warehouselocation.domain.dto.WarehouseLocationListQueryRequest;
import com.nongcang.server.modules.warehouselocation.domain.dto.WarehouseLocationStatusUpdateRequest;
import com.nongcang.server.modules.warehouselocation.domain.dto.WarehouseLocationUpdateRequest;
import com.nongcang.server.modules.warehouselocation.domain.vo.WarehouseLocationDetailResponse;
import com.nongcang.server.modules.warehouselocation.domain.vo.WarehouseLocationListItemResponse;
import com.nongcang.server.modules.warehouselocation.domain.vo.WarehouseLocationOptionResponse;
import com.nongcang.server.modules.warehouselocation.service.WarehouseLocationService;
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
@RequestMapping("/api/warehouse-location")
public class WarehouseLocationController {

	private final WarehouseLocationService warehouseLocationService;

	public WarehouseLocationController(WarehouseLocationService warehouseLocationService) {
		this.warehouseLocationService = warehouseLocationService;
	}

	@GetMapping("/list")
	public ApiResponse<List<WarehouseLocationListItemResponse>> getWarehouseLocationList(
			@Valid @ModelAttribute WarehouseLocationListQueryRequest queryRequest) {
		return ApiResponse.success(warehouseLocationService.getWarehouseLocationList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<WarehouseLocationOptionResponse>> getWarehouseLocationOptions() {
		return ApiResponse.success(warehouseLocationService.getWarehouseLocationOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<WarehouseLocationDetailResponse> getWarehouseLocationDetail(@PathVariable Long id) {
		return ApiResponse.success(warehouseLocationService.getWarehouseLocationDetail(id));
	}

	@PostMapping
	public ApiResponse<WarehouseLocationDetailResponse> createWarehouseLocation(
			@Valid @RequestBody WarehouseLocationCreateRequest request) {
		return ApiResponse.success("新增成功", warehouseLocationService.createWarehouseLocation(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<WarehouseLocationDetailResponse> updateWarehouseLocation(
			@PathVariable Long id,
			@Valid @RequestBody WarehouseLocationUpdateRequest request) {
		return ApiResponse.success("更新成功", warehouseLocationService.updateWarehouseLocation(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateWarehouseLocationStatus(
			@PathVariable Long id,
			@Valid @RequestBody WarehouseLocationStatusUpdateRequest request) {
		warehouseLocationService.updateWarehouseLocationStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteWarehouseLocation(@PathVariable Long id) {
		warehouseLocationService.deleteWarehouseLocation(id);
		return ApiResponse.success("删除成功", null);
	}
}
