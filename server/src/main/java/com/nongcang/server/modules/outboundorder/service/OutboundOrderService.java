package com.nongcang.server.modules.outboundorder.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.common.validation.QuantityPrecisionValidator;
import com.nongcang.server.modules.customer.repository.CustomerRepository;
import com.nongcang.server.modules.outboundtask.service.OutboundTaskService;
import com.nongcang.server.modules.outboundorder.domain.dto.OutboundOrderCreateRequest;
import com.nongcang.server.modules.outboundorder.domain.dto.OutboundOrderItemRequest;
import com.nongcang.server.modules.outboundorder.domain.dto.OutboundOrderListQueryRequest;
import com.nongcang.server.modules.outboundorder.domain.dto.OutboundOrderUpdateRequest;
import com.nongcang.server.modules.outboundorder.domain.entity.OutboundOrderEntity;
import com.nongcang.server.modules.outboundorder.domain.entity.OutboundOrderItemEntity;
import com.nongcang.server.modules.outboundorder.domain.vo.OutboundOrderDetailResponse;
import com.nongcang.server.modules.outboundorder.domain.vo.OutboundOrderItemResponse;
import com.nongcang.server.modules.outboundorder.domain.vo.OutboundOrderListItemResponse;
import com.nongcang.server.modules.outboundorder.repository.OutboundOrderRepository;
import com.nongcang.server.modules.productarchive.repository.ProductArchiveRepository;
import com.nongcang.server.modules.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OutboundOrderService {

	private static final int STATUS_PENDING_ALLOCATE = 1;
	private static final int STATUS_WAIT_PICK = 2;
	private static final int STATUS_WAIT_OUTBOUND = 3;
	private static final int STATUS_COMPLETED = 4;
	private static final int STATUS_CANCELLED = 5;

	private static final DateTimeFormatter OUTBOUND_ORDER_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final OutboundOrderRepository outboundOrderRepository;
	private final CustomerRepository customerRepository;
	private final WarehouseRepository warehouseRepository;
	private final ProductArchiveRepository productArchiveRepository;
	private final OutboundTaskService outboundTaskService;
	private final QuantityPrecisionValidator quantityPrecisionValidator;

	public OutboundOrderService(
			OutboundOrderRepository outboundOrderRepository,
			CustomerRepository customerRepository,
			WarehouseRepository warehouseRepository,
			ProductArchiveRepository productArchiveRepository,
			OutboundTaskService outboundTaskService,
			QuantityPrecisionValidator quantityPrecisionValidator) {
		this.outboundOrderRepository = outboundOrderRepository;
		this.customerRepository = customerRepository;
		this.warehouseRepository = warehouseRepository;
		this.productArchiveRepository = productArchiveRepository;
		this.outboundTaskService = outboundTaskService;
		this.quantityPrecisionValidator = quantityPrecisionValidator;
	}

	public List<OutboundOrderListItemResponse> getOutboundOrderList(OutboundOrderListQueryRequest queryRequest) {
		return outboundOrderRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public OutboundOrderDetailResponse getOutboundOrderDetail(Long id) {
		OutboundOrderEntity outboundOrder = getExistingOutboundOrder(id);
		List<OutboundOrderItemResponse> items = outboundOrderRepository.findItemsByOrderId(id)
				.stream()
				.map(this::toItemResponse)
				.toList();
		return toDetailResponse(outboundOrder, items);
	}

	@Transactional
	public OutboundOrderDetailResponse createOutboundOrder(OutboundOrderCreateRequest request) {
		resolveCustomerId(request.customerId());
		resolveWarehouseId(request.warehouseId());
		List<OutboundOrderItemEntity> items = buildValidatedItems(request.items());

		OutboundOrderEntity outboundOrder = new OutboundOrderEntity(
				null,
				generateOutboundOrderCode(),
				request.customerId(),
				null,
				request.warehouseId(),
				null,
				parseDateTime(request.expectedDeliveryAt()),
				null,
				items.size(),
				OutboundOrderRepository.sumQuantity(items),
				STATUS_PENDING_ALLOCATE,
				trimToNull(request.remarks()),
				null,
				null);

		long outboundOrderId = outboundOrderRepository.insertOrder(outboundOrder);
		outboundOrderRepository.insertOrderItems(outboundOrderId, items);
		return getOutboundOrderDetail(outboundOrderId);
	}

	@Transactional
	public OutboundOrderDetailResponse updateOutboundOrder(Long id, OutboundOrderUpdateRequest request) {
		OutboundOrderEntity currentOrder = getExistingOutboundOrder(id);
		ensurePendingAllocate(currentOrder.status(), "当前出库单状态不允许编辑");

		resolveCustomerId(request.customerId());
		resolveWarehouseId(request.warehouseId());
		List<OutboundOrderItemEntity> items = buildValidatedItems(request.items());

		OutboundOrderEntity updatedOrder = new OutboundOrderEntity(
				currentOrder.id(),
				currentOrder.orderCode(),
				request.customerId(),
				currentOrder.customerName(),
				request.warehouseId(),
				currentOrder.warehouseName(),
				parseDateTime(request.expectedDeliveryAt()),
				currentOrder.actualOutboundAt(),
				items.size(),
				OutboundOrderRepository.sumQuantity(items),
				currentOrder.status(),
				trimToNull(request.remarks()),
				currentOrder.createdAt(),
				currentOrder.updatedAt());

		outboundOrderRepository.updateOrder(updatedOrder);
		outboundOrderRepository.deleteOrderItems(id);
		outboundOrderRepository.insertOrderItems(id, items);
		return getOutboundOrderDetail(id);
	}

	@Transactional
	public void cancelOutboundOrder(Long id) {
		OutboundOrderEntity currentOrder = getExistingOutboundOrder(id);
		ensurePendingAllocate(currentOrder.status(), "当前出库单状态不允许取消");
		outboundOrderRepository.updateStatus(id, STATUS_CANCELLED, currentOrder.actualOutboundAt());
	}

	@Transactional
	public void dispatchOutboundOrder(Long id) {
		OutboundOrderEntity currentOrder = getExistingOutboundOrder(id);
		ensurePendingAllocate(currentOrder.status(), "当前出库单状态不允许生成拣货任务");
		List<OutboundOrderItemEntity> items = outboundOrderRepository.findItemsByOrderId(id);
		outboundOrderRepository.updateStatus(id, STATUS_WAIT_PICK, null);
		OutboundOrderEntity updatedOrder = getExistingOutboundOrder(id);
		outboundTaskService.createTasksForOutboundOrder(updatedOrder, items);
	}

	private OutboundOrderEntity getExistingOutboundOrder(Long id) {
		return outboundOrderRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.OUTBOUND_ORDER_NOT_FOUND));
	}

	private boolean matchesQuery(OutboundOrderEntity entity, OutboundOrderListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.orderCode())
				&& !entity.orderCode().contains(queryRequest.orderCode().trim())) {
			return false;
		}

		if (queryRequest.customerId() != null && !Objects.equals(entity.customerId(), queryRequest.customerId())) {
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

	private List<OutboundOrderItemEntity> buildValidatedItems(List<OutboundOrderItemRequest> requests) {
		if (requests == null || requests.isEmpty()) {
			throw new BusinessException(CommonErrorCode.OUTBOUND_ORDER_ITEMS_EMPTY);
		}

		return requests.stream()
				.map(this::toValidatedItemEntity)
				.toList();
	}

	private OutboundOrderItemEntity toValidatedItemEntity(OutboundOrderItemRequest request) {
		if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException(CommonErrorCode.OUTBOUND_ORDER_ITEM_QUANTITY_INVALID);
		}

		var productArchive = productArchiveRepository.findById(request.productId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ARCHIVE_NOT_FOUND));
		quantityPrecisionValidator.validate(
				request.quantity(),
				productArchive.precisionDigits(),
				productArchive.unitName());

		return new OutboundOrderItemEntity(
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

	private Long resolveCustomerId(Long customerId) {
		return customerRepository.findById(customerId)
				.map(customer -> customer.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.CUSTOMER_NOT_FOUND));
	}

	private Long resolveWarehouseId(Long warehouseId) {
		return warehouseRepository.findById(warehouseId)
				.map(warehouse -> warehouse.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_NOT_FOUND));
	}

	private void ensurePendingAllocate(Integer status, String message) {
		if (!Objects.equals(status, STATUS_PENDING_ALLOCATE)) {
			throw new BusinessException(CommonErrorCode.OUTBOUND_ORDER_STATUS_INVALID, message);
		}
	}

	private String generateOutboundOrderCode() {
		for (int index = 0; index < 20; index += 1) {
			String outboundOrderCode = "OUT-" + LocalDateTime.now().format(OUTBOUND_ORDER_CODE_FORMATTER);

			if (index > 0) {
				outboundOrderCode += "-" + index;
			}

			if (!outboundOrderRepository.existsByOrderCode(outboundOrderCode)) {
				return outboundOrderCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "出库单编号生成失败，请稍后重试");
	}

	private OutboundOrderListItemResponse toListItemResponse(OutboundOrderEntity entity) {
		return new OutboundOrderListItemResponse(
				entity.id(),
				entity.orderCode(),
				entity.customerId(),
				entity.customerName(),
				entity.warehouseId(),
				entity.warehouseName(),
				toIsoDateTime(entity.expectedDeliveryAt()),
				toIsoDateTime(entity.actualOutboundAt()),
				entity.totalItemCount(),
				toDouble(entity.totalQuantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private OutboundOrderDetailResponse toDetailResponse(
			OutboundOrderEntity entity,
			List<OutboundOrderItemResponse> items) {
		return new OutboundOrderDetailResponse(
				entity.id(),
				entity.orderCode(),
				entity.customerId(),
				entity.customerName(),
				entity.warehouseId(),
				entity.warehouseName(),
				toIsoDateTime(entity.expectedDeliveryAt()),
				toIsoDateTime(entity.actualOutboundAt()),
				entity.totalItemCount(),
				toDouble(entity.totalQuantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()),
				items);
	}

	private OutboundOrderItemResponse toItemResponse(OutboundOrderItemEntity entity) {
		return new OutboundOrderItemResponse(
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
			case STATUS_PENDING_ALLOCATE -> "待分配";
			case STATUS_WAIT_PICK -> "待拣货";
			case STATUS_WAIT_OUTBOUND -> "待出库";
			case STATUS_COMPLETED -> "已完成";
			case STATUS_CANCELLED -> "已取消";
			default -> "未知状态";
		};
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private LocalDateTime parseDateTime(String value) {
		return LocalDateTime.parse(value);
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
