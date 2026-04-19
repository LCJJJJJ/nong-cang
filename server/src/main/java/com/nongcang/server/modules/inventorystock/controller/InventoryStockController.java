package com.nongcang.server.modules.inventorystock.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.inventorystock.domain.dto.InventoryStockListQueryRequest;
import com.nongcang.server.modules.inventorystock.domain.vo.InventoryStockListItemResponse;
import com.nongcang.server.modules.inventorystock.service.InventoryStockQueryService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/inventory-stock")
public class InventoryStockController {

	private final InventoryStockQueryService inventoryStockQueryService;

	public InventoryStockController(InventoryStockQueryService inventoryStockQueryService) {
		this.inventoryStockQueryService = inventoryStockQueryService;
	}

	@GetMapping("/list")
	public ApiResponse<List<InventoryStockListItemResponse>> getInventoryStockList(
			@Valid @ModelAttribute InventoryStockListQueryRequest queryRequest) {
		return ApiResponse.success(inventoryStockQueryService.getInventoryStockList(queryRequest));
	}
}
