package com.nongcang.server.modules.inventorystocktaking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.common.validation.QuantityPrecisionValidator;
import com.nongcang.server.modules.inventorystock.domain.entity.InventoryStockEntity;
import com.nongcang.server.modules.inventorystock.repository.InventoryStockQueryRepository;
import com.nongcang.server.modules.inventorystocktaking.domain.dto.InventoryStocktakingCreateRequest;
import com.nongcang.server.modules.inventorystocktaking.domain.dto.InventoryStocktakingItemSaveBatchRequest;
import com.nongcang.server.modules.inventorystocktaking.domain.dto.InventoryStocktakingItemSaveRequest;
import com.nongcang.server.modules.inventorystocktaking.domain.dto.InventoryStocktakingListQueryRequest;
import com.nongcang.server.modules.inventorystocktaking.domain.entity.InventoryStocktakingItemEntity;
import com.nongcang.server.modules.inventorystocktaking.domain.entity.InventoryStocktakingOrderEntity;
import com.nongcang.server.modules.inventorystocktaking.domain.vo.InventoryStocktakingDetailResponse;
import com.nongcang.server.modules.inventorystocktaking.domain.vo.InventoryStocktakingItemResponse;
import com.nongcang.server.modules.inventorystocktaking.domain.vo.InventoryStocktakingListItemResponse;
import com.nongcang.server.modules.inventorystocktaking.repository.InventoryStocktakingRepository;
import com.nongcang.server.modules.inventorysupport.repository.InventoryStockRepository;
import com.nongcang.server.modules.inventorysupport.repository.InventoryTransactionRepository;
import com.nongcang.server.modules.warehouse.repository.WarehouseRepository;
import com.nongcang.server.modules.warehousezone.domain.entity.WarehouseZoneEntity;
import com.nongcang.server.modules.warehousezone.repository.WarehouseZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class InventoryStocktakingService {

	private static final int STATUS_PENDING_COUNT = 1;
	private static final int STATUS_WAIT_CONFIRM = 2;
	private static final int STATUS_COMPLETED = 3;
	private static final int STATUS_CANCELLED = 4;
	private static final DateTimeFormatter STOCKTAKING_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
	private static final DateTimeFormatter TRANSACTION_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final InventoryStocktakingRepository inventoryStocktakingRepository;
	private final InventoryStockQueryRepository inventoryStockQueryRepository;
	private final InventoryStockRepository inventoryStockRepository;
	private final InventoryTransactionRepository inventoryTransactionRepository;
	private final WarehouseRepository warehouseRepository;
	private final WarehouseZoneRepository warehouseZoneRepository;
	private final QuantityPrecisionValidator quantityPrecisionValidator;

	public InventoryStocktakingService(
			InventoryStocktakingRepository inventoryStocktakingRepository,
			InventoryStockQueryRepository inventoryStockQueryRepository,
			InventoryStockRepository inventoryStockRepository,
			InventoryTransactionRepository inventoryTransactionRepository,
			WarehouseRepository warehouseRepository,
			WarehouseZoneRepository warehouseZoneRepository,
			QuantityPrecisionValidator quantityPrecisionValidator) {
		this.inventoryStocktakingRepository = inventoryStocktakingRepository;
		this.inventoryStockQueryRepository = inventoryStockQueryRepository;
		this.inventoryStockRepository = inventoryStockRepository;
		this.inventoryTransactionRepository = inventoryTransactionRepository;
		this.warehouseRepository = warehouseRepository;
		this.warehouseZoneRepository = warehouseZoneRepository;
		this.quantityPrecisionValidator = quantityPrecisionValidator;
	}

	public List<InventoryStocktakingListItemResponse> getInventoryStocktakingList(
			InventoryStocktakingListQueryRequest queryRequest) {
		return inventoryStocktakingRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public InventoryStocktakingDetailResponse getInventoryStocktakingDetail(Long id) {
		InventoryStocktakingOrderEntity order = getExistingOrder(id);
		List<InventoryStocktakingItemResponse> items = inventoryStocktakingRepository.findItemsByOrderId(id)
				.stream()
				.map(this::toItemResponse)
				.toList();
		return toDetailResponse(order, items);
	}

	@Transactional
	public InventoryStocktakingDetailResponse createInventoryStocktaking(InventoryStocktakingCreateRequest request) {
		resolveWarehouseId(request.warehouseId());
		if (request.zoneId() != null) {
			WarehouseZoneEntity zone = resolveZone(request.zoneId());
			if (!Objects.equals(zone.warehouseId(), request.warehouseId())) {
				throw new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_ZONE_MISMATCH);
			}
		}

		List<InventoryStockEntity> scopeStocks = inventoryStockQueryRepository.findAll()
				.stream()
				.filter(stock -> Objects.equals(stock.warehouseId(), request.warehouseId()))
				.filter(stock -> request.zoneId() == null || Objects.equals(stock.zoneId(), request.zoneId()))
				.toList();

		if (scopeStocks.isEmpty()) {
			throw new BusinessException(CommonErrorCode.INVENTORY_STOCKTAKING_SCOPE_EMPTY);
		}

		long orderId = inventoryStocktakingRepository.insertOrder(
				request.warehouseId(),
				request.zoneId(),
				STATUS_PENDING_COUNT,
				generateStocktakingCode(),
				trimToNull(request.remarks()));
		inventoryStocktakingRepository.insertItems(orderId, scopeStocks);
		return getInventoryStocktakingDetail(orderId);
	}

	@Transactional
	public InventoryStocktakingDetailResponse saveItems(
			Long id,
			InventoryStocktakingItemSaveBatchRequest request) {
		InventoryStocktakingOrderEntity order = getExistingOrder(id);
		ensureEditable(order.status());

		Map<Long, InventoryStocktakingItemEntity> itemMap = inventoryStocktakingRepository.findItemsByOrderId(id)
				.stream()
				.collect(Collectors.toMap(InventoryStocktakingItemEntity::id, Function.identity()));

		for (InventoryStocktakingItemSaveRequest itemRequest : request.items()) {
			InventoryStocktakingItemEntity currentItem = itemMap.get(itemRequest.itemId());

			if (currentItem == null) {
				throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
			}

				if (itemRequest.countedQuantity().compareTo(BigDecimal.ZERO) < 0) {
					throw new BusinessException(CommonErrorCode.INVENTORY_STOCKTAKING_COUNT_INVALID);
				}
				quantityPrecisionValidator.validate(
						itemRequest.countedQuantity(),
						currentItem.precisionDigits(),
						currentItem.unitName());

				BigDecimal difference = itemRequest.countedQuantity().subtract(currentItem.systemQuantity());
			inventoryStocktakingRepository.updateItemCount(
					id,
					currentItem.id(),
					itemRequest.countedQuantity(),
					difference,
					trimToNull(itemRequest.remarks()));
		}

		inventoryStocktakingRepository.updateOrderStatus(id, STATUS_WAIT_CONFIRM);
		return getInventoryStocktakingDetail(id);
	}

	@Transactional
	public void confirm(Long id) {
		InventoryStocktakingOrderEntity order = getExistingOrder(id);

		if (!Objects.equals(order.status(), STATUS_WAIT_CONFIRM)) {
			throw new BusinessException(CommonErrorCode.INVENTORY_STOCKTAKING_STATUS_INVALID);
		}

		List<InventoryStocktakingItemEntity> items = inventoryStocktakingRepository.findItemsByOrderId(id);

		for (InventoryStocktakingItemEntity item : items) {
			if (item.countedQuantity() == null) {
				throw new BusinessException(CommonErrorCode.INVENTORY_STOCKTAKING_COUNT_REQUIRED);
			}

			BigDecimal reservedQuantity = inventoryStockRepository.getReservedQuantity(item.productId(), item.locationId());

			if (item.countedQuantity().compareTo(reservedQuantity) < 0) {
				throw new BusinessException(CommonErrorCode.INVENTORY_STOCKTAKING_RESERVED_CONFLICT);
			}

			inventoryStockRepository.setStockQuantity(
					item.productId(),
					item.warehouseId(),
					item.zoneId(),
					item.locationId(),
					item.countedQuantity());

			if (item.differenceQuantity() != null && item.differenceQuantity().compareTo(BigDecimal.ZERO) != 0) {
				inventoryTransactionRepository.insertTransaction(
						generateTransactionCode(),
						"STOCKTAKING",
						item.productId(),
						item.warehouseId(),
						item.zoneId(),
						item.locationId(),
						item.differenceQuantity(),
						"INVENTORY_STOCKTAKING",
						item.id(),
						LocalDateTime.now(),
						item.remarks());
			}
		}

		inventoryStocktakingRepository.updateOrderStatus(id, STATUS_COMPLETED);
	}

	@Transactional
	public void cancel(Long id) {
		InventoryStocktakingOrderEntity order = getExistingOrder(id);

		if (!Objects.equals(order.status(), STATUS_PENDING_COUNT)
				&& !Objects.equals(order.status(), STATUS_WAIT_CONFIRM)) {
			throw new BusinessException(CommonErrorCode.INVENTORY_STOCKTAKING_STATUS_INVALID);
		}

		inventoryStocktakingRepository.updateOrderStatus(id, STATUS_CANCELLED);
	}

	private InventoryStocktakingOrderEntity getExistingOrder(Long id) {
		return inventoryStocktakingRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.INVENTORY_STOCKTAKING_NOT_FOUND));
	}

	private boolean matchesQuery(
			InventoryStocktakingOrderEntity entity,
			InventoryStocktakingListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.stocktakingCode())
				&& !entity.stocktakingCode().contains(queryRequest.stocktakingCode().trim())) {
			return false;
		}

		if (queryRequest.warehouseId() != null && !Objects.equals(entity.warehouseId(), queryRequest.warehouseId())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void ensureEditable(Integer status) {
		if (!Objects.equals(status, STATUS_PENDING_COUNT) && !Objects.equals(status, STATUS_WAIT_CONFIRM)) {
			throw new BusinessException(CommonErrorCode.INVENTORY_STOCKTAKING_STATUS_INVALID);
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

	private String generateStocktakingCode() {
		for (int index = 0; index < 20; index += 1) {
			String stocktakingCode = "STK-" + LocalDateTime.now().format(STOCKTAKING_CODE_FORMATTER);

			if (index > 0) {
				stocktakingCode += "-" + index;
			}

			if (!inventoryStocktakingRepository.existsByStocktakingCode(stocktakingCode)) {
				return stocktakingCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "盘点单编号生成失败，请稍后重试");
	}

	private String generateTransactionCode() {
		return "INVTX-" + LocalDateTime.now().format(TRANSACTION_CODE_FORMATTER);
	}

	private InventoryStocktakingListItemResponse toListItemResponse(InventoryStocktakingOrderEntity entity) {
		return new InventoryStocktakingListItemResponse(
				entity.id(),
				entity.stocktakingCode(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.itemCount(),
				entity.countedItemCount(),
				InventoryStocktakingRepository.toDouble(entity.totalDifferenceQuantity()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private InventoryStocktakingDetailResponse toDetailResponse(
			InventoryStocktakingOrderEntity entity,
			List<InventoryStocktakingItemResponse> items) {
		return new InventoryStocktakingDetailResponse(
				entity.id(),
				entity.stocktakingCode(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.itemCount(),
				entity.countedItemCount(),
				InventoryStocktakingRepository.toDouble(entity.totalDifferenceQuantity()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()),
				items);
	}

	private InventoryStocktakingItemResponse toItemResponse(InventoryStocktakingItemEntity entity) {
		return new InventoryStocktakingItemResponse(
				entity.id(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.productSpecification(),
				entity.unitId(),
				entity.unitName(),
				entity.unitSymbol(),
				entity.precisionDigits(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				InventoryStocktakingRepository.toDouble(entity.systemQuantity()),
				InventoryStocktakingRepository.toDouble(entity.countedQuantity()),
				InventoryStocktakingRepository.toDouble(entity.differenceQuantity()),
				entity.remarks());
	}

	private String toStatusLabel(Integer status) {
		return switch (status) {
			case STATUS_PENDING_COUNT -> "待盘点";
			case STATUS_WAIT_CONFIRM -> "待确认";
			case STATUS_COMPLETED -> "已完成";
			case STATUS_CANCELLED -> "已取消";
			default -> "未知状态";
		};
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
