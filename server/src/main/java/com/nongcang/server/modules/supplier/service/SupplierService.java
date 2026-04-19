package com.nongcang.server.modules.supplier.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.supplier.domain.dto.SupplierCreateRequest;
import com.nongcang.server.modules.supplier.domain.dto.SupplierListQueryRequest;
import com.nongcang.server.modules.supplier.domain.dto.SupplierStatusUpdateRequest;
import com.nongcang.server.modules.supplier.domain.dto.SupplierUpdateRequest;
import com.nongcang.server.modules.supplier.domain.entity.SupplierEntity;
import com.nongcang.server.modules.supplier.domain.vo.SupplierDetailResponse;
import com.nongcang.server.modules.supplier.domain.vo.SupplierListItemResponse;
import com.nongcang.server.modules.supplier.domain.vo.SupplierOptionResponse;
import com.nongcang.server.modules.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SupplierService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter SUPPLIER_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final SupplierRepository supplierRepository;

	public SupplierService(SupplierRepository supplierRepository) {
		this.supplierRepository = supplierRepository;
	}

	public List<SupplierListItemResponse> getSupplierList(SupplierListQueryRequest queryRequest) {
		return supplierRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<SupplierOptionResponse> getSupplierOptions() {
		return supplierRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new SupplierOptionResponse(
						entity.id(),
						entity.supplierName(),
						entity.supplierType(),
						entity.status()))
				.toList();
	}

	public SupplierDetailResponse getSupplierDetail(Long id) {
		return toDetailResponse(getExistingSupplier(id));
	}

	@Transactional
	public SupplierDetailResponse createSupplier(SupplierCreateRequest request) {
		validateUniqueName(request.supplierName(), null);

		SupplierEntity supplierEntity = new SupplierEntity(
				null,
				generateSupplierCode(),
				request.supplierName().trim(),
				request.supplierType().trim(),
				trimToNull(request.contactName()),
				trimToNull(request.contactPhone()),
				trimToNull(request.regionName()),
				trimToNull(request.address()),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = supplierRepository.insert(supplierEntity);
		return getSupplierDetail(id);
	}

	@Transactional
	public SupplierDetailResponse updateSupplier(Long id, SupplierUpdateRequest request) {
		SupplierEntity currentSupplier = getExistingSupplier(id);

		validateUniqueName(request.supplierName(), id);

		SupplierEntity updatedSupplier = new SupplierEntity(
				currentSupplier.id(),
				currentSupplier.supplierCode(),
				request.supplierName().trim(),
				request.supplierType().trim(),
				trimToNull(request.contactName()),
				trimToNull(request.contactPhone()),
				trimToNull(request.regionName()),
				trimToNull(request.address()),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentSupplier.createdAt(),
				currentSupplier.updatedAt());

		supplierRepository.update(updatedSupplier);
		return getSupplierDetail(id);
	}

	@Transactional
	public void updateSupplierStatus(Long id, SupplierStatusUpdateRequest request) {
		getExistingSupplier(id);
		supplierRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteSupplier(Long id) {
		getExistingSupplier(id);
		supplierRepository.deleteById(id);
	}

	private SupplierEntity getExistingSupplier(Long id) {
		return supplierRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.SUPPLIER_NOT_FOUND));
	}

	private boolean matchesQuery(SupplierEntity entity, SupplierListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.supplierCode())
				&& !entity.supplierCode().contains(queryRequest.supplierCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.supplierName())
				&& !entity.supplierName().contains(queryRequest.supplierName().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.contactName())
				&& (entity.contactName() == null
					|| !entity.contactName().contains(queryRequest.contactName().trim()))) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(String supplierName, Long excludeId) {
		if (supplierRepository.existsBySupplierName(supplierName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.SUPPLIER_NAME_DUPLICATED);
		}
	}

	private String generateSupplierCode() {
		for (int index = 0; index < 20; index += 1) {
			String supplierCode = "SUP-" + LocalDateTime.now().format(SUPPLIER_CODE_FORMATTER);

			if (index > 0) {
				supplierCode += "-" + index;
			}

			if (!supplierRepository.existsBySupplierCode(supplierCode, null)) {
				return supplierCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "供应商编号生成失败，请稍后重试");
	}

	private SupplierListItemResponse toListItemResponse(SupplierEntity entity) {
		return new SupplierListItemResponse(
				entity.id(),
				entity.supplierCode(),
				entity.supplierName(),
				entity.supplierType(),
				entity.contactName(),
				entity.contactPhone(),
				entity.regionName(),
				entity.address(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private SupplierDetailResponse toDetailResponse(SupplierEntity entity) {
		return new SupplierDetailResponse(
				entity.id(),
				entity.supplierCode(),
				entity.supplierName(),
				entity.supplierType(),
				entity.contactName(),
				entity.contactPhone(),
				entity.regionName(),
				entity.address(),
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
