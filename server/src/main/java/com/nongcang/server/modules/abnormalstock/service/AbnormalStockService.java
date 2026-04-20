package com.nongcang.server.modules.abnormalstock.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.common.security.WarehouseAccessScopeService;
import com.nongcang.server.modules.inventorysupport.service.InventoryBatchService;
import com.nongcang.server.modules.abnormalstock.domain.dto.AbnormalStockLossRequest;
import com.nongcang.server.modules.abnormalstock.domain.dto.AbnormalStockListQueryRequest;
import com.nongcang.server.modules.abnormalstock.domain.entity.AbnormalStockEntity;
import com.nongcang.server.modules.abnormalstock.domain.vo.AbnormalStockDetailResponse;
import com.nongcang.server.modules.abnormalstock.domain.vo.AbnormalStockListItemResponse;
import com.nongcang.server.modules.abnormalstock.domain.vo.AbnormalStockOptionResponse;
import com.nongcang.server.modules.lossrecord.service.LossRecordService;
import com.nongcang.server.modules.abnormalstock.repository.AbnormalStockRepository;
import com.nongcang.server.modules.qualityinspection.domain.vo.QualityInspectionDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AbnormalStockService {

	private static final int STATUS_LOCKED = 1;
	private static final int STATUS_RELEASED = 2;
	private static final int STATUS_DISPOSED = 3;
	private static final DateTimeFormatter ABNORMAL_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final AbnormalStockRepository abnormalStockRepository;
	private final LossRecordService lossRecordService;
	private final InventoryBatchService inventoryBatchService;
	private final WarehouseAccessScopeService warehouseAccessScopeService;

	public AbnormalStockService(
			AbnormalStockRepository abnormalStockRepository,
			LossRecordService lossRecordService,
			InventoryBatchService inventoryBatchService,
			WarehouseAccessScopeService warehouseAccessScopeService) {
		this.abnormalStockRepository = abnormalStockRepository;
		this.lossRecordService = lossRecordService;
		this.inventoryBatchService = inventoryBatchService;
		this.warehouseAccessScopeService = warehouseAccessScopeService;
	}

	public List<AbnormalStockListItemResponse> getAbnormalStockList(AbnormalStockListQueryRequest queryRequest) {
		Long scopedWarehouseId = warehouseAccessScopeService.resolveQueryWarehouseId(queryRequest.warehouseId());
		return abnormalStockRepository.findAll()
				.stream()
				.filter(entity -> scopedWarehouseId == null || Objects.equals(entity.warehouseId(), scopedWarehouseId))
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<AbnormalStockOptionResponse> getAbnormalStockOptions() {
		Long scopedWarehouseId = warehouseAccessScopeService.currentWarehouseIdOrNull();
		return abnormalStockRepository.findAll()
				.stream()
				.filter(entity -> scopedWarehouseId == null || Objects.equals(entity.warehouseId(), scopedWarehouseId))
				.filter(entity -> Objects.equals(entity.status(), STATUS_LOCKED))
				.map(entity -> new AbnormalStockOptionResponse(
						entity.id(),
						entity.productName() + " / " + entity.abnormalCode(),
						AbnormalStockRepository.toDouble(entity.lockedQuantity()),
						entity.status()))
				.toList();
	}

	public AbnormalStockDetailResponse getAbnormalStockDetail(Long id) {
		AbnormalStockEntity abnormalStock = getExistingAbnormalStock(id);
		warehouseAccessScopeService.assertWarehouseAccess(abnormalStock.warehouseId());
		return toDetailResponse(abnormalStock);
	}

	@Transactional
	public void createFromInspection(QualityInspectionDetailResponse inspectionDetail) {
		long abnormalStockId = abnormalStockRepository.insert(
				generateAbnormalCode(),
				inspectionDetail.id(),
				inspectionDetail.inspectionCode(),
				inspectionDetail.productId(),
				inspectionDetail.warehouseId(),
				inspectionDetail.zoneId(),
				inspectionDetail.locationId(),
				java.math.BigDecimal.valueOf(inspectionDetail.unqualifiedQuantity()),
				STATUS_LOCKED,
				inspectionDetail.resultStatusLabel(),
				inspectionDetail.remarks());
		inventoryBatchService.lockForInspection(
				abnormalStockId,
				inspectionDetail.sourceType(),
				inspectionDetail.sourceId(),
				inspectionDetail.productId(),
				inspectionDetail.warehouseId(),
				inspectionDetail.locationId(),
				java.math.BigDecimal.valueOf(inspectionDetail.unqualifiedQuantity()));
	}

	@Transactional
	public void release(Long id) {
		AbnormalStockEntity abnormalStock = getExistingAbnormalStock(id);
		warehouseAccessScopeService.assertWarehouseAccess(abnormalStock.warehouseId());

		if (!Objects.equals(abnormalStock.status(), STATUS_LOCKED)) {
			throw new BusinessException(CommonErrorCode.ABNORMAL_STOCK_STATUS_INVALID);
		}

		abnormalStockRepository.updateStatus(id, STATUS_RELEASED, LocalDateTime.now());
	}

	@Transactional
	public void disposeToLoss(Long id, AbnormalStockLossRequest request) {
		AbnormalStockEntity abnormalStock = getExistingAbnormalStock(id);
		warehouseAccessScopeService.assertWarehouseAccess(abnormalStock.warehouseId());

		if (!Objects.equals(abnormalStock.status(), STATUS_LOCKED)) {
			throw new BusinessException(CommonErrorCode.ABNORMAL_STOCK_STATUS_INVALID);
		}

		lossRecordService.createFromAbnormalStock(
				abnormalStock,
				request.lossReason().trim(),
				StringUtils.hasText(request.remarks()) ? request.remarks().trim() : null);
		abnormalStockRepository.updateStatus(id, STATUS_DISPOSED, LocalDateTime.now());
	}

	public AbnormalStockEntity getExistingAbnormalStock(Long id) {
		return abnormalStockRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.ABNORMAL_STOCK_NOT_FOUND));
	}

	private boolean matchesQuery(AbnormalStockEntity entity, AbnormalStockListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.abnormalCode())
				&& !entity.abnormalCode().contains(queryRequest.abnormalCode().trim())) {
			return false;
		}

		if (queryRequest.productId() != null && !Objects.equals(entity.productId(), queryRequest.productId())) {
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

	private String generateAbnormalCode() {
		for (int index = 0; index < 20; index += 1) {
			String abnormalCode = "ABN-" + LocalDateTime.now().format(ABNORMAL_CODE_FORMATTER);

			if (index > 0) {
				abnormalCode += "-" + index;
			}

			if (!abnormalStockRepository.existsByAbnormalCode(abnormalCode)) {
				return abnormalCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "异常库存编号生成失败，请稍后重试");
	}

	private AbnormalStockListItemResponse toListItemResponse(AbnormalStockEntity entity) {
		return new AbnormalStockListItemResponse(
				entity.id(),
				entity.abnormalCode(),
				entity.qualityInspectionId(),
				entity.inspectionCode(),
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
				AbnormalStockRepository.toDouble(entity.lockedQuantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.reason(),
				entity.remarks(),
				toIsoDateTime(entity.processedAt()),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private AbnormalStockDetailResponse toDetailResponse(AbnormalStockEntity entity) {
		return new AbnormalStockDetailResponse(
				entity.id(),
				entity.abnormalCode(),
				entity.qualityInspectionId(),
				entity.inspectionCode(),
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
				AbnormalStockRepository.toDouble(entity.lockedQuantity()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.reason(),
				entity.remarks(),
				toIsoDateTime(entity.processedAt()),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String toStatusLabel(Integer status) {
		return switch (status) {
			case STATUS_LOCKED -> "锁定中";
			case STATUS_RELEASED -> "已释放";
			case STATUS_DISPOSED -> "已转损耗";
			default -> "未知状态";
		};
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}
}
