package com.nongcang.server.modules.warehouse.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.warehouse.domain.dto.WarehouseCreateRequest;
import com.nongcang.server.modules.warehouse.domain.dto.WarehouseListQueryRequest;
import com.nongcang.server.modules.warehouse.domain.dto.WarehouseStatusUpdateRequest;
import com.nongcang.server.modules.warehouse.domain.dto.WarehouseUpdateRequest;
import com.nongcang.server.modules.warehouse.domain.vo.WarehouseDetailResponse;
import com.nongcang.server.modules.warehouse.domain.vo.WarehouseListItemResponse;
import com.nongcang.server.modules.warehouse.domain.vo.WarehouseOptionResponse;
import com.nongcang.server.modules.warehouse.service.WarehouseService;
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
@RequestMapping("/api/warehouse")
public class WarehouseController {

	private final WarehouseService warehouseService;

	public WarehouseController(WarehouseService warehouseService) {
		this.warehouseService = warehouseService;
	}

	@GetMapping("/list")
	public ApiResponse<List<WarehouseListItemResponse>> getWarehouseList(
			@Valid @ModelAttribute WarehouseListQueryRequest queryRequest) {
		return ApiResponse.success(warehouseService.getWarehouseList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<WarehouseOptionResponse>> getWarehouseOptions() {
		return ApiResponse.success(warehouseService.getWarehouseOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<WarehouseDetailResponse> getWarehouseDetail(@PathVariable Long id) {
		return ApiResponse.success(warehouseService.getWarehouseDetail(id));
	}

	@PostMapping
	public ApiResponse<WarehouseDetailResponse> createWarehouse(
			@Valid @RequestBody WarehouseCreateRequest request) {
		return ApiResponse.success("新增成功", warehouseService.createWarehouse(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<WarehouseDetailResponse> updateWarehouse(
			@PathVariable Long id,
			@Valid @RequestBody WarehouseUpdateRequest request) {
		return ApiResponse.success("更新成功", warehouseService.updateWarehouse(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateWarehouseStatus(
			@PathVariable Long id,
			@Valid @RequestBody WarehouseStatusUpdateRequest request) {
		warehouseService.updateWarehouseStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteWarehouse(@PathVariable Long id) {
		warehouseService.deleteWarehouse(id);
		return ApiResponse.success("删除成功", null);
	}
}
