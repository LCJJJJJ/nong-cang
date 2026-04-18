package com.nongcang.server.modules.productunit.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitCreateRequest;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitListQueryRequest;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitStatusUpdateRequest;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitUpdateRequest;
import com.nongcang.server.modules.productunit.domain.entity.ProductUnitEntity;
import com.nongcang.server.modules.productunit.domain.vo.ProductUnitDetailResponse;
import com.nongcang.server.modules.productunit.domain.vo.ProductUnitListItemResponse;
import com.nongcang.server.modules.productunit.domain.vo.ProductUnitOptionResponse;
import com.nongcang.server.modules.productunit.repository.ProductUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductUnitService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter PRODUCT_UNIT_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final ProductUnitRepository productUnitRepository;

	public ProductUnitService(ProductUnitRepository productUnitRepository) {
		this.productUnitRepository = productUnitRepository;
	}

	public List<ProductUnitListItemResponse> getProductUnitList(ProductUnitListQueryRequest queryRequest) {
		return productUnitRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<ProductUnitOptionResponse> getProductUnitOptions() {
		return productUnitRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new ProductUnitOptionResponse(
						entity.id(),
						entity.unitName(),
						entity.unitSymbol(),
						entity.status()))
				.toList();
	}

	public ProductUnitDetailResponse getProductUnitDetail(Long id) {
		return toDetailResponse(getExistingProductUnit(id));
	}

	@Transactional
	public ProductUnitDetailResponse createProductUnit(ProductUnitCreateRequest request) {
		validateUniqueName(request.unitName(), null);

		ProductUnitEntity productUnitEntity = new ProductUnitEntity(
				null,
				generateUnitCode(),
				request.unitName().trim(),
				request.unitSymbol().trim(),
				request.unitType().trim(),
				request.precisionDigits(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = productUnitRepository.insert(productUnitEntity);
		return getProductUnitDetail(id);
	}

	@Transactional
	public ProductUnitDetailResponse updateProductUnit(Long id, ProductUnitUpdateRequest request) {
		ProductUnitEntity currentProductUnit = getExistingProductUnit(id);

		validateUniqueName(request.unitName(), id);

		ProductUnitEntity updatedProductUnit = new ProductUnitEntity(
				currentProductUnit.id(),
				currentProductUnit.unitCode(),
				request.unitName().trim(),
				request.unitSymbol().trim(),
				request.unitType().trim(),
				request.precisionDigits(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentProductUnit.createdAt(),
				currentProductUnit.updatedAt());

		productUnitRepository.update(updatedProductUnit);
		return getProductUnitDetail(id);
	}

	@Transactional
	public void updateProductUnitStatus(Long id, ProductUnitStatusUpdateRequest request) {
		getExistingProductUnit(id);
		productUnitRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteProductUnit(Long id) {
		getExistingProductUnit(id);
		productUnitRepository.deleteById(id);
	}

	private ProductUnitEntity getExistingProductUnit(Long id) {
		return productUnitRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_UNIT_NOT_FOUND));
	}

	private boolean matchesQuery(ProductUnitEntity entity, ProductUnitListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.unitCode())
				&& !entity.unitCode().contains(queryRequest.unitCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.unitName())
				&& !entity.unitName().contains(queryRequest.unitName().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.unitType())
				&& !entity.unitType().contains(queryRequest.unitType().trim())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(String unitName, Long excludeId) {
		if (productUnitRepository.existsByUnitName(unitName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.PRODUCT_UNIT_NAME_DUPLICATED);
		}
	}

	private String generateUnitCode() {
		for (int index = 0; index < 20; index += 1) {
			String unitCode = "UNIT-" + LocalDateTime.now().format(PRODUCT_UNIT_CODE_FORMATTER);

			if (index > 0) {
				unitCode += "-" + index;
			}

			if (!productUnitRepository.existsByUnitCode(unitCode, null)) {
				return unitCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "产品单位编号生成失败，请稍后重试");
	}

	private ProductUnitListItemResponse toListItemResponse(ProductUnitEntity entity) {
		return new ProductUnitListItemResponse(
				entity.id(),
				entity.unitCode(),
				entity.unitName(),
				entity.unitSymbol(),
				entity.unitType(),
				entity.precisionDigits(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private ProductUnitDetailResponse toDetailResponse(ProductUnitEntity entity) {
		return new ProductUnitDetailResponse(
				entity.id(),
				entity.unitCode(),
				entity.unitName(),
				entity.unitSymbol(),
				entity.unitType(),
				entity.precisionDigits(),
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
