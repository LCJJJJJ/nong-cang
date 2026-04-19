package com.nongcang.server.modules.inventorysupport.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.nongcang.server.modules.inventorysupport.repository.InventoryStockRepository;
import com.nongcang.server.modules.inventorysupport.repository.InventoryTransactionRepository;
import com.nongcang.server.modules.putawaytask.domain.entity.PutawayTaskEntity;
import org.springframework.stereotype.Service;

@Service
public class InventoryStockService {

	private static final DateTimeFormatter TRANSACTION_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final InventoryStockRepository inventoryStockRepository;
	private final InventoryTransactionRepository inventoryTransactionRepository;

	public InventoryStockService(
			InventoryStockRepository inventoryStockRepository,
			InventoryTransactionRepository inventoryTransactionRepository) {
		this.inventoryStockRepository = inventoryStockRepository;
		this.inventoryTransactionRepository = inventoryTransactionRepository;
	}

	public void recordInbound(PutawayTaskEntity task) {
		inventoryStockRepository.increaseStock(
				task.productId(),
				task.warehouseId(),
				task.zoneId(),
				task.locationId(),
				task.quantity());

		inventoryTransactionRepository.insertTransaction(
				generateTransactionCode(),
				task.productId(),
				task.warehouseId(),
				task.zoneId(),
				task.locationId(),
				task.quantity(),
				"PUTAWAY_TASK",
				task.id(),
				LocalDateTime.now(),
				task.remarks());
	}

	private String generateTransactionCode() {
		return "INVTX-" + LocalDateTime.now().format(TRANSACTION_CODE_FORMATTER);
	}
}
