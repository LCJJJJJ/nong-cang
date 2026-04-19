package com.nongcang.server.modules.inventorystocktaking.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.inventorystocktaking.domain.dto.InventoryStocktakingCreateRequest;
import com.nongcang.server.modules.inventorystocktaking.domain.dto.InventoryStocktakingItemSaveBatchRequest;
import com.nongcang.server.modules.inventorystocktaking.domain.dto.InventoryStocktakingListQueryRequest;
import com.nongcang.server.modules.inventorystocktaking.domain.vo.InventoryStocktakingDetailResponse;
import com.nongcang.server.modules.inventorystocktaking.domain.vo.InventoryStocktakingListItemResponse;
import com.nongcang.server.modules.inventorystocktaking.service.InventoryStocktakingService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/inventory-stocktaking")
public class InventoryStocktakingController {

	private final InventoryStocktakingService inventoryStocktakingService;

	public InventoryStocktakingController(InventoryStocktakingService inventoryStocktakingService) {
		this.inventoryStocktakingService = inventoryStocktakingService;
	}

	@GetMapping("/list")
	public ApiResponse<List<InventoryStocktakingListItemResponse>> getInventoryStocktakingList(
			@Valid @ModelAttribute InventoryStocktakingListQueryRequest queryRequest) {
		return ApiResponse.success(inventoryStocktakingService.getInventoryStocktakingList(queryRequest));
	}

	@GetMapping("/{id}")
	public ApiResponse<InventoryStocktakingDetailResponse> getInventoryStocktakingDetail(@PathVariable Long id) {
		return ApiResponse.success(inventoryStocktakingService.getInventoryStocktakingDetail(id));
	}

	@PostMapping
	public ApiResponse<InventoryStocktakingDetailResponse> createInventoryStocktaking(
			@Valid @RequestBody InventoryStocktakingCreateRequest request) {
		return ApiResponse.success("新增成功", inventoryStocktakingService.createInventoryStocktaking(request));
	}

	@PutMapping("/{id}/items")
	public ApiResponse<InventoryStocktakingDetailResponse> saveItems(
			@PathVariable Long id,
			@Valid @RequestBody InventoryStocktakingItemSaveBatchRequest request) {
		return ApiResponse.success("盘点结果已保存", inventoryStocktakingService.saveItems(id, request));
	}

	@PatchMapping("/{id}/confirm")
	public ApiResponse<Void> confirm(@PathVariable Long id) {
		inventoryStocktakingService.confirm(id);
		return ApiResponse.success("盘点已确认", null);
	}

	@PatchMapping("/{id}/cancel")
	public ApiResponse<Void> cancel(@PathVariable Long id) {
		inventoryStocktakingService.cancel(id);
		return ApiResponse.success("盘点单已取消", null);
	}
}
