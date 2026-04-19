package com.nongcang.server.modules.outboundtask.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.inventorysupport.domain.entity.InventoryLocationStockEntity;
import com.nongcang.server.modules.inventorysupport.repository.InventoryStockRepository;
import com.nongcang.server.modules.inventorysupport.service.InventoryStockService;
import com.nongcang.server.modules.outboundorder.domain.entity.OutboundOrderEntity;
import com.nongcang.server.modules.outboundorder.domain.entity.OutboundOrderItemEntity;
import com.nongcang.server.modules.outboundorder.repository.OutboundOrderRepository;
import com.nongcang.server.modules.outboundrecord.service.OutboundRecordService;
import com.nongcang.server.modules.outboundtask.domain.dto.OutboundAssignRequest;
import com.nongcang.server.modules.outboundtask.domain.dto.OutboundTaskListQueryRequest;
import com.nongcang.server.modules.outboundtask.domain.entity.OutboundTaskEntity;
import com.nongcang.server.modules.outboundtask.domain.vo.OutboundTaskDetailResponse;
import com.nongcang.server.modules.outboundtask.domain.vo.OutboundTaskListItemResponse;
import com.nongcang.server.modules.outboundtask.domain.vo.OutboundTaskStockOptionResponse;
import com.nongcang.server.modules.outboundtask.repository.OutboundTaskRepository;
import com.nongcang.server.modules.warehouselocation.domain.entity.WarehouseLocationEntity;
import com.nongcang.server.modules.warehouselocation.repository.WarehouseLocationRepository;
import com.nongcang.server.modules.warehousezone.domain.entity.WarehouseZoneEntity;
import com.nongcang.server.modules.warehousezone.repository.WarehouseZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OutboundTaskService {

	private static final int STATUS_WAIT_ASSIGN = 1;
	private static final int STATUS_WAIT_PICK = 2;
	private static final int STATUS_WAIT_OUTBOUND = 3;
	private static final int STATUS_COMPLETED = 4;
	private static final int STATUS_CANCELLED = 5;

	private static final int ORDER_STATUS_WAIT_PICK = 2;
	private static final int ORDER_STATUS_WAIT_OUTBOUND = 3;
	private static final int ORDER_STATUS_COMPLETED = 4;
	private static final int ORDER_STATUS_CANCELLED = 5;

	private static final DateTimeFormatter OUTBOUND_TASK_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final OutboundTaskRepository outboundTaskRepository;
	private final OutboundOrderRepository outboundOrderRepository;
	private final WarehouseZoneRepository warehouseZoneRepository;
	private final WarehouseLocationRepository warehouseLocationRepository;
	private final InventoryStockRepository inventoryStockRepository;
	private final InventoryStockService inventoryStockService;
	private final OutboundRecordService outboundRecordService;

	public OutboundTaskService(
			OutboundTaskRepository outboundTaskRepository,
			OutboundOrderRepository outboundOrderRepository,
			WarehouseZoneRepository warehouseZoneRepository,
			WarehouseLocationRepository warehouseLocationRepository,
			InventoryStockRepository inventoryStockRepository,
			InventoryStockService inventoryStockService,
			OutboundRecordService outboundRecordService) {
		this.outboundTaskRepository = outboundTaskRepository;
		this.outboundOrderRepository = outboundOrderRepository;
		this.warehouseZoneRepository = warehouseZoneRepository;
		this.warehouseLocationRepository = warehouseLocationRepository;
		this.inventoryStockRepository = inventoryStockRepository;
		this.inventoryStockService = inventoryStockService;
		this.outboundRecordService = outboundRecordService;
	}

	public List<OutboundTaskListItemResponse> getOutboundTaskList(OutboundTaskListQueryRequest queryRequest) {
		return outboundTaskRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public OutboundTaskDetailResponse getOutboundTaskDetail(Long id) {
		return toDetailResponse(getExistingTask(id));
	}

	public List<OutboundTaskStockOptionResponse> getStockOptions(Long id) {
		OutboundTaskEntity task = getExistingTask(id);
		return inventoryStockRepository.findAvailableStocks(task.productId(), task.warehouseId(), task.id())
				.stream()
				.map(this::toStockOptionResponse)
				.toList();
	}

	@Transactional
	public void createTasksForOutboundOrder(OutboundOrderEntity order, List<OutboundOrderItemEntity> items) {
		if (outboundTaskRepository.existsByOutboundOrderId(order.id())) {
			return;
		}

		List<OutboundTaskEntity> tasks = items.stream()
				.map(item -> new OutboundTaskEntity(
						null,
						generateTaskCode(),
						order.id(),
						order.orderCode(),
						item.id(),
						order.customerId(),
						order.customerName(),
						order.warehouseId(),
						order.warehouseName(),
						null,
						null,
						null,
						null,
						item.productId(),
						item.productCode(),
						item.productName(),
						item.quantity(),
						STATUS_WAIT_ASSIGN,
						item.remarks(),
						null,
						null,
						null,
						null))
				.toList();

		outboundTaskRepository.insertTasks(tasks);
	}

	@Transactional
	public OutboundTaskDetailResponse assignStock(Long id, OutboundAssignRequest request) {
		OutboundTaskEntity task = getExistingTask(id);
		ensureAssignable(task.status());

		WarehouseZoneEntity zone = warehouseZoneRepository.findById(request.zoneId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_ZONE_NOT_FOUND));
		WarehouseLocationEntity location = warehouseLocationRepository.findById(request.locationId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_NOT_FOUND));

		if (!Objects.equals(zone.warehouseId(), task.warehouseId())
				|| !Objects.equals(location.zoneId(), zone.id())
				|| !Objects.equals(location.warehouseId(), task.warehouseId())) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_ZONE_MISMATCH);
		}

		InventoryLocationStockEntity stockOption = inventoryStockRepository
				.findAvailableStocks(task.productId(), task.warehouseId(), task.id())
				.stream()
				.filter(option -> Objects.equals(option.zoneId(), zone.id())
						&& Objects.equals(option.locationId(), location.id()))
				.findFirst()
				.orElseThrow(() -> new BusinessException(CommonErrorCode.OUTBOUND_TASK_STOCK_INSUFFICIENT));

		if (stockOption.availableQuantity().compareTo(task.quantity()) < 0) {
			throw new BusinessException(CommonErrorCode.OUTBOUND_TASK_STOCK_INSUFFICIENT);
		}

		outboundTaskRepository.assignLocation(id, zone.id(), location.id(), STATUS_WAIT_PICK);
		syncOrderStatus(task.outboundOrderId());
		return getOutboundTaskDetail(id);
	}

	@Transactional
	public void confirmPick(Long id) {
		OutboundTaskEntity task = getExistingTask(id);

		if (!Objects.equals(task.status(), STATUS_WAIT_PICK)
				|| task.zoneId() == null
				|| task.locationId() == null) {
			throw new BusinessException(CommonErrorCode.OUTBOUND_TASK_STATUS_INVALID);
		}

		outboundTaskRepository.updatePicked(id, STATUS_WAIT_OUTBOUND, LocalDateTime.now());
		syncOrderStatus(task.outboundOrderId());
	}

	@Transactional
	public void completeTask(Long id) {
		OutboundTaskEntity task = getExistingTask(id);

		if (!Objects.equals(task.status(), STATUS_WAIT_OUTBOUND)
				|| task.zoneId() == null
				|| task.locationId() == null) {
			throw new BusinessException(CommonErrorCode.OUTBOUND_TASK_STATUS_INVALID);
		}

		outboundTaskRepository.updateCompleted(id, STATUS_COMPLETED, LocalDateTime.now());
		OutboundTaskEntity updatedTask = getExistingTask(id);
		inventoryStockService.recordOutbound(updatedTask);
		outboundRecordService.createRecord(updatedTask);
		syncOrderStatus(task.outboundOrderId());
	}

	@Transactional
	public void cancelTask(Long id) {
		OutboundTaskEntity task = getExistingTask(id);

		if (Objects.equals(task.status(), STATUS_COMPLETED) || Objects.equals(task.status(), STATUS_CANCELLED)) {
			throw new BusinessException(CommonErrorCode.OUTBOUND_TASK_STATUS_INVALID);
		}

		outboundTaskRepository.updateCancelled(id, STATUS_CANCELLED);
		syncOrderStatus(task.outboundOrderId());
	}

	private OutboundTaskEntity getExistingTask(Long id) {
		return outboundTaskRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.OUTBOUND_TASK_NOT_FOUND));
	}

	private boolean matchesQuery(OutboundTaskEntity entity, OutboundTaskListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.taskCode())
				&& !entity.taskCode().contains(queryRequest.taskCode().trim())) {
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

	private void ensureAssignable(Integer status) {
		if (!Objects.equals(status, STATUS_WAIT_ASSIGN) && !Objects.equals(status, STATUS_WAIT_PICK)) {
			throw new BusinessException(CommonErrorCode.OUTBOUND_TASK_STATUS_INVALID);
		}
	}

	private void syncOrderStatus(Long outboundOrderId) {
		List<OutboundTaskEntity> tasks = outboundTaskRepository.findByOutboundOrderId(outboundOrderId);
		List<OutboundTaskEntity> activeTasks = tasks.stream()
				.filter(task -> !Objects.equals(task.status(), STATUS_CANCELLED))
				.toList();

		if (activeTasks.isEmpty()) {
			outboundOrderRepository.updateStatus(outboundOrderId, ORDER_STATUS_CANCELLED, null);
			return;
		}

		boolean allCompleted = activeTasks.stream().allMatch(task -> Objects.equals(task.status(), STATUS_COMPLETED));

		if (allCompleted) {
			LocalDateTime completedAt = activeTasks.stream()
					.map(OutboundTaskEntity::completedAt)
					.filter(Objects::nonNull)
					.max(LocalDateTime::compareTo)
					.orElse(LocalDateTime.now());
			outboundOrderRepository.updateStatus(outboundOrderId, ORDER_STATUS_COMPLETED, completedAt);
			return;
		}

		boolean hasWaitingAssignOrPick = activeTasks.stream()
				.anyMatch(task -> Objects.equals(task.status(), STATUS_WAIT_ASSIGN)
						|| Objects.equals(task.status(), STATUS_WAIT_PICK));

		if (hasWaitingAssignOrPick) {
			outboundOrderRepository.updateStatus(outboundOrderId, ORDER_STATUS_WAIT_PICK, null);
			return;
		}

		outboundOrderRepository.updateStatus(outboundOrderId, ORDER_STATUS_WAIT_OUTBOUND, null);
	}

	private String generateTaskCode() {
		for (int index = 0; index < 20; index += 1) {
			String taskCode = "OT-" + LocalDateTime.now().format(OUTBOUND_TASK_CODE_FORMATTER);

			if (index > 0) {
				taskCode += "-" + index;
			}

			if (!outboundTaskRepository.existsByTaskCode(taskCode)) {
				return taskCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "拣货任务编号生成失败，请稍后重试");
	}

	private OutboundTaskListItemResponse toListItemResponse(OutboundTaskEntity entity) {
		return new OutboundTaskListItemResponse(
				entity.id(),
				entity.taskCode(),
				entity.outboundOrderId(),
				entity.outboundOrderCode(),
				entity.customerId(),
				entity.customerName(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				OutboundTaskRepository.toDouble(entity.quantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.pickedAt()),
				toIsoDateTime(entity.completedAt()),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private OutboundTaskDetailResponse toDetailResponse(OutboundTaskEntity entity) {
		return new OutboundTaskDetailResponse(
				entity.id(),
				entity.taskCode(),
				entity.outboundOrderId(),
				entity.outboundOrderCode(),
				entity.customerId(),
				entity.customerName(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				OutboundTaskRepository.toDouble(entity.quantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.pickedAt()),
				toIsoDateTime(entity.completedAt()),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private OutboundTaskStockOptionResponse toStockOptionResponse(InventoryLocationStockEntity entity) {
		return new OutboundTaskStockOptionResponse(
				entity.warehouseId(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				toDouble(entity.stockQuantity()),
				toDouble(entity.reservedQuantity()),
				toDouble(entity.availableQuantity()));
	}

	private String toStatusLabel(Integer status) {
		return switch (status) {
			case STATUS_WAIT_ASSIGN -> "待分配";
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
}
