package com.nongcang.server.modules.inboundrecord.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.nongcang.server.common.security.WarehouseAccessScopeService;
import com.nongcang.server.modules.inboundrecord.domain.dto.InboundRecordListQueryRequest;
import com.nongcang.server.modules.inboundrecord.domain.entity.InboundRecordEntity;
import com.nongcang.server.modules.inboundrecord.domain.vo.InboundRecordListItemResponse;
import com.nongcang.server.modules.inboundrecord.repository.InboundRecordRepository;
import com.nongcang.server.modules.productarchive.domain.entity.ProductArchiveEntity;
import com.nongcang.server.modules.productarchive.repository.ProductArchiveRepository;
import com.nongcang.server.modules.putawaytask.domain.entity.PutawayTaskEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class InboundRecordService {

	private static final DateTimeFormatter INBOUND_RECORD_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final InboundRecordRepository inboundRecordRepository;
	private final ProductArchiveRepository productArchiveRepository;
	private final WarehouseAccessScopeService warehouseAccessScopeService;

	public InboundRecordService(
			InboundRecordRepository inboundRecordRepository,
			ProductArchiveRepository productArchiveRepository,
			WarehouseAccessScopeService warehouseAccessScopeService) {
		this.inboundRecordRepository = inboundRecordRepository;
		this.productArchiveRepository = productArchiveRepository;
		this.warehouseAccessScopeService = warehouseAccessScopeService;
	}

	public List<InboundRecordListItemResponse> getInboundRecordList(InboundRecordListQueryRequest queryRequest) {
		Long scopedWarehouseId = warehouseAccessScopeService.resolveQueryWarehouseId(queryRequest.warehouseId());
		return inboundRecordRepository.findAll()
				.stream()
				.filter(entity -> scopedWarehouseId == null || entity.warehouseId().equals(scopedWarehouseId))
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	@Transactional
	public InboundRecordEntity createRecord(PutawayTaskEntity task) {
		ProductArchiveEntity productArchive = productArchiveRepository.findById(task.productId())
				.orElseThrow();
		LocalDateTime occurredAt = task.completedAt() == null ? LocalDateTime.now() : task.completedAt();
		long inboundRecordId = inboundRecordRepository.insertRecord(
				generateRecordCode(),
				task.inboundOrderId(),
				task.id(),
				task.supplierId(),
				task.warehouseId(),
				task.zoneId(),
				task.locationId(),
				task.productId(),
				task.quantity(),
				productArchive.shelfLifeDays(),
				productArchive.warningDays(),
				occurredAt.plusDays(productArchive.shelfLifeDays()),
				occurredAt,
				task.remarks());
		return inboundRecordRepository.findById(inboundRecordId)
				.orElseThrow();
	}

	private boolean matchesQuery(InboundRecordEntity entity, InboundRecordListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.recordCode())
				&& !entity.recordCode().contains(queryRequest.recordCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.orderCode())
				&& !entity.inboundOrderCode().contains(queryRequest.orderCode().trim())) {
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
			String recordCode = "IR-" + LocalDateTime.now().format(INBOUND_RECORD_CODE_FORMATTER);

			if (index > 0) {
				recordCode += "-" + index;
			}

			if (!inboundRecordRepository.existsByRecordCode(recordCode)) {
				return recordCode;
			}
		}

		return "IR-" + LocalDateTime.now().format(INBOUND_RECORD_CODE_FORMATTER) + "-F";
	}

	private InboundRecordListItemResponse toListItemResponse(InboundRecordEntity entity) {
		return new InboundRecordListItemResponse(
				entity.id(),
				entity.recordCode(),
				entity.inboundOrderId(),
				entity.inboundOrderCode(),
				entity.putawayTaskId(),
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
				InboundRecordRepository.toDouble(entity.quantity()),
				entity.shelfLifeDaysSnapshot(),
				entity.warningDaysSnapshot(),
				toIsoDateTime(entity.expectedExpireAt()),
				toIsoDateTime(entity.occurredAt()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()));
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}
}
