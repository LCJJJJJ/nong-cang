package com.nongcang.server.modules.assistant.service;

import java.util.List;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.inventoryadjustment.domain.dto.InventoryAdjustmentListQueryRequest;
import com.nongcang.server.modules.inventoryadjustment.service.InventoryAdjustmentService;
import com.nongcang.server.modules.inventorystock.domain.dto.InventoryStockListQueryRequest;
import com.nongcang.server.modules.inventorystock.service.InventoryStockQueryService;
import com.nongcang.server.modules.inventorystocktaking.domain.dto.InventoryStocktakingListQueryRequest;
import com.nongcang.server.modules.inventorystocktaking.service.InventoryStocktakingService;
import com.nongcang.server.modules.inventorytransaction.domain.dto.InventoryTransactionListQueryRequest;
import com.nongcang.server.modules.inventorytransaction.service.InventoryTransactionQueryService;
import org.springframework.stereotype.Service;

@Service
public class AssistantInventoryQueryService {

	private final InventoryStockQueryService inventoryStockQueryService;
	private final InventoryTransactionQueryService inventoryTransactionQueryService;
	private final InventoryAdjustmentService inventoryAdjustmentService;
	private final InventoryStocktakingService inventoryStocktakingService;
	private final AssistantQuerySupport assistantQuerySupport;

	public AssistantInventoryQueryService(
			InventoryStockQueryService inventoryStockQueryService,
			InventoryTransactionQueryService inventoryTransactionQueryService,
			InventoryAdjustmentService inventoryAdjustmentService,
			InventoryStocktakingService inventoryStocktakingService,
			AssistantQuerySupport assistantQuerySupport) {
		this.inventoryStockQueryService = inventoryStockQueryService;
		this.inventoryTransactionQueryService = inventoryTransactionQueryService;
		this.inventoryAdjustmentService = inventoryAdjustmentService;
		this.inventoryStocktakingService = inventoryStocktakingService;
		this.assistantQuerySupport = assistantQuerySupport;
	}

	public AssistantToolExecutionResult execute(AssistantToolArguments arguments, int maxRows) {
		int limit = arguments.resolveLimit(10, maxRows);
		return switch (normalizeEntityType(arguments.entityType())) {
			case "stock" -> createResult(
					"实时库存查询",
					"/inventory-stocks",
					"实时库存查询",
					assistantQuerySupport.filterItems(
							inventoryStockQueryService.getInventoryStockList(new InventoryStockListQueryRequest(null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("productCode", "productName", "warehouseName", "zoneName", "locationName",
							"stockQuantity", "reservedQuantity", "lockedQuantity", "availableQuantity"),
					limit);
			case "transaction" -> createResult(
					"库存流水查询",
					"/inventory-transactions",
					"库存流水",
					assistantQuerySupport.filterItems(
							inventoryTransactionQueryService
									.getInventoryTransactionList(new InventoryTransactionListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("transactionCode", "transactionType", "warehouseName", "zoneName", "locationName",
							"productName", "quantity", "occurredAt"),
					limit);
			case "adjustment" -> createResult(
					"库存调整查询",
					"/inventory-adjustments",
					"库存调整管理",
					assistantQuerySupport.filterItems(
							inventoryAdjustmentService.getInventoryAdjustmentList(
									new InventoryAdjustmentListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("adjustmentCode", "adjustmentType", "warehouseName", "zoneName", "locationName",
							"productName", "quantity", "reason", "createdAt"),
					limit);
			case "stocktaking" -> createResult(
					"库存盘点查询",
					"/inventory-stocktakings",
					"库存盘点管理",
					assistantQuerySupport.filterItems(
							inventoryStocktakingService
									.getInventoryStocktakingList(new InventoryStocktakingListQueryRequest(null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("stocktakingCode", "warehouseName", "itemCount", "countedItemCount", "totalDifferenceQuantity",
							"statusLabel", "updatedAt"),
					limit);
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "库存查询实体不正确");
		};
	}

	private AssistantToolExecutionResult createResult(
			String title,
			String routePath,
			String routeLabel,
			List<?> items,
			List<String> preferredKeys,
			int limit) {
		AssistantResultBlock block = assistantQuerySupport.toTableBlock(
				title,
				routePath,
				routeLabel,
				items,
				preferredKeys,
				limit,
				title + "：");
		return new AssistantToolExecutionResult("query_inventory_data", block.summary(), List.of(block));
	}

	private String normalizeEntityType(String entityType) {
		return entityType == null ? "" : entityType.trim().toLowerCase();
	}
}
