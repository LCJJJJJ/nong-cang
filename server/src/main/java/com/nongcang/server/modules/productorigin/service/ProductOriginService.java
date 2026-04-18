package com.nongcang.server.modules.productorigin.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginCreateRequest;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginListQueryRequest;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginStatusUpdateRequest;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginUpdateRequest;
import com.nongcang.server.modules.productorigin.domain.entity.ProductOriginEntity;
import com.nongcang.server.modules.productorigin.domain.vo.ProductOriginDetailResponse;
import com.nongcang.server.modules.productorigin.domain.vo.ProductOriginListItemResponse;
import com.nongcang.server.modules.productorigin.domain.vo.ProductOriginOptionResponse;
import com.nongcang.server.modules.productorigin.repository.ProductOriginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductOriginService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter PRODUCT_ORIGIN_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final ProductOriginRepository productOriginRepository;

	public ProductOriginService(ProductOriginRepository productOriginRepository) {
		this.productOriginRepository = productOriginRepository;
	}

	public List<ProductOriginListItemResponse> getProductOriginList(ProductOriginListQueryRequest queryRequest) {
		return productOriginRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<ProductOriginOptionResponse> getProductOriginOptions() {
		return productOriginRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new ProductOriginOptionResponse(
						entity.id(),
						entity.originName(),
						buildLocationText(entity),
						entity.status()))
				.toList();
	}

	public ProductOriginDetailResponse getProductOriginDetail(Long id) {
		return toDetailResponse(getExistingProductOrigin(id));
	}

	@Transactional
	public ProductOriginDetailResponse createProductOrigin(ProductOriginCreateRequest request) {
		validateUniqueName(request.originName(), null);

		ProductOriginEntity productOriginEntity = new ProductOriginEntity(
				null,
				generateOriginCode(),
				request.originName().trim(),
				request.countryName().trim(),
				request.provinceName().trim(),
				trimToNull(request.cityName()),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = productOriginRepository.insert(productOriginEntity);
		return getProductOriginDetail(id);
	}

	@Transactional
	public ProductOriginDetailResponse updateProductOrigin(Long id, ProductOriginUpdateRequest request) {
		ProductOriginEntity currentProductOrigin = getExistingProductOrigin(id);

		validateUniqueName(request.originName(), id);

		ProductOriginEntity updatedProductOrigin = new ProductOriginEntity(
				currentProductOrigin.id(),
				currentProductOrigin.originCode(),
				request.originName().trim(),
				request.countryName().trim(),
				request.provinceName().trim(),
				trimToNull(request.cityName()),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentProductOrigin.createdAt(),
				currentProductOrigin.updatedAt());

		productOriginRepository.update(updatedProductOrigin);
		return getProductOriginDetail(id);
	}

	@Transactional
	public void updateProductOriginStatus(Long id, ProductOriginStatusUpdateRequest request) {
		getExistingProductOrigin(id);
		productOriginRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteProductOrigin(Long id) {
		getExistingProductOrigin(id);
		productOriginRepository.deleteById(id);
	}

	private ProductOriginEntity getExistingProductOrigin(Long id) {
		return productOriginRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ORIGIN_NOT_FOUND));
	}

	private boolean matchesQuery(ProductOriginEntity entity, ProductOriginListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.originCode())
				&& !entity.originCode().contains(queryRequest.originCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.originName())
				&& !entity.originName().contains(queryRequest.originName().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.provinceName())
				&& !entity.provinceName().contains(queryRequest.provinceName().trim())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(String originName, Long excludeId) {
		if (productOriginRepository.existsByOriginName(originName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.PRODUCT_ORIGIN_NAME_DUPLICATED);
		}
	}

	private String generateOriginCode() {
		for (int index = 0; index < 20; index += 1) {
			String originCode = "ORI-" + LocalDateTime.now().format(PRODUCT_ORIGIN_CODE_FORMATTER);

			if (index > 0) {
				originCode += "-" + index;
			}

			if (!productOriginRepository.existsByOriginCode(originCode, null)) {
				return originCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "产地编号生成失败，请稍后重试");
	}

	private ProductOriginListItemResponse toListItemResponse(ProductOriginEntity entity) {
		return new ProductOriginListItemResponse(
				entity.id(),
				entity.originCode(),
				entity.originName(),
				entity.countryName(),
				entity.provinceName(),
				entity.cityName(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private ProductOriginDetailResponse toDetailResponse(ProductOriginEntity entity) {
		return new ProductOriginDetailResponse(
				entity.id(),
				entity.originCode(),
				entity.originName(),
				entity.countryName(),
				entity.provinceName(),
				entity.cityName(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String buildLocationText(ProductOriginEntity entity) {
		return entity.cityName() == null
				? entity.provinceName()
				: entity.provinceName() + " / " + entity.cityName();
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
