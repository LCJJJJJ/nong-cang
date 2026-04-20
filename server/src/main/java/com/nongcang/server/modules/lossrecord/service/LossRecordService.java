package com.nongcang.server.modules.lossrecord.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.common.security.WarehouseAccessScopeService;
import com.nongcang.server.common.validation.QuantityPrecisionValidator;
import com.nongcang.server.modules.abnormalstock.domain.entity.AbnormalStockEntity;
import com.nongcang.server.modules.inventorysupport.service.InventoryBatchService;
import com.nongcang.server.modules.inventorysupport.domain.entity.InventoryLocationStockEntity;
import com.nongcang.server.modules.inventorysupport.repository.InventoryStockRepository;
import com.nongcang.server.modules.inventorysupport.repository.InventoryTransactionRepository;
import com.nongcang.server.modules.lossrecord.domain.dto.LossRecordDirectCreateRequest;
import com.nongcang.server.modules.lossrecord.domain.dto.LossRecordListQueryRequest;
import com.nongcang.server.modules.lossrecord.domain.entity.LossRecordEntity;
import com.nongcang.server.modules.lossrecord.domain.vo.LossRecordDetailResponse;
import com.nongcang.server.modules.lossrecord.domain.vo.LossRecordListItemResponse;
import com.nongcang.server.modules.lossrecord.repository.LossRecordRepository;
import com.nongcang.server.modules.productarchive.repository.ProductArchiveRepository;
import com.nongcang.server.modules.warehouselocation.domain.entity.WarehouseLocationEntity;
import com.nongcang.server.modules.warehouselocation.repository.WarehouseLocationRepository;
import com.nongcang.server.modules.warehousezone.domain.entity.WarehouseZoneEntity;
import com.nongcang.server.modules.warehousezone.repository.WarehouseZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LossRecordService {

	private static final DateTimeFormatter LOSS_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final LossRecordRepository lossRecordRepository;
	private final InventoryStockRepository inventoryStockRepository;
	private final InventoryTransactionRepository inventoryTransactionRepository;
	private final ProductArchiveRepository productArchiveRepository;
	private final WarehouseZoneRepository warehouseZoneRepository;
	private final WarehouseLocationRepository warehouseLocationRepository;
	private final QuantityPrecisionValidator quantityPrecisionValidator;
	private final InventoryBatchService inventoryBatchService;
	private final WarehouseAccessScopeService warehouseAccessScopeService;

	public LossRecordService(
			LossRecordRepository lossRecordRepository,
			InventoryStockRepository inventoryStockRepository,
			InventoryTransactionRepository inventoryTransactionRepository,
			ProductArchiveRepository productArchiveRepository,
			WarehouseZoneRepository warehouseZoneRepository,
			WarehouseLocationRepository warehouseLocationRepository,
			QuantityPrecisionValidator quantityPrecisionValidator,
			InventoryBatchService inventoryBatchService,
			WarehouseAccessScopeService warehouseAccessScopeService) {
		this.lossRecordRepository = lossRecordRepository;
		this.inventoryStockRepository = inventoryStockRepository;
		this.inventoryTransactionRepository = inventoryTransactionRepository;
		this.productArchiveRepository = productArchiveRepository;
		this.warehouseZoneRepository = warehouseZoneRepository;
		this.warehouseLocationRepository = warehouseLocationRepository;
		this.quantityPrecisionValidator = quantityPrecisionValidator;
		this.inventoryBatchService = inventoryBatchService;
		this.warehouseAccessScopeService = warehouseAccessScopeService;
	}

	public List<LossRecordListItemResponse> getLossRecordList(LossRecordListQueryRequest queryRequest) {
		Long scopedWarehouseId = warehouseAccessScopeService.resolveQueryWarehouseId(queryRequest.warehouseId());
		return lossRecordRepository.findAll()
				.stream()
				.filter(entity -> scopedWarehouseId == null || Objects.equals(entity.warehouseId(), scopedWarehouseId))
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	@Transactional
	public LossRecordDetailResponse createFromAbnormalStock(AbnormalStockEntity abnormalStock, String lossReason, String remarks) {
		inventoryBatchService.consumeAbnormalStockLocks(abnormalStock.id());
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
		return getLossRecordDetail(lossRecordId);
	}

	@Transactional
	public LossRecordDetailResponse createDirect(LossRecordDirectCreateRequest request) {
		if (request.quantity() == null || request.quantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
			throw new BusinessException(CommonErrorCode.LOSS_RECORD_QUANTITY_INVALID);
		}
		Long warehouseId = warehouseAccessScopeService.resolveRequiredWarehouseId(request.warehouseId());

		WarehouseZoneEntity zone = warehouseZoneRepository.findById(request.zoneId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_ZONE_NOT_FOUND));
		WarehouseLocationEntity location = warehouseLocationRepository.findById(request.locationId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_NOT_FOUND));

		if (!Objects.equals(zone.warehouseId(), warehouseId)
				|| !Objects.equals(location.warehouseId(), warehouseId)
				|| !Objects.equals(location.zoneId(), zone.id())) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_ZONE_MISMATCH);
		}

		var productArchive = productArchiveRepository.findById(request.productId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ARCHIVE_NOT_FOUND));
		quantityPrecisionValidator.validate(request.quantity(), productArchive.precisionDigits(), productArchive.unitName());

		InventoryLocationStockEntity stockOption = inventoryStockRepository.findAvailableStocks(
				request.productId(),
				warehouseId,
				null)
				.stream()
				.filter(option -> Objects.equals(option.locationId(), request.locationId()))
				.findFirst()
				.orElseThrow(() -> new BusinessException(CommonErrorCode.LOSS_RECORD_STOCK_INSUFFICIENT));

		if (stockOption.availableQuantity().compareTo(request.quantity()) < 0) {
			throw new BusinessException(CommonErrorCode.LOSS_RECORD_STOCK_INSUFFICIENT);
		}

		inventoryBatchService.consumeDirectLoss(
				request.productId(),
				warehouseId,
				request.locationId(),
				request.quantity());
		boolean decreased = inventoryStockRepository.decreaseStock(
				request.productId(),
				warehouseId,
				request.zoneId(),
				request.locationId(),
				request.quantity());

		if (!decreased) {
			throw new BusinessException(CommonErrorCode.LOSS_RECORD_STOCK_INSUFFICIENT);
		}

		long lossRecordId = lossRecordRepository.insert(
				generateLossCode(),
				"DIRECT",
				null,
				request.productId(),
				warehouseId,
				request.zoneId(),
				request.locationId(),
				request.quantity(),
				request.lossReason().trim(),
				StringUtils.hasText(request.remarks()) ? request.remarks().trim() : null);

		inventoryTransactionRepository.insertTransaction(
				generateTransactionCode(),
				"LOSS",
				request.productId(),
				warehouseId,
				request.zoneId(),
				request.locationId(),
				request.quantity().negate(),
				"LOSS_RECORD",
				lossRecordId,
				LocalDateTime.now(),
				request.lossReason().trim());
		return getLossRecordDetail(lossRecordId);
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

	public LossRecordDetailResponse getLossRecordDetail(Long id) {
		LossRecordEntity entity = lossRecordRepository.findAll()
				.stream()
				.filter(record -> Objects.equals(record.id(), id))
				.findFirst()
				.orElseThrow(() -> new BusinessException(CommonErrorCode.LOSS_RECORD_NOT_FOUND));
		warehouseAccessScopeService.assertWarehouseAccess(entity.warehouseId());
		return toDetailResponse(entity);
	}

	private boolean matchesQuery(LossRecordEntity entity, LossRecordListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.lossCode())
				&& !entity.lossCode().contains(queryRequest.lossCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.sourceType())
				&& !entity.sourceType().equals(queryRequest.sourceType().trim().toUpperCase())) {
			return false;
		}

		if (queryRequest.warehouseId() != null && !Objects.equals(entity.warehouseId(), queryRequest.warehouseId())) {
			return false;
		}

		if (queryRequest.productId() != null && !Objects.equals(entity.productId(), queryRequest.productId())) {
			return false;
		}

		return true;
	}

	private LossRecordListItemResponse toListItemResponse(LossRecordEntity entity) {
		return new LossRecordListItemResponse(
				entity.id(),
				entity.lossCode(),
				entity.sourceType(),
				entity.sourceId(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.unitName(),
				entity.unitSymbol(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				LossRecordRepository.toDouble(entity.quantity()),
				entity.lossReason(),
				entity.remarks(),
				entity.createdAt().atOffset(java.time.ZoneOffset.ofHours(8)).toString());
	}

	private LossRecordDetailResponse toDetailResponse(LossRecordEntity entity) {
		return new LossRecordDetailResponse(
				entity.id(),
				entity.lossCode(),
				entity.sourceType(),
				entity.sourceId(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.unitName(),
				entity.unitSymbol(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				LossRecordRepository.toDouble(entity.quantity()),
				entity.lossReason(),
				entity.remarks(),
				entity.createdAt().atOffset(java.time.ZoneOffset.ofHours(8)).toString());
	}
}
