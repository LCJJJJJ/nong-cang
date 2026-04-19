package com.nongcang.server.modules.warehousezone.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.warehousezone.domain.dto.WarehouseZoneCreateRequest;
import com.nongcang.server.modules.warehousezone.domain.dto.WarehouseZoneListQueryRequest;
import com.nongcang.server.modules.warehousezone.domain.dto.WarehouseZoneStatusUpdateRequest;
import com.nongcang.server.modules.warehousezone.domain.dto.WarehouseZoneUpdateRequest;
import com.nongcang.server.modules.warehousezone.domain.vo.WarehouseZoneDetailResponse;
import com.nongcang.server.modules.warehousezone.domain.vo.WarehouseZoneListItemResponse;
import com.nongcang.server.modules.warehousezone.domain.vo.WarehouseZoneOptionResponse;
import com.nongcang.server.modules.warehousezone.service.WarehouseZoneService;
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
@RequestMapping("/api/warehouse-zone")
public class WarehouseZoneController {

	private final WarehouseZoneService warehouseZoneService;

	public WarehouseZoneController(WarehouseZoneService warehouseZoneService) {
		this.warehouseZoneService = warehouseZoneService;
	}

	@GetMapping("/list")
	public ApiResponse<List<WarehouseZoneListItemResponse>> getWarehouseZoneList(
			@Valid @ModelAttribute WarehouseZoneListQueryRequest queryRequest) {
		return ApiResponse.success(warehouseZoneService.getWarehouseZoneList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<WarehouseZoneOptionResponse>> getWarehouseZoneOptions() {
		return ApiResponse.success(warehouseZoneService.getWarehouseZoneOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<WarehouseZoneDetailResponse> getWarehouseZoneDetail(@PathVariable Long id) {
		return ApiResponse.success(warehouseZoneService.getWarehouseZoneDetail(id));
	}

	@PostMapping
	public ApiResponse<WarehouseZoneDetailResponse> createWarehouseZone(
			@Valid @RequestBody WarehouseZoneCreateRequest request) {
		return ApiResponse.success("新增成功", warehouseZoneService.createWarehouseZone(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<WarehouseZoneDetailResponse> updateWarehouseZone(
			@PathVariable Long id,
			@Valid @RequestBody WarehouseZoneUpdateRequest request) {
		return ApiResponse.success("更新成功", warehouseZoneService.updateWarehouseZone(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateWarehouseZoneStatus(
			@PathVariable Long id,
			@Valid @RequestBody WarehouseZoneStatusUpdateRequest request) {
		warehouseZoneService.updateWarehouseZoneStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteWarehouseZone(@PathVariable Long id) {
		warehouseZoneService.deleteWarehouseZone(id);
		return ApiResponse.success("删除成功", null);
	}
}
