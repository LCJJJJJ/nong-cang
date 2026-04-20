package com.nongcang.server.modules.outboundrecord.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.nongcang.server.common.security.WarehouseAccessScopeService;
import com.nongcang.server.modules.outboundrecord.domain.dto.OutboundRecordListQueryRequest;
import com.nongcang.server.modules.outboundrecord.domain.entity.OutboundRecordEntity;
import com.nongcang.server.modules.outboundrecord.domain.vo.OutboundRecordListItemResponse;
import com.nongcang.server.modules.outboundrecord.repository.OutboundRecordRepository;
import com.nongcang.server.modules.outboundtask.domain.entity.OutboundTaskEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OutboundRecordService {

	private static final DateTimeFormatter OUTBOUND_RECORD_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final OutboundRecordRepository outboundRecordRepository;
	private final WarehouseAccessScopeService warehouseAccessScopeService;

	public OutboundRecordService(
			OutboundRecordRepository outboundRecordRepository,
			WarehouseAccessScopeService warehouseAccessScopeService) {
		this.outboundRecordRepository = outboundRecordRepository;
		this.warehouseAccessScopeService = warehouseAccessScopeService;
	}

	public List<OutboundRecordListItemResponse> getOutboundRecordList(OutboundRecordListQueryRequest queryRequest) {
		Long scopedWarehouseId = warehouseAccessScopeService.resolveQueryWarehouseId(queryRequest.warehouseId());
		return outboundRecordRepository.findAll()
				.stream()
				.filter(entity -> scopedWarehouseId == null || entity.warehouseId().equals(scopedWarehouseId))
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	@Transactional
	public void createRecord(OutboundTaskEntity task) {
		outboundRecordRepository.insertRecord(
				generateRecordCode(),
				task.outboundOrderId(),
				task.id(),
				task.customerId(),
				task.warehouseId(),
				task.zoneId(),
				task.locationId(),
				task.productId(),
				task.quantity(),
				task.completedAt() == null ? LocalDateTime.now() : task.completedAt(),
				task.remarks());
	}

	private boolean matchesQuery(OutboundRecordEntity entity, OutboundRecordListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.recordCode())
				&& !entity.recordCode().contains(queryRequest.recordCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.orderCode())
				&& !entity.outboundOrderCode().contains(queryRequest.orderCode().trim())) {
			return false;
		}

		if (queryRequest.warehouseId() != null && !queryRequest.warehouseId().equals(entity.warehouseId())) {
			return false;
		}

		if (queryRequest.productId() != null && !queryRequest.productId().equals(entity.productId())) {
			return false;
		}

		return true;
	}

	private String generateRecordCode() {
		for (int index = 0; index < 20; index += 1) {
			String recordCode = "OR-" + LocalDateTime.now().format(OUTBOUND_RECORD_CODE_FORMATTER);

			if (index > 0) {
				recordCode += "-" + index;
			}

			if (!outboundRecordRepository.existsByRecordCode(recordCode)) {
				return recordCode;
			}
		}

		return "OR-" + LocalDateTime.now().format(OUTBOUND_RECORD_CODE_FORMATTER) + "-F";
	}

	private OutboundRecordListItemResponse toListItemResponse(OutboundRecordEntity entity) {
		return new OutboundRecordListItemResponse(
				entity.id(),
				entity.recordCode(),
				entity.outboundOrderId(),
				entity.outboundOrderCode(),
				entity.outboundTaskId(),
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
				OutboundRecordRepository.toDouble(entity.quantity()),
				toIsoDateTime(entity.occurredAt()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()));
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}
}
