package com.nongcang.server.modules.inventoryadjustment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.common.security.WarehouseAccessScopeService;
import com.nongcang.server.common.validation.QuantityPrecisionValidator;
import com.nongcang.server.modules.inventoryadjustment.domain.dto.InventoryAdjustmentCreateRequest;
import com.nongcang.server.modules.inventoryadjustment.domain.dto.InventoryAdjustmentListQueryRequest;
import com.nongcang.server.modules.inventoryadjustment.domain.entity.InventoryAdjustmentEntity;
import com.nongcang.server.modules.inventoryadjustment.domain.vo.InventoryAdjustmentDetailResponse;
import com.nongcang.server.modules.inventoryadjustment.domain.vo.InventoryAdjustmentListItemResponse;
import com.nongcang.server.modules.inventoryadjustment.repository.InventoryAdjustmentRepository;
import com.nongcang.server.modules.inventorysupport.domain.entity.InventoryLocationStockEntity;
import com.nongcang.server.modules.inventorysupport.repository.InventoryStockRepository;
import com.nongcang.server.modules.inventorysupport.repository.InventoryTransactionRepository;
import com.nongcang.server.modules.productarchive.repository.ProductArchiveRepository;
import com.nongcang.server.modules.warehouse.repository.WarehouseRepository;
import com.nongcang.server.modules.warehouselocation.domain.entity.WarehouseLocationEntity;
import com.nongcang.server.modules.warehouselocation.repository.WarehouseLocationRepository;
import com.nongcang.server.modules.warehousezone.domain.entity.WarehouseZoneEntity;
import com.nongcang.server.modules.warehousezone.repository.WarehouseZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class InventoryAdjustmentService {

	private static final String TYPE_INCREASE = "INCREASE";
	private static final String TYPE_DECREASE = "DECREASE";
	private static final DateTimeFormatter ADJUSTMENT_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
	private static final DateTimeFormatter TRANSACTION_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
	private final InventoryStockRepository inventoryStockRepository;
	private final InventoryTransactionRepository inventoryTransactionRepository;
	private final WarehouseRepository warehouseRepository;
	private final WarehouseZoneRepository warehouseZoneRepository;
	private final WarehouseLocationRepository warehouseLocationRepository;
	private final ProductArchiveRepository productArchiveRepository;
	private final QuantityPrecisionValidator quantityPrecisionValidator;
	private final WarehouseAccessScopeService warehouseAccessScopeService;

	public InventoryAdjustmentService(
			InventoryAdjustmentRepository inventoryAdjustmentRepository,
			InventoryStockRepository inventoryStockRepository,
			InventoryTransactionRepository inventoryTransactionRepository,
			WarehouseRepository warehouseRepository,
			WarehouseZoneRepository warehouseZoneRepository,
			WarehouseLocationRepository warehouseLocationRepository,
			ProductArchiveRepository productArchiveRepository,
			QuantityPrecisionValidator quantityPrecisionValidator,
			WarehouseAccessScopeService warehouseAccessScopeService) {
		this.inventoryAdjustmentRepository = inventoryAdjustmentRepository;
		this.inventoryStockRepository = inventoryStockRepository;
		this.inventoryTransactionRepository = inventoryTransactionRepository;
		this.warehouseRepository = warehouseRepository;
		this.warehouseZoneRepository = warehouseZoneRepository;
		this.warehouseLocationRepository = warehouseLocationRepository;
		this.productArchiveRepository = productArchiveRepository;
		this.quantityPrecisionValidator = quantityPrecisionValidator;
		this.warehouseAccessScopeService = warehouseAccessScopeService;
	}

	public List<InventoryAdjustmentListItemResponse> getInventoryAdjustmentList(
			InventoryAdjustmentListQueryRequest queryRequest) {
		Long scopedWarehouseId = warehouseAccessScopeService.resolveQueryWarehouseId(queryRequest.warehouseId());
		return inventoryAdjustmentRepository.findAll()
				.stream()
				.filter(entity -> scopedWarehouseId == null || Objects.equals(entity.warehouseId(), scopedWarehouseId))
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public InventoryAdjustmentDetailResponse getInventoryAdjustmentDetail(Long id) {
		InventoryAdjustmentEntity adjustment = getExistingAdjustment(id);
		warehouseAccessScopeService.assertWarehouseAccess(adjustment.warehouseId());
		return toDetailResponse(adjustment);
	}

	@Transactional
	public InventoryAdjustmentDetailResponse createInventoryAdjustment(InventoryAdjustmentCreateRequest request) {
		String adjustmentType = normalizeAdjustmentType(request.adjustmentType());
		BigDecimal quantity = validateQuantity(request.quantity());

		Long warehouseId = resolveWarehouseId(warehouseAccessScopeService.resolveRequiredWarehouseId(request.warehouseId()));
		WarehouseZoneEntity zone = resolveZone(request.zoneId());
		WarehouseLocationEntity location = resolveLocation(request.locationId());
		validateWarehouseLocationRelation(warehouseId, zone, location);
		var productArchive = productArchiveRepository.findById(request.productId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ARCHIVE_NOT_FOUND));
		quantityPrecisionValidator.validate(
				quantity,
				productArchive.precisionDigits(),
				productArchive.unitName());

		if (TYPE_DECREASE.equals(adjustmentType)) {
			validateDecreaseAvailable(
					request.productId(),
					warehouseId,
					request.locationId(),
					quantity);
			boolean decreased = inventoryStockRepository.decreaseStock(
					request.productId(),
					warehouseId,
					request.zoneId(),
					request.locationId(),
					quantity);

			if (!decreased) {
				throw new BusinessException(CommonErrorCode.INVENTORY_ADJUSTMENT_STOCK_INSUFFICIENT);
			}
		} else {
			inventoryStockRepository.increaseStock(
					request.productId(),
					warehouseId,
					request.zoneId(),
					request.locationId(),
					quantity);
		}

		long adjustmentId = inventoryAdjustmentRepository.insert(
				generateAdjustmentCode(),
				warehouseId,
				request.zoneId(),
				request.locationId(),
				request.productId(),
				adjustmentType,
				quantity,
				request.reason().trim(),
				trimToNull(request.remarks()));

		inventoryTransactionRepository.insertTransaction(
				generateTransactionCode(),
				"ADJUSTMENT",
				request.productId(),
				warehouseId,
				request.zoneId(),
				request.locationId(),
				TYPE_INCREASE.equals(adjustmentType) ? quantity : quantity.negate(),
				"INVENTORY_ADJUSTMENT",
				adjustmentId,
				LocalDateTime.now(),
				request.reason().trim());

		return getInventoryAdjustmentDetail(adjustmentId);
	}

	private InventoryAdjustmentEntity getExistingAdjustment(Long id) {
		return inventoryAdjustmentRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));
	}

	private boolean matchesQuery(
			InventoryAdjustmentEntity entity,
			InventoryAdjustmentListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.adjustmentCode())
				&& !entity.adjustmentCode().contains(queryRequest.adjustmentCode().trim())) {
			return false;
		}

		if (queryRequest.warehouseId() != null && !Objects.equals(entity.warehouseId(), queryRequest.warehouseId())) {
			return false;
		}

		if (queryRequest.productId() != null && !Objects.equals(entity.productId(), queryRequest.productId())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.adjustmentType())
				&& !entity.adjustmentType().equals(queryRequest.adjustmentType().trim())) {
			return false;
		}

		return true;
	}

	private String normalizeAdjustmentType(String adjustmentType) {
		String normalized = adjustmentType == null ? "" : adjustmentType.trim().toUpperCase();

		if (!TYPE_INCREASE.equals(normalized) && !TYPE_DECREASE.equals(normalized)) {
			throw new BusinessException(CommonErrorCode.INVENTORY_ADJUSTMENT_TYPE_INVALID);
		}

		return normalized;
	}

	private BigDecimal validateQuantity(BigDecimal quantity) {
		if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException(CommonErrorCode.INVENTORY_ADJUSTMENT_QUANTITY_INVALID);
		}

		return quantity;
	}

	private void validateDecreaseAvailable(
			Long productId,
			Long warehouseId,
			Long locationId,
			BigDecimal quantity) {
		InventoryLocationStockEntity stockOption = inventoryStockRepository
				.findAvailableStocks(productId, warehouseId, null)
				.stream()
				.filter(option -> Objects.equals(option.locationId(), locationId))
				.findFirst()
				.orElseThrow(() -> new BusinessException(CommonErrorCode.INVENTORY_ADJUSTMENT_STOCK_INSUFFICIENT));

		if (stockOption.availableQuantity().compareTo(quantity) < 0) {
			throw new BusinessException(CommonErrorCode.INVENTORY_ADJUSTMENT_STOCK_INSUFFICIENT);
		}
	}

	private Long resolveWarehouseId(Long warehouseId) {
		return warehouseRepository.findById(warehouseId)
				.map(warehouse -> warehouse.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_NOT_FOUND));
	}

	private WarehouseZoneEntity resolveZone(Long zoneId) {
		return warehouseZoneRepository.findById(zoneId)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_ZONE_NOT_FOUND));
	}

	private WarehouseLocationEntity resolveLocation(Long locationId) {
		return warehouseLocationRepository.findById(locationId)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_NOT_FOUND));
	}

	private void validateWarehouseLocationRelation(
			Long warehouseId,
			WarehouseZoneEntity zone,
			WarehouseLocationEntity location) {
		if (!Objects.equals(zone.warehouseId(), warehouseId)
				|| !Objects.equals(location.warehouseId(), warehouseId)
				|| !Objects.equals(location.zoneId(), zone.id())) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_ZONE_MISMATCH);
		}
	}

	private String generateAdjustmentCode() {
		for (int index = 0; index < 20; index += 1) {
			String adjustmentCode = "ADJ-" + LocalDateTime.now().format(ADJUSTMENT_CODE_FORMATTER);

			if (index > 0) {
				adjustmentCode += "-" + index;
			}

			if (!inventoryAdjustmentRepository.existsByAdjustmentCode(adjustmentCode)) {
				return adjustmentCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "库存调整编号生成失败，请稍后重试");
	}

	private String generateTransactionCode() {
		return "INVTX-" + LocalDateTime.now().format(TRANSACTION_CODE_FORMATTER);
	}

	private InventoryAdjustmentListItemResponse toListItemResponse(InventoryAdjustmentEntity entity) {
		return new InventoryAdjustmentListItemResponse(
				entity.id(),
				entity.adjustmentCode(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.adjustmentType(),
				InventoryAdjustmentRepository.toDouble(entity.quantity()),
				entity.reason(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private InventoryAdjustmentDetailResponse toDetailResponse(InventoryAdjustmentEntity entity) {
		return new InventoryAdjustmentDetailResponse(
				entity.id(),
				entity.adjustmentCode(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.adjustmentType(),
				InventoryAdjustmentRepository.toDouble(entity.quantity()),
				entity.reason(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
