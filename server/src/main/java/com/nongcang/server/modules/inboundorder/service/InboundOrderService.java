package com.nongcang.server.modules.inboundorder.service;

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
import com.nongcang.server.modules.inboundorder.domain.dto.InboundOrderCreateRequest;
import com.nongcang.server.modules.inboundorder.domain.dto.InboundOrderItemRequest;
import com.nongcang.server.modules.inboundorder.domain.dto.InboundOrderListQueryRequest;
import com.nongcang.server.modules.inboundorder.domain.dto.InboundOrderUpdateRequest;
import com.nongcang.server.modules.inboundorder.domain.entity.InboundOrderEntity;
import com.nongcang.server.modules.inboundorder.domain.entity.InboundOrderItemEntity;
import com.nongcang.server.modules.inboundorder.domain.vo.InboundOrderDetailResponse;
import com.nongcang.server.modules.inboundorder.domain.vo.InboundOrderItemResponse;
import com.nongcang.server.modules.inboundorder.domain.vo.InboundOrderListItemResponse;
import com.nongcang.server.modules.inboundorder.repository.InboundOrderRepository;
import com.nongcang.server.modules.putawaytask.service.PutawayTaskService;
import com.nongcang.server.modules.productarchive.repository.ProductArchiveRepository;
import com.nongcang.server.modules.supplier.repository.SupplierRepository;
import com.nongcang.server.modules.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class InboundOrderService {

	private static final int STATUS_PENDING_ARRIVAL = 1;
	private static final int STATUS_WAIT_PUTAWAY = 2;
	private static final int STATUS_COMPLETED = 3;
	private static final int STATUS_CANCELLED = 4;

	private static final DateTimeFormatter INBOUND_ORDER_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final InboundOrderRepository inboundOrderRepository;
	private final SupplierRepository supplierRepository;
	private final WarehouseRepository warehouseRepository;
	private final ProductArchiveRepository productArchiveRepository;
	private final PutawayTaskService putawayTaskService;
	private final QuantityPrecisionValidator quantityPrecisionValidator;
	private final WarehouseAccessScopeService warehouseAccessScopeService;

	public InboundOrderService(
			InboundOrderRepository inboundOrderRepository,
			SupplierRepository supplierRepository,
			WarehouseRepository warehouseRepository,
			ProductArchiveRepository productArchiveRepository,
			PutawayTaskService putawayTaskService,
			QuantityPrecisionValidator quantityPrecisionValidator,
			WarehouseAccessScopeService warehouseAccessScopeService) {
		this.inboundOrderRepository = inboundOrderRepository;
		this.supplierRepository = supplierRepository;
		this.warehouseRepository = warehouseRepository;
		this.productArchiveRepository = productArchiveRepository;
		this.putawayTaskService = putawayTaskService;
		this.quantityPrecisionValidator = quantityPrecisionValidator;
		this.warehouseAccessScopeService = warehouseAccessScopeService;
	}

	public List<InboundOrderListItemResponse> getInboundOrderList(InboundOrderListQueryRequest queryRequest) {
		Long scopedWarehouseId = warehouseAccessScopeService.resolveQueryWarehouseId(queryRequest.warehouseId());
		return inboundOrderRepository.findAll()
				.stream()
				.filter(entity -> scopedWarehouseId == null || Objects.equals(entity.warehouseId(), scopedWarehouseId))
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public InboundOrderDetailResponse getInboundOrderDetail(Long id) {
		InboundOrderEntity inboundOrder = getExistingInboundOrder(id);
		warehouseAccessScopeService.assertWarehouseAccess(inboundOrder.warehouseId());
		List<InboundOrderItemResponse> items = inboundOrderRepository.findItemsByOrderId(id)
				.stream()
				.map(this::toItemResponse)
				.toList();
		return toDetailResponse(inboundOrder, items);
	}

	@Transactional
	public InboundOrderDetailResponse createInboundOrder(InboundOrderCreateRequest request) {
		resolveSupplierId(request.supplierId());
		Long warehouseId = resolveWarehouseId(warehouseAccessScopeService.resolveRequiredWarehouseId(request.warehouseId()));
		List<InboundOrderItemEntity> items = buildValidatedItems(request.items());

		InboundOrderEntity inboundOrder = new InboundOrderEntity(
				null,
				generateInboundOrderCode(),
				request.supplierId(),
				null,
				warehouseId,
				null,
				parseDateTime(request.expectedArrivalAt()),
				null,
				items.size(),
				InboundOrderRepository.sumQuantity(items),
				STATUS_PENDING_ARRIVAL,
				trimToNull(request.remarks()),
				null,
				null);

		long inboundOrderId = inboundOrderRepository.insertOrder(inboundOrder);
		inboundOrderRepository.insertOrderItems(inboundOrderId, items);
		return getInboundOrderDetail(inboundOrderId);
	}

	@Transactional
	public InboundOrderDetailResponse updateInboundOrder(Long id, InboundOrderUpdateRequest request) {
		InboundOrderEntity currentOrder = getExistingInboundOrder(id);
		warehouseAccessScopeService.assertWarehouseAccess(currentOrder.warehouseId());
		ensurePendingArrival(currentOrder.status(), "当前入库单状态不允许编辑");

		resolveSupplierId(request.supplierId());
		Long warehouseId = resolveWarehouseId(warehouseAccessScopeService.resolveRequiredWarehouseId(request.warehouseId()));
		List<InboundOrderItemEntity> items = buildValidatedItems(request.items());

		InboundOrderEntity updatedOrder = new InboundOrderEntity(
				currentOrder.id(),
				currentOrder.orderCode(),
				request.supplierId(),
				currentOrder.supplierName(),
				warehouseId,
				currentOrder.warehouseName(),
				parseDateTime(request.expectedArrivalAt()),
				currentOrder.actualArrivalAt(),
				items.size(),
				InboundOrderRepository.sumQuantity(items),
				currentOrder.status(),
				trimToNull(request.remarks()),
				currentOrder.createdAt(),
				currentOrder.updatedAt());

		inboundOrderRepository.updateOrder(updatedOrder);
		inboundOrderRepository.deleteOrderItems(id);
		inboundOrderRepository.insertOrderItems(id, items);
		return getInboundOrderDetail(id);
	}

	@Transactional
	public void confirmArrival(Long id) {
		InboundOrderEntity currentOrder = getExistingInboundOrder(id);
		warehouseAccessScopeService.assertWarehouseAccess(currentOrder.warehouseId());
		ensurePendingArrival(currentOrder.status(), "当前入库单状态不允许到货确认");
		inboundOrderRepository.updateStatus(id, STATUS_WAIT_PUTAWAY, LocalDateTime.now());
		InboundOrderEntity updatedOrder = getExistingInboundOrder(id);
		List<InboundOrderItemEntity> items = inboundOrderRepository.findItemsByOrderId(id);
		putawayTaskService.createTasksForInboundOrder(updatedOrder, items);
	}

	@Transactional
	public void cancelInboundOrder(Long id) {
		InboundOrderEntity currentOrder = getExistingInboundOrder(id);
		warehouseAccessScopeService.assertWarehouseAccess(currentOrder.warehouseId());
		ensurePendingArrival(currentOrder.status(), "当前入库单状态不允许取消");
		inboundOrderRepository.updateStatus(id, STATUS_CANCELLED, currentOrder.actualArrivalAt());
	}

	private InboundOrderEntity getExistingInboundOrder(Long id) {
		return inboundOrderRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.INBOUND_ORDER_NOT_FOUND));
	}

	private boolean matchesQuery(InboundOrderEntity entity, InboundOrderListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.orderCode())
				&& !entity.orderCode().contains(queryRequest.orderCode().trim())) {
			return false;
		}

		if (queryRequest.supplierId() != null && !Objects.equals(entity.supplierId(), queryRequest.supplierId())) {
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

	private List<InboundOrderItemEntity> buildValidatedItems(List<InboundOrderItemRequest> requests) {
		if (requests == null || requests.isEmpty()) {
			throw new BusinessException(CommonErrorCode.INBOUND_ORDER_ITEMS_EMPTY);
		}

		return requests.stream()
				.map(this::toValidatedItemEntity)
				.toList();
	}

	private InboundOrderItemEntity toValidatedItemEntity(InboundOrderItemRequest request) {
		if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException(CommonErrorCode.INBOUND_ORDER_ITEM_QUANTITY_INVALID);
		}

		var productArchive = productArchiveRepository.findById(request.productId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ARCHIVE_NOT_FOUND));
		quantityPrecisionValidator.validate(
				request.quantity(),
				productArchive.precisionDigits(),
				productArchive.unitName());

		return new InboundOrderItemEntity(
				null,
				null,
				productArchive.id(),
				productArchive.productCode(),
				productArchive.productName(),
				productArchive.productSpecification(),
				productArchive.unitId(),
				productArchive.unitName(),
				productArchive.unitSymbol(),
				request.quantity(),
				request.sortOrder() == null ? 0 : request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);
	}

	private Long resolveSupplierId(Long supplierId) {
		return supplierRepository.findById(supplierId)
				.map(supplier -> supplier.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.SUPPLIER_NOT_FOUND));
	}

	private Long resolveWarehouseId(Long warehouseId) {
		return warehouseRepository.findById(warehouseId)
				.map(warehouse -> warehouse.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_NOT_FOUND));
	}

	private void ensurePendingArrival(Integer status, String message) {
		if (!Objects.equals(status, STATUS_PENDING_ARRIVAL)) {
			throw new BusinessException(CommonErrorCode.INBOUND_ORDER_STATUS_INVALID, message);
		}
	}

	private String generateInboundOrderCode() {
		return "IN-" + LocalDateTime.now().format(INBOUND_ORDER_CODE_FORMATTER);
	}

	private InboundOrderListItemResponse toListItemResponse(InboundOrderEntity entity) {
		return new InboundOrderListItemResponse(
				entity.id(),
				entity.orderCode(),
				entity.supplierId(),
				entity.supplierName(),
				entity.warehouseId(),
				entity.warehouseName(),
				toIsoDateTime(entity.expectedArrivalAt()),
				toIsoDateTime(entity.actualArrivalAt()),
				entity.totalItemCount(),
				toDouble(entity.totalQuantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private InboundOrderDetailResponse toDetailResponse(
			InboundOrderEntity entity,
			List<InboundOrderItemResponse> items) {
		return new InboundOrderDetailResponse(
				entity.id(),
				entity.orderCode(),
				entity.supplierId(),
				entity.supplierName(),
				entity.warehouseId(),
				entity.warehouseName(),
				toIsoDateTime(entity.expectedArrivalAt()),
				toIsoDateTime(entity.actualArrivalAt()),
				entity.totalItemCount(),
				toDouble(entity.totalQuantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()),
				items);
	}

	private InboundOrderItemResponse toItemResponse(InboundOrderItemEntity entity) {
		return new InboundOrderItemResponse(
				entity.id(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.productSpecification(),
				entity.unitId(),
				entity.unitName(),
				entity.unitSymbol(),
				toDouble(entity.quantity()),
				entity.sortOrder(),
				entity.remarks());
	}

	private String toStatusLabel(Integer status) {
		return switch (status) {
			case STATUS_PENDING_ARRIVAL -> "待到货";
			case STATUS_WAIT_PUTAWAY -> "待上架";
			case STATUS_COMPLETED -> "已完成";
			case STATUS_CANCELLED -> "已取消";
			default -> "未知状态";
		};
	}

	private Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private LocalDateTime parseDateTime(String value) {
		return LocalDateTime.parse(value);
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
