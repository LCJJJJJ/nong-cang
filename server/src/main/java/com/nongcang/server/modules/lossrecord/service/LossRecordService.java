package com.nongcang.server.modules.lossrecord.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.abnormalstock.domain.entity.AbnormalStockEntity;
import com.nongcang.server.modules.inventorysupport.repository.InventoryStockRepository;
import com.nongcang.server.modules.inventorysupport.repository.InventoryTransactionRepository;
import com.nongcang.server.modules.lossrecord.repository.LossRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LossRecordService {

	private static final DateTimeFormatter LOSS_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final LossRecordRepository lossRecordRepository;
	private final InventoryStockRepository inventoryStockRepository;
	private final InventoryTransactionRepository inventoryTransactionRepository;

	public LossRecordService(
			LossRecordRepository lossRecordRepository,
			InventoryStockRepository inventoryStockRepository,
			InventoryTransactionRepository inventoryTransactionRepository) {
		this.lossRecordRepository = lossRecordRepository;
		this.inventoryStockRepository = inventoryStockRepository;
		this.inventoryTransactionRepository = inventoryTransactionRepository;
	}

	@Transactional
	public void createFromAbnormalStock(AbnormalStockEntity abnormalStock, String lossReason, String remarks) {
		boolean decreased = inventoryStockRepository.decreaseStock(
				abnormalStock.productId(),
				abnormalStock.warehouseId(),
				abnormalStock.zoneId(),
				abnormalStock.locationId(),
				abnormalStock.lockedQuantity());

		if (!decreased) {
			throw new BusinessException(CommonErrorCode.LOSS_RECORD_STOCK_INSUFFICIENT);
		}

		long lossRecordId = lossRecordRepository.insert(
				generateLossCode(),
				"ABNORMAL_STOCK",
				abnormalStock.id(),
				abnormalStock.productId(),
				abnormalStock.warehouseId(),
				abnormalStock.zoneId(),
				abnormalStock.locationId(),
				abnormalStock.lockedQuantity(),
				lossReason,
				remarks);

		inventoryTransactionRepository.insertTransaction(
				generateTransactionCode(),
				"LOSS",
				abnormalStock.productId(),
				abnormalStock.warehouseId(),
				abnormalStock.zoneId(),
				abnormalStock.locationId(),
				abnormalStock.lockedQuantity().negate(),
				"LOSS_RECORD",
				lossRecordId,
				LocalDateTime.now(),
				lossReason);
	}

	private String generateLossCode() {
		for (int index = 0; index < 20; index += 1) {
			String lossCode = "LOSS-" + LocalDateTime.now().format(LOSS_CODE_FORMATTER);

			if (index > 0) {
				lossCode += "-" + index;
			}

			if (!lossRecordRepository.existsByLossCode(lossCode)) {
				return lossCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "损耗记录编号生成失败，请稍后重试");
	}

	private String generateTransactionCode() {
		return "INVTX-" + LocalDateTime.now().format(LOSS_CODE_FORMATTER);
	}
}
