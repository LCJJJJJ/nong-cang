package com.nongcang.server.modules.putawaytask.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.inboundorder.domain.entity.InboundOrderEntity;
import com.nongcang.server.modules.inboundorder.domain.entity.InboundOrderItemEntity;
import com.nongcang.server.modules.inboundorder.repository.InboundOrderRepository;
import com.nongcang.server.modules.inboundrecord.service.InboundRecordService;
import com.nongcang.server.modules.inventorysupport.service.InventoryStockService;
import com.nongcang.server.modules.putawaytask.domain.dto.PutawayAssignRequest;
import com.nongcang.server.modules.putawaytask.domain.dto.PutawayTaskListQueryRequest;
import com.nongcang.server.modules.putawaytask.domain.entity.PutawayTaskEntity;
import com.nongcang.server.modules.putawaytask.domain.vo.PutawayTaskDetailResponse;
import com.nongcang.server.modules.putawaytask.domain.vo.PutawayTaskListItemResponse;
import com.nongcang.server.modules.putawaytask.repository.PutawayTaskRepository;
import com.nongcang.server.modules.warehouselocation.domain.entity.WarehouseLocationEntity;
import com.nongcang.server.modules.warehouselocation.repository.WarehouseLocationRepository;
import com.nongcang.server.modules.warehousezone.domain.entity.WarehouseZoneEntity;
import com.nongcang.server.modules.warehousezone.repository.WarehouseZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PutawayTaskService {

	private static final int STATUS_WAIT_ASSIGN = 1;
	private static final int STATUS_WAIT_PUTAWAY = 2;
	private static final int STATUS_COMPLETED = 3;
	private static final int STATUS_CANCELLED = 4;

	private static final int ORDER_STATUS_COMPLETED = 3;

	private static final DateTimeFormatter PUTAWAY_TASK_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final PutawayTaskRepository putawayTaskRepository;
	private final InboundOrderRepository inboundOrderRepository;
	private final WarehouseZoneRepository warehouseZoneRepository;
	private final WarehouseLocationRepository warehouseLocationRepository;
	private final InventoryStockService inventoryStockService;
	private final InboundRecordService inboundRecordService;

	public PutawayTaskService(
			PutawayTaskRepository putawayTaskRepository,
			InboundOrderRepository inboundOrderRepository,
			WarehouseZoneRepository warehouseZoneRepository,
			WarehouseLocationRepository warehouseLocationRepository,
			InventoryStockService inventoryStockService,
			InboundRecordService inboundRecordService) {
		this.putawayTaskRepository = putawayTaskRepository;
		this.inboundOrderRepository = inboundOrderRepository;
		this.warehouseZoneRepository = warehouseZoneRepository;
		this.warehouseLocationRepository = warehouseLocationRepository;
		this.inventoryStockService = inventoryStockService;
		this.inboundRecordService = inboundRecordService;
	}

	public List<PutawayTaskListItemResponse> getPutawayTaskList(PutawayTaskListQueryRequest queryRequest) {
		return putawayTaskRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public PutawayTaskDetailResponse getPutawayTaskDetail(Long id) {
		return toDetailResponse(getExistingTask(id));
	}

	@Transactional
	public void createTasksForInboundOrder(InboundOrderEntity order, List<InboundOrderItemEntity> items) {
		if (putawayTaskRepository.existsByInboundOrderId(order.id())) {
			return;
		}

		List<PutawayTaskEntity> tasks = items.stream()
				.map(item -> new PutawayTaskEntity(
						null,
						generateTaskCode(),
						order.id(),
						order.orderCode(),
						item.id(),
						order.supplierId(),
						order.supplierName(),
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
						null))
				.toList();

		putawayTaskRepository.insertTasks(tasks);
	}

	@Transactional
	public PutawayTaskDetailResponse assignLocation(Long id, PutawayAssignRequest request) {
		PutawayTaskEntity task = getExistingTask(id);
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

		putawayTaskRepository.assignLocation(id, zone.id(), location.id(), STATUS_WAIT_PUTAWAY);
		return getPutawayTaskDetail(id);
	}

	@Transactional
	public void completeTask(Long id) {
		PutawayTaskEntity task = getExistingTask(id);

		if (!Objects.equals(task.status(), STATUS_WAIT_PUTAWAY)
				|| task.zoneId() == null
				|| task.locationId() == null) {
			throw new BusinessException(CommonErrorCode.PUTAWAY_TASK_STATUS_INVALID);
		}

		putawayTaskRepository.updateStatus(id, STATUS_COMPLETED, LocalDateTime.now());
		PutawayTaskEntity updatedTask = getExistingTask(id);
		inventoryStockService.recordInbound(updatedTask);
		inboundRecordService.createRecord(updatedTask);

		if (putawayTaskRepository.countOpenTasksByOrderId(task.inboundOrderId()) == 0) {
			InboundOrderEntity order = inboundOrderRepository.findById(task.inboundOrderId())
					.orElseThrow(() -> new BusinessException(CommonErrorCode.INBOUND_ORDER_NOT_FOUND));
			inboundOrderRepository.updateStatus(order.id(), ORDER_STATUS_COMPLETED, order.actualArrivalAt());
		}
	}

	@Transactional
	public void cancelTask(Long id) {
		PutawayTaskEntity task = getExistingTask(id);

		if (Objects.equals(task.status(), STATUS_COMPLETED) || Objects.equals(task.status(), STATUS_CANCELLED)) {
			throw new BusinessException(CommonErrorCode.PUTAWAY_TASK_STATUS_INVALID);
		}

		putawayTaskRepository.updateStatus(id, STATUS_CANCELLED, task.completedAt());
	}

	private PutawayTaskEntity getExistingTask(Long id) {
		return putawayTaskRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PUTAWAY_TASK_NOT_FOUND));
	}

	private boolean matchesQuery(PutawayTaskEntity entity, PutawayTaskListQueryRequest queryRequest) {
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
		if (!Objects.equals(status, STATUS_WAIT_ASSIGN) && !Objects.equals(status, STATUS_WAIT_PUTAWAY)) {
			throw new BusinessException(CommonErrorCode.PUTAWAY_TASK_STATUS_INVALID);
		}
	}

	private String generateTaskCode() {
		for (int index = 0; index < 20; index += 1) {
			String taskCode = "PT-" + LocalDateTime.now().format(PUTAWAY_TASK_CODE_FORMATTER);

			if (index > 0) {
				taskCode += "-" + index;
			}

			if (!putawayTaskRepository.existsByTaskCode(taskCode)) {
				return taskCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "上架任务编号生成失败，请稍后重试");
	}

	private PutawayTaskListItemResponse toListItemResponse(PutawayTaskEntity entity) {
		return new PutawayTaskListItemResponse(
				entity.id(),
				entity.taskCode(),
				entity.inboundOrderId(),
				entity.inboundOrderCode(),
				entity.supplierId(),
				entity.supplierName(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				PutawayTaskRepository.toDouble(entity.quantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.completedAt()),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private PutawayTaskDetailResponse toDetailResponse(PutawayTaskEntity entity) {
		return new PutawayTaskDetailResponse(
				entity.id(),
				entity.taskCode(),
				entity.inboundOrderId(),
				entity.inboundOrderCode(),
				entity.supplierId(),
				entity.supplierName(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				PutawayTaskRepository.toDouble(entity.quantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.completedAt()),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String toStatusLabel(Integer status) {
		return switch (status) {
			case STATUS_WAIT_ASSIGN -> "待分配";
			case STATUS_WAIT_PUTAWAY -> "待上架";
			case STATUS_COMPLETED -> "已完成";
			case STATUS_CANCELLED -> "已取消";
			default -> "未知状态";
		};
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}
}
