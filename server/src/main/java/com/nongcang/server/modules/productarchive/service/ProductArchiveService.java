package com.nongcang.server.modules.productarchive.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.category.repository.CategoryRepository;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveCreateRequest;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveListQueryRequest;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveStatusUpdateRequest;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveUpdateRequest;
import com.nongcang.server.modules.productarchive.domain.entity.ProductArchiveEntity;
import com.nongcang.server.modules.productarchive.domain.vo.ProductArchiveDetailResponse;
import com.nongcang.server.modules.productarchive.domain.vo.ProductArchiveListItemResponse;
import com.nongcang.server.modules.productarchive.domain.vo.ProductArchiveOptionResponse;
import com.nongcang.server.modules.productarchive.repository.ProductArchiveRepository;
import com.nongcang.server.modules.productorigin.repository.ProductOriginRepository;
import com.nongcang.server.modules.productunit.repository.ProductUnitRepository;
import com.nongcang.server.modules.qualitygrade.repository.QualityGradeRepository;
import com.nongcang.server.modules.storagecondition.repository.StorageConditionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductArchiveService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter PRODUCT_ARCHIVE_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final ProductArchiveRepository productArchiveRepository;
	private final CategoryRepository categoryRepository;
	private final ProductUnitRepository productUnitRepository;
	private final ProductOriginRepository productOriginRepository;
	private final StorageConditionRepository storageConditionRepository;
	private final QualityGradeRepository qualityGradeRepository;

	public ProductArchiveService(
			ProductArchiveRepository productArchiveRepository,
			CategoryRepository categoryRepository,
			ProductUnitRepository productUnitRepository,
			ProductOriginRepository productOriginRepository,
			StorageConditionRepository storageConditionRepository,
			QualityGradeRepository qualityGradeRepository) {
		this.productArchiveRepository = productArchiveRepository;
		this.categoryRepository = categoryRepository;
		this.productUnitRepository = productUnitRepository;
		this.productOriginRepository = productOriginRepository;
		this.storageConditionRepository = storageConditionRepository;
		this.qualityGradeRepository = qualityGradeRepository;
	}

	public List<ProductArchiveListItemResponse> getProductArchiveList(ProductArchiveListQueryRequest queryRequest) {
		return productArchiveRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<ProductArchiveOptionResponse> getProductArchiveOptions() {
		return productArchiveRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new ProductArchiveOptionResponse(
						entity.id(),
						entity.productName(),
						entity.unitName(),
						entity.unitSymbol(),
						entity.precisionDigits(),
						entity.status()))
				.toList();
	}

	public ProductArchiveDetailResponse getProductArchiveDetail(Long id) {
		return toDetailResponse(getExistingProductArchive(id));
	}

	@Transactional
	public ProductArchiveDetailResponse createProductArchive(ProductArchiveCreateRequest request) {
		validateUniqueName(request.productName(), null);
		validateReferences(request);

		ProductArchiveEntity productArchiveEntity = new ProductArchiveEntity(
				null,
				generateProductCode(),
				request.productName().trim(),
				trimToNull(request.productSpecification()),
				request.categoryId(),
				null,
				request.unitId(),
				null,
				null,
				null,
				request.originId(),
				null,
				request.storageConditionId(),
				null,
				request.shelfLifeDays(),
				request.warningDays(),
				request.qualityGradeId(),
				null,
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = productArchiveRepository.insert(productArchiveEntity);
		return getProductArchiveDetail(id);
	}

	@Transactional
	public ProductArchiveDetailResponse updateProductArchive(Long id, ProductArchiveUpdateRequest request) {
		ProductArchiveEntity currentProductArchive = getExistingProductArchive(id);

		validateUniqueName(request.productName(), id);
		validateReferences(request);

		ProductArchiveEntity updatedProductArchive = new ProductArchiveEntity(
				currentProductArchive.id(),
				currentProductArchive.productCode(),
				request.productName().trim(),
				trimToNull(request.productSpecification()),
				request.categoryId(),
				null,
				request.unitId(),
				null,
				null,
				currentProductArchive.precisionDigits(),
				request.originId(),
				null,
				request.storageConditionId(),
				null,
				request.shelfLifeDays(),
				request.warningDays(),
				request.qualityGradeId(),
				null,
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentProductArchive.createdAt(),
				currentProductArchive.updatedAt());

		productArchiveRepository.update(updatedProductArchive);
		return getProductArchiveDetail(id);
	}

	@Transactional
	public void updateProductArchiveStatus(Long id, ProductArchiveStatusUpdateRequest request) {
		getExistingProductArchive(id);
		productArchiveRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteProductArchive(Long id) {
		getExistingProductArchive(id);
		productArchiveRepository.deleteById(id);
	}

	private ProductArchiveEntity getExistingProductArchive(Long id) {
		return productArchiveRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ARCHIVE_NOT_FOUND));
	}

	private boolean matchesQuery(ProductArchiveEntity entity, ProductArchiveListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.productCode())
				&& !entity.productCode().contains(queryRequest.productCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.productName())
				&& !entity.productName().contains(queryRequest.productName().trim())) {
			return false;
		}

		if (queryRequest.categoryId() != null && !Objects.equals(entity.categoryId(), queryRequest.categoryId())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(String productName, Long excludeId) {
		if (productArchiveRepository.existsByProductName(productName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.PRODUCT_ARCHIVE_NAME_DUPLICATED);
		}
	}

	private void validateReferences(ProductArchiveCreateRequest request) {
		resolveCategoryId(request.categoryId());
		resolveUnitId(request.unitId());
		resolveOriginId(request.originId());
		resolveStorageConditionId(request.storageConditionId());
		resolveQualityGradeId(request.qualityGradeId());
	}

	private void validateReferences(ProductArchiveUpdateRequest request) {
		resolveCategoryId(request.categoryId());
		resolveUnitId(request.unitId());
		resolveOriginId(request.originId());
		resolveStorageConditionId(request.storageConditionId());
		resolveQualityGradeId(request.qualityGradeId());
	}

	private Long resolveCategoryId(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.map(category -> category.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.CATEGORY_NOT_FOUND));
	}

	private Long resolveUnitId(Long unitId) {
		return productUnitRepository.findById(unitId)
				.map(productUnit -> productUnit.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_UNIT_NOT_FOUND));
	}

	private Long resolveOriginId(Long originId) {
		return productOriginRepository.findById(originId)
				.map(productOrigin -> productOrigin.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ORIGIN_NOT_FOUND));
	}

	private Long resolveStorageConditionId(Long storageConditionId) {
		return storageConditionRepository.findById(storageConditionId)
				.map(storageCondition -> storageCondition.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.STORAGE_CONDITION_NOT_FOUND));
	}

	private Long resolveQualityGradeId(Long qualityGradeId) {
		return qualityGradeRepository.findById(qualityGradeId)
				.map(qualityGrade -> qualityGrade.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.QUALITY_GRADE_NOT_FOUND));
	}

	private String generateProductCode() {
		for (int index = 0; index < 20; index += 1) {
			String productCode = "PROD-" + LocalDateTime.now().format(PRODUCT_ARCHIVE_CODE_FORMATTER);

			if (index > 0) {
				productCode += "-" + index;
			}

			if (!productArchiveRepository.existsByProductCode(productCode, null)) {
				return productCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "产品档案编号生成失败，请稍后重试");
	}

	private ProductArchiveListItemResponse toListItemResponse(ProductArchiveEntity entity) {
		return new ProductArchiveListItemResponse(
				entity.id(),
				entity.productCode(),
				entity.productName(),
				entity.productSpecification(),
				entity.categoryId(),
				entity.categoryName(),
				entity.unitId(),
				entity.unitName(),
				entity.unitSymbol(),
				entity.originId(),
				entity.originName(),
				entity.storageConditionId(),
				entity.storageConditionName(),
				entity.shelfLifeDays(),
				entity.warningDays(),
				entity.qualityGradeId(),
				entity.qualityGradeName(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private ProductArchiveDetailResponse toDetailResponse(ProductArchiveEntity entity) {
		return new ProductArchiveDetailResponse(
				entity.id(),
				entity.productCode(),
				entity.productName(),
				entity.productSpecification(),
				entity.categoryId(),
				entity.categoryName(),
				entity.unitId(),
				entity.unitName(),
				entity.unitSymbol(),
				entity.originId(),
				entity.originName(),
				entity.storageConditionId(),
				entity.storageConditionName(),
				entity.shelfLifeDays(),
				entity.warningDays(),
				entity.qualityGradeId(),
				entity.qualityGradeName(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String toStatusLabel(Integer status) {
		return ENABLED == status ? "启用" : "停用";
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
