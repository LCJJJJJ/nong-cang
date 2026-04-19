package com.nongcang.server.modules.qualityinspection.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.common.validation.QuantityPrecisionValidator;
import com.nongcang.server.modules.abnormalstock.service.AbnormalStockService;
import com.nongcang.server.modules.inboundrecord.domain.entity.InboundRecordEntity;
import com.nongcang.server.modules.inboundrecord.repository.InboundRecordRepository;
import com.nongcang.server.modules.inventorystock.domain.entity.InventoryStockEntity;
import com.nongcang.server.modules.inventorystock.repository.InventoryStockQueryRepository;
import com.nongcang.server.modules.productarchive.repository.ProductArchiveRepository;
import com.nongcang.server.modules.qualityinspection.domain.dto.QualityInspectionCreateRequest;
import com.nongcang.server.modules.qualityinspection.domain.dto.QualityInspectionListQueryRequest;
import com.nongcang.server.modules.qualityinspection.domain.entity.QualityInspectionEntity;
import com.nongcang.server.modules.qualityinspection.domain.vo.QualityInspectionDetailResponse;
import com.nongcang.server.modules.qualityinspection.domain.vo.QualityInspectionListItemResponse;
import com.nongcang.server.modules.qualityinspection.repository.QualityInspectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class QualityInspectionService {

	private static final String SOURCE_INBOUND_RECORD = "INBOUND_RECORD";
	private static final String SOURCE_INVENTORY_STOCK = "INVENTORY_STOCK";
	private static final int RESULT_QUALIFIED = 1;
	private static final int RESULT_PARTIAL = 2;
	private static final int RESULT_UNQUALIFIED = 3;
	private static final DateTimeFormatter INSPECTION_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final QualityInspectionRepository qualityInspectionRepository;
	private final InboundRecordRepository inboundRecordRepository;
	private final InventoryStockQueryRepository inventoryStockQueryRepository;
	private final ProductArchiveRepository productArchiveRepository;
	private final QuantityPrecisionValidator quantityPrecisionValidator;
	private final AbnormalStockService abnormalStockService;

	public QualityInspectionService(
			QualityInspectionRepository qualityInspectionRepository,
			InboundRecordRepository inboundRecordRepository,
			InventoryStockQueryRepository inventoryStockQueryRepository,
			ProductArchiveRepository productArchiveRepository,
			QuantityPrecisionValidator quantityPrecisionValidator,
			AbnormalStockService abnormalStockService) {
		this.qualityInspectionRepository = qualityInspectionRepository;
		this.inboundRecordRepository = inboundRecordRepository;
		this.inventoryStockQueryRepository = inventoryStockQueryRepository;
		this.productArchiveRepository = productArchiveRepository;
		this.quantityPrecisionValidator = quantityPrecisionValidator;
		this.abnormalStockService = abnormalStockService;
	}

	public List<QualityInspectionListItemResponse> getQualityInspectionList(
			QualityInspectionListQueryRequest queryRequest) {
		return qualityInspectionRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public QualityInspectionDetailResponse getQualityInspectionDetail(Long id) {
		return toDetailResponse(getExistingInspection(id));
	}

	@Transactional
	public QualityInspectionDetailResponse createQualityInspection(QualityInspectionCreateRequest request) {
		InspectionSourceSnapshot sourceSnapshot = resolveSource(normalizeSourceType(request.sourceType()), request.sourceId());
		BigDecimal alreadyInspected = qualityInspectionRepository.sumInspectQuantityBySource(
				sourceSnapshot.sourceType(),
				sourceSnapshot.sourceId());
		BigDecimal remainingInspectable = sourceSnapshot.maxInspectableQuantity().subtract(alreadyInspected);

		if (request.inspectQuantity() == null || request.inspectQuantity().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException(CommonErrorCode.QUALITY_INSPECTION_QUANTITY_INVALID);
		}

		if (remainingInspectable.compareTo(request.inspectQuantity()) < 0) {
			throw new BusinessException(CommonErrorCode.QUALITY_INSPECTION_SOURCE_INSUFFICIENT);
		}

		if (request.unqualifiedQuantity() == null
				|| request.unqualifiedQuantity().compareTo(BigDecimal.ZERO) < 0
				|| request.unqualifiedQuantity().compareTo(request.inspectQuantity()) > 0) {
			throw new BusinessException(CommonErrorCode.QUALITY_INSPECTION_RESULT_INVALID);
		}

		quantityPrecisionValidator.validate(
				request.inspectQuantity(),
				sourceSnapshot.precisionDigits(),
				sourceSnapshot.unitName());
		quantityPrecisionValidator.validate(
				request.unqualifiedQuantity(),
				sourceSnapshot.precisionDigits(),
				sourceSnapshot.unitName());

		BigDecimal qualifiedQuantity = request.inspectQuantity().subtract(request.unqualifiedQuantity());
		Integer resultStatus = deriveResultStatus(request.inspectQuantity(), request.unqualifiedQuantity());

		long inspectionId = qualityInspectionRepository.insert(
				generateInspectionCode(),
				sourceSnapshot.sourceType(),
				sourceSnapshot.sourceId(),
				sourceSnapshot.sourceCode(),
				sourceSnapshot.sourceLabel(),
				sourceSnapshot.productId(),
				sourceSnapshot.warehouseId(),
				sourceSnapshot.zoneId(),
				sourceSnapshot.locationId(),
				request.inspectQuantity(),
				qualifiedQuantity,
				request.unqualifiedQuantity(),
				resultStatus,
				trimToNull(request.remarks()));

		QualityInspectionDetailResponse detail = getQualityInspectionDetail(inspectionId);

		if (request.unqualifiedQuantity().compareTo(BigDecimal.ZERO) > 0) {
			abnormalStockService.createFromInspection(detail);
		}

		return detail;
	}

	private QualityInspectionEntity getExistingInspection(Long id) {
		return qualityInspectionRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.QUALITY_INSPECTION_NOT_FOUND));
	}

	private boolean matchesQuery(QualityInspectionEntity entity, QualityInspectionListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.inspectionCode())
				&& !entity.inspectionCode().contains(queryRequest.inspectionCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.sourceType())
				&& !entity.sourceType().equals(queryRequest.sourceType().trim().toUpperCase())) {
			return false;
		}

		if (queryRequest.productId() != null && !Objects.equals(entity.productId(), queryRequest.productId())) {
			return false;
		}

		if (queryRequest.resultStatus() != null && !Objects.equals(entity.resultStatus(), queryRequest.resultStatus())) {
			return false;
		}

		return true;
	}

	private InspectionSourceSnapshot resolveSource(String sourceType, Long sourceId) {
		return switch (sourceType) {
			case SOURCE_INBOUND_RECORD -> inboundRecordRepository.findById(sourceId)
					.map(this::toInboundRecordSnapshot)
					.orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));
			case SOURCE_INVENTORY_STOCK -> inventoryStockQueryRepository.findById(sourceId)
					.map(this::toInventoryStockSnapshot)
					.orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));
			default -> throw new BusinessException(CommonErrorCode.QUALITY_INSPECTION_SOURCE_INVALID);
		};
	}

	private InspectionSourceSnapshot toInboundRecordSnapshot(InboundRecordEntity entity) {
		var productArchive = productArchiveRepository.findById(entity.productId())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ARCHIVE_NOT_FOUND));
		return new InspectionSourceSnapshot(
				SOURCE_INBOUND_RECORD,
				entity.id(),
				entity.recordCode(),
				entity.productName() + " / " + entity.recordCode(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				productArchive.unitName(),
				productArchive.precisionDigits(),
				entity.warehouseId(),
				entity.zoneId(),
				entity.locationId(),
				entity.quantity());
	}

	private InspectionSourceSnapshot toInventoryStockSnapshot(InventoryStockEntity entity) {
		return new InspectionSourceSnapshot(
				SOURCE_INVENTORY_STOCK,
				entity.id(),
				"STOCK-" + entity.id(),
				entity.productName() + " / " + entity.locationName(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.unitName(),
				entity.precisionDigits(),
				entity.warehouseId(),
				entity.zoneId(),
				entity.locationId(),
				entity.availableQuantity());
	}

	private String normalizeSourceType(String sourceType) {
		String normalized = sourceType == null ? "" : sourceType.trim().toUpperCase();

		if (!SOURCE_INBOUND_RECORD.equals(normalized) && !SOURCE_INVENTORY_STOCK.equals(normalized)) {
			throw new BusinessException(CommonErrorCode.QUALITY_INSPECTION_SOURCE_INVALID);
		}

		return normalized;
	}

	private Integer deriveResultStatus(BigDecimal inspectQuantity, BigDecimal unqualifiedQuantity) {
		if (unqualifiedQuantity.compareTo(BigDecimal.ZERO) == 0) {
			return RESULT_QUALIFIED;
		}

		if (unqualifiedQuantity.compareTo(inspectQuantity) == 0) {
			return RESULT_UNQUALIFIED;
		}

		return RESULT_PARTIAL;
	}

	private String generateInspectionCode() {
		for (int index = 0; index < 20; index += 1) {
			String inspectionCode = "QI-" + LocalDateTime.now().format(INSPECTION_CODE_FORMATTER);

			if (index > 0) {
				inspectionCode += "-" + index;
			}

			if (!qualityInspectionRepository.existsByInspectionCode(inspectionCode)) {
				return inspectionCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "质检单编号生成失败，请稍后重试");
	}

	private QualityInspectionListItemResponse toListItemResponse(QualityInspectionEntity entity) {
		return new QualityInspectionListItemResponse(
				entity.id(),
				entity.inspectionCode(),
				entity.sourceType(),
				entity.sourceId(),
				entity.sourceCode(),
				entity.sourceLabel(),
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
				QualityInspectionRepository.toDouble(entity.inspectQuantity()),
				QualityInspectionRepository.toDouble(entity.qualifiedQuantity()),
				QualityInspectionRepository.toDouble(entity.unqualifiedQuantity()),
				entity.resultStatus(),
				toResultStatusLabel(entity.resultStatus()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private QualityInspectionDetailResponse toDetailResponse(QualityInspectionEntity entity) {
		return new QualityInspectionDetailResponse(
				entity.id(),
				entity.inspectionCode(),
				entity.sourceType(),
				entity.sourceId(),
				entity.sourceCode(),
				entity.sourceLabel(),
				entity.productId(),
				entity.productCode(),
				entity.productName(),
				entity.unitName(),
				entity.unitSymbol(),
				entity.precisionDigits(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationId(),
				entity.locationName(),
				QualityInspectionRepository.toDouble(entity.inspectQuantity()),
				QualityInspectionRepository.toDouble(entity.qualifiedQuantity()),
				QualityInspectionRepository.toDouble(entity.unqualifiedQuantity()),
				entity.resultStatus(),
				toResultStatusLabel(entity.resultStatus()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String toResultStatusLabel(Integer resultStatus) {
		return switch (resultStatus) {
			case RESULT_QUALIFIED -> "合格";
			case RESULT_PARTIAL -> "部分不合格";
			case RESULT_UNQUALIFIED -> "不合格";
			default -> "未知结果";
		};
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private record InspectionSourceSnapshot(
			String sourceType,
			Long sourceId,
			String sourceCode,
			String sourceLabel,
			Long productId,
			String productCode,
			String productName,
			String unitName,
			Integer precisionDigits,
			Long warehouseId,
			Long zoneId,
			Long locationId,
			BigDecimal maxInspectableQuantity) {
	}
}
