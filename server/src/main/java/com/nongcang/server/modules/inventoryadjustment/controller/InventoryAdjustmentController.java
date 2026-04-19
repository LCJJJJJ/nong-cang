package com.nongcang.server.modules.inventoryadjustment.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.inventoryadjustment.domain.dto.InventoryAdjustmentCreateRequest;
import com.nongcang.server.modules.inventoryadjustment.domain.dto.InventoryAdjustmentListQueryRequest;
import com.nongcang.server.modules.inventoryadjustment.domain.vo.InventoryAdjustmentDetailResponse;
import com.nongcang.server.modules.inventoryadjustment.domain.vo.InventoryAdjustmentListItemResponse;
import com.nongcang.server.modules.inventoryadjustment.service.InventoryAdjustmentService;
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
@RequestMapping("/api/inventory-adjustment")
public class InventoryAdjustmentController {

	private final InventoryAdjustmentService inventoryAdjustmentService;

	public InventoryAdjustmentController(InventoryAdjustmentService inventoryAdjustmentService) {
		this.inventoryAdjustmentService = inventoryAdjustmentService;
	}

	@GetMapping("/list")
	public ApiResponse<List<InventoryAdjustmentListItemResponse>> getInventoryAdjustmentList(
			@Valid @ModelAttribute InventoryAdjustmentListQueryRequest queryRequest) {
		return ApiResponse.success(inventoryAdjustmentService.getInventoryAdjustmentList(queryRequest));
	}

	@PostMapping
	public ApiResponse<InventoryAdjustmentDetailResponse> createInventoryAdjustment(
			@Valid @RequestBody InventoryAdjustmentCreateRequest request) {
		return ApiResponse.success("新增成功", inventoryAdjustmentService.createInventoryAdjustment(request));
	}
}
