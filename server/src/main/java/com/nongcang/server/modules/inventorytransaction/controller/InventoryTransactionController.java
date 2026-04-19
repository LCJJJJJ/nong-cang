package com.nongcang.server.modules.inventorytransaction.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.inventorytransaction.domain.dto.InventoryTransactionListQueryRequest;
import com.nongcang.server.modules.inventorytransaction.domain.vo.InventoryTransactionListItemResponse;
import com.nongcang.server.modules.inventorytransaction.service.InventoryTransactionQueryService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/inventory-transaction")
public class InventoryTransactionController {

	private final InventoryTransactionQueryService inventoryTransactionQueryService;

	public InventoryTransactionController(InventoryTransactionQueryService inventoryTransactionQueryService) {
		this.inventoryTransactionQueryService = inventoryTransactionQueryService;
	}

	@GetMapping("/list")
	public ApiResponse<List<InventoryTransactionListItemResponse>> getInventoryTransactionList(
			@Valid @ModelAttribute InventoryTransactionListQueryRequest queryRequest) {
		return ApiResponse.success(inventoryTransactionQueryService.getInventoryTransactionList(queryRequest));
	}
}
