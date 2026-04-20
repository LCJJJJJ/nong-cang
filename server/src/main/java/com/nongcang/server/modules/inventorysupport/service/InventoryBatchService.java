package com.nongcang.server.modules.inventorysupport.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.nongcang.server.modules.abnormalstock.domain.entity.AbnormalStockBatchLockEntity;
import com.nongcang.server.modules.abnormalstock.repository.AbnormalStockBatchLockRepository;
import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.inboundrecord.domain.entity.InboundRecordEntity;
import com.nongcang.server.modules.inventorysupport.domain.entity.InventoryBatchEntity;
import com.nongcang.server.modules.inventorysupport.repository.InventoryBatchRepository;
import com.nongcang.server.modules.outboundtask.domain.entity.OutboundTaskBatchAllocationEntity;
import com.nongcang.server.modules.outboundtask.repository.OutboundTaskBatchAllocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryBatchService {

	private static final String STATUS_ACTIVE = "ACTIVE";
	private static final String STATUS_DEPLETED = "DEPLETED";
	private static final DateTimeFormatter BATCH_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final InventoryBatchRepository inventoryBatchRepository;
	private final OutboundTaskBatchAllocationRepository outboundTaskBatchAllocationRepository;
	private final AbnormalStockBatchLockRepository abnormalStockBatchLockRepository;

	public InventoryBatchService(
			InventoryBatchRepository inventoryBatchRepository,
			OutboundTaskBatchAllocationRepository outboundTaskBatchAllocationRepository,
			AbnormalStockBatchLockRepository abnormalStockBatchLockRepository) {
		this.inventoryBatchRepository = inventoryBatchRepository;
		this.outboundTaskBatchAllocationRepository = outboundTaskBatchAllocationRepository;
		this.abnormalStockBatchLockRepository = abnormalStockBatchLockRepository;
	}

	@Transactional
	public void createInboundBatch(InboundRecordEntity inboundRecord) {
		inventoryBatchRepository.insert(new InventoryBatchEntity(
				null,
				generateBatchCode(),
				"INBOUND_RECORD",
				inboundRecord.id(),
				inboundRecord.productId(),
				inboundRecord.warehouseId(),
				inboundRecord.zoneId(),
				inboundRecord.locationId(),
				inboundRecord.occurredAt(),
				inboundRecord.shelfLifeDaysSnapshot(),
				inboundRecord.warningDaysSnapshot(),
				calculateWarningAt(
						inboundRecord.occurredAt(),
						inboundRecord.shelfLifeDaysSnapshot(),
						inboundRecord.warningDaysSnapshot()),
				inboundRecord.expectedExpireAt(),
				inboundRecord.quantity(),
				inboundRecord.quantity(),
				STATUS_ACTIVE,
				null,
				null));
	}

	@Transactional
	public void recalculateActiveBatchesForProduct(Long productId, Integer shelfLifeDays, Integer warningDays) {
		inventoryBatchRepository.findActiveByProductId(productId)
				.forEach(batch -> inventoryBatchRepository.updateShelfLifeSnapshot(
						batch.id(),
						shelfLifeDays,
						warningDays,
						calculateWarningAt(batch.baseOccurredAt(), shelfLifeDays, warningDays),
						batch.baseOccurredAt().plusDays(shelfLifeDays)));
	}

	@Transactional
	public void replaceOutboundAllocations(
			Long outboundTaskId,
			Long productId,
			Long warehouseId,
			Long locationId,
			BigDecimal quantity) {
		List<OutboundTaskBatchAllocationEntity> allocations = new ArrayList<>();
		BigDecimal remainingQuantity = quantity;

		for (InventoryBatchEntity batch : inventoryBatchRepository.findActiveByProductAndLocation(
				productId, warehouseId, locationId)) {
			remainingQuantity = allocateFromBatch(
					allocations,
					remainingQuantity,
					outboundTaskId,
					batch,
					outboundTaskBatchAllocationRepository.sumReservedQuantityByBatchId(batch.id(), outboundTaskId),
					abnormalStockBatchLockRepository.sumActiveLockedQuantityByBatchId(batch.id(), null));
			if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
				break;
			}
		}

		if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
			throw new BusinessException(CommonErrorCode.OUTBOUND_TASK_STOCK_INSUFFICIENT);
		}

		outboundTaskBatchAllocationRepository.replaceAllocations(outboundTaskId, allocations);
	}

	@Transactional
	public void consumeOutboundAllocations(Long outboundTaskId) {
		for (OutboundTaskBatchAllocationEntity allocation : outboundTaskBatchAllocationRepository.findByOutboundTaskId(outboundTaskId)) {
			boolean decreased = inventoryBatchRepository.decreaseRemainingQuantity(
					allocation.inventoryBatchId(),
					allocation.allocatedQuantity());
			if (!decreased) {
				throw new BusinessException(CommonErrorCode.OUTBOUND_TASK_STOCK_INSUFFICIENT);
			}

			InventoryBatchEntity updatedBatch = inventoryBatchRepository.findById(allocation.inventoryBatchId())
					.orElseThrow();
			if (updatedBatch.remainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
				inventoryBatchRepository.updateStatus(updatedBatch.id(), STATUS_DEPLETED);
			}
		}
	}

	@Transactional
	public void clearOutboundAllocations(Long outboundTaskId) {
		outboundTaskBatchAllocationRepository.deleteByOutboundTaskId(outboundTaskId);
	}

	@Transactional
	public void lockForInspection(
			Long abnormalStockId,
			String sourceType,
			Long sourceId,
			Long productId,
			Long warehouseId,
			Long locationId,
			BigDecimal quantity) {
		List<AbnormalStockBatchLockEntity> locks = new ArrayList<>();
		BigDecimal remainingQuantity = quantity;

		Optional<InventoryBatchEntity> preferredBatch = "INBOUND_RECORD".equals(sourceType)
				? inventoryBatchRepository.findBySource("INBOUND_RECORD", sourceId)
				: Optional.empty();

		List<InventoryBatchEntity> candidateBatches = new ArrayList<>(inventoryBatchRepository.findActiveByProductAndLocation(
				productId, warehouseId, locationId));
		preferredBatch.ifPresent(batch -> candidateBatches.sort(Comparator.comparing(
				current -> !current.id().equals(batch.id()))));

		for (InventoryBatchEntity batch : candidateBatches) {
			BigDecimal reservedQuantity = outboundTaskBatchAllocationRepository.sumReservedQuantityByBatchId(batch.id(), null);
			BigDecimal lockedQuantity = abnormalStockBatchLockRepository.sumActiveLockedQuantityByBatchId(batch.id(), abnormalStockId);
			BigDecimal availableQuantity = batch.remainingQuantity()
					.subtract(reservedQuantity)
					.subtract(lockedQuantity);
			if (availableQuantity.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			BigDecimal lockedPortion = availableQuantity.min(remainingQuantity);
			locks.add(new AbnormalStockBatchLockEntity(
					null,
					abnormalStockId,
					batch.id(),
					lockedPortion,
					null));
			remainingQuantity = remainingQuantity.subtract(lockedPortion);
			if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
				break;
			}
		}

		if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
			throw new BusinessException(CommonErrorCode.QUALITY_INSPECTION_SOURCE_INSUFFICIENT);
		}

		abnormalStockBatchLockRepository.insertLocks(locks);
	}

	@Transactional
	public void consumeAbnormalStockLocks(Long abnormalStockId) {
		for (AbnormalStockBatchLockEntity lock : abnormalStockBatchLockRepository.findByAbnormalStockId(abnormalStockId)) {
			boolean decreased = inventoryBatchRepository.decreaseRemainingQuantity(lock.inventoryBatchId(), lock.lockedQuantity());
			if (!decreased) {
				throw new BusinessException(CommonErrorCode.LOSS_RECORD_STOCK_INSUFFICIENT);
			}

			InventoryBatchEntity updatedBatch = inventoryBatchRepository.findById(lock.inventoryBatchId())
					.orElseThrow();
			if (updatedBatch.remainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
				inventoryBatchRepository.updateStatus(updatedBatch.id(), STATUS_DEPLETED);
			}
		}
	}

	@Transactional
	public void consumeDirectLoss(Long productId, Long warehouseId, Long locationId, BigDecimal quantity) {
		BigDecimal remainingQuantity = quantity;
		for (InventoryBatchEntity batch : inventoryBatchRepository.findActiveByProductAndLocation(
				productId, warehouseId, locationId)) {
			BigDecimal reservedQuantity = outboundTaskBatchAllocationRepository.sumReservedQuantityByBatchId(batch.id(), null);
			BigDecimal lockedQuantity = abnormalStockBatchLockRepository.sumActiveLockedQuantityByBatchId(batch.id(), null);
			BigDecimal availableQuantity = batch.remainingQuantity()
					.subtract(reservedQuantity)
					.subtract(lockedQuantity);
			if (availableQuantity.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			BigDecimal consumedQuantity = availableQuantity.min(remainingQuantity);
			boolean decreased = inventoryBatchRepository.decreaseRemainingQuantity(batch.id(), consumedQuantity);
			if (!decreased) {
				throw new BusinessException(CommonErrorCode.LOSS_RECORD_STOCK_INSUFFICIENT);
			}

			InventoryBatchEntity updatedBatch = inventoryBatchRepository.findById(batch.id()).orElseThrow();
			if (updatedBatch.remainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
				inventoryBatchRepository.updateStatus(updatedBatch.id(), STATUS_DEPLETED);
			}

			remainingQuantity = remainingQuantity.subtract(consumedQuantity);
			if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
				return;
			}
		}

		throw new BusinessException(CommonErrorCode.LOSS_RECORD_STOCK_INSUFFICIENT);
	}

	private BigDecimal allocateFromBatch(
			List<OutboundTaskBatchAllocationEntity> allocations,
			BigDecimal remainingQuantity,
			Long outboundTaskId,
			InventoryBatchEntity batch,
			BigDecimal reservedQuantity,
			BigDecimal lockedQuantity) {
		BigDecimal availableQuantity = batch.remainingQuantity()
				.subtract(reservedQuantity)
				.subtract(lockedQuantity);
		if (availableQuantity.compareTo(BigDecimal.ZERO) <= 0) {
			return remainingQuantity;
		}

		BigDecimal allocatedQuantity = availableQuantity.min(remainingQuantity);
		allocations.add(new OutboundTaskBatchAllocationEntity(
				null,
				outboundTaskId,
				batch.id(),
				allocatedQuantity,
				null));
		return remainingQuantity.subtract(allocatedQuantity);
	}

	private LocalDateTime calculateWarningAt(
			LocalDateTime baseOccurredAt,
			Integer shelfLifeDays,
			Integer warningDays) {
		return baseOccurredAt.plusDays(Math.max(shelfLifeDays - warningDays, 0));
	}

	private String generateBatchCode() {
		return "BATCH-" + LocalDateTime.now().format(BATCH_CODE_FORMATTER);
	}
}
