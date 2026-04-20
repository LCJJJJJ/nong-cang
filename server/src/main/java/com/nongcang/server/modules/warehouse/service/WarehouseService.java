package com.nongcang.server.modules.warehouse.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.common.security.WarehouseAccessScopeService;
import com.nongcang.server.modules.warehouse.domain.dto.WarehouseCreateRequest;
import com.nongcang.server.modules.warehouse.domain.dto.WarehouseListQueryRequest;
import com.nongcang.server.modules.warehouse.domain.dto.WarehouseStatusUpdateRequest;
import com.nongcang.server.modules.warehouse.domain.dto.WarehouseUpdateRequest;
import com.nongcang.server.modules.warehouse.domain.entity.WarehouseEntity;
import com.nongcang.server.modules.warehouse.domain.vo.WarehouseDetailResponse;
import com.nongcang.server.modules.warehouse.domain.vo.WarehouseListItemResponse;
import com.nongcang.server.modules.warehouse.domain.vo.WarehouseOptionResponse;
import com.nongcang.server.modules.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class WarehouseService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter WAREHOUSE_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final WarehouseRepository warehouseRepository;
	private final WarehouseAccessScopeService warehouseAccessScopeService;

	public WarehouseService(
			WarehouseRepository warehouseRepository,
			WarehouseAccessScopeService warehouseAccessScopeService) {
		this.warehouseRepository = warehouseRepository;
		this.warehouseAccessScopeService = warehouseAccessScopeService;
	}

	public List<WarehouseListItemResponse> getWarehouseList(WarehouseListQueryRequest queryRequest) {
		Long scopedWarehouseId = warehouseAccessScopeService.resolveQueryWarehouseId(null);
		return warehouseRepository.findAll()
				.stream()
				.filter(entity -> scopedWarehouseId == null || Objects.equals(entity.id(), scopedWarehouseId))
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<WarehouseOptionResponse> getWarehouseOptions() {
		Long scopedWarehouseId = warehouseAccessScopeService.resolveQueryWarehouseId(null);
		return warehouseRepository.findAll()
				.stream()
				.filter(entity -> scopedWarehouseId == null || Objects.equals(entity.id(), scopedWarehouseId))
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new WarehouseOptionResponse(
						entity.id(),
						entity.warehouseName(),
						entity.warehouseType(),
						entity.status()))
				.toList();
	}

	public WarehouseDetailResponse getWarehouseDetail(Long id) {
		WarehouseEntity warehouse = getExistingWarehouse(id);
		warehouseAccessScopeService.assertWarehouseAccess(warehouse.id());
		return toDetailResponse(warehouse);
	}

	@Transactional
	public WarehouseDetailResponse createWarehouse(WarehouseCreateRequest request) {
		warehouseAccessScopeService.assertAdminOrNoWarehouseScope();
		validateUniqueName(request.warehouseName(), null);

		WarehouseEntity warehouseEntity = new WarehouseEntity(
				null,
				generateWarehouseCode(),
				request.warehouseName().trim(),
				request.warehouseType().trim(),
				trimToNull(request.managerName()),
				trimToNull(request.contactPhone()),
				trimToNull(request.address()),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = warehouseRepository.insert(warehouseEntity);
		return getWarehouseDetail(id);
	}

	@Transactional
	public WarehouseDetailResponse updateWarehouse(Long id, WarehouseUpdateRequest request) {
		WarehouseEntity currentWarehouse = getExistingWarehouse(id);
		warehouseAccessScopeService.assertWarehouseAccess(currentWarehouse.id());

		validateUniqueName(request.warehouseName(), id);

		WarehouseEntity updatedWarehouse = new WarehouseEntity(
				currentWarehouse.id(),
				currentWarehouse.warehouseCode(),
				request.warehouseName().trim(),
				request.warehouseType().trim(),
				trimToNull(request.managerName()),
				trimToNull(request.contactPhone()),
				trimToNull(request.address()),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentWarehouse.createdAt(),
				currentWarehouse.updatedAt());

		warehouseRepository.update(updatedWarehouse);
		return getWarehouseDetail(id);
	}

	@Transactional
	public void updateWarehouseStatus(Long id, WarehouseStatusUpdateRequest request) {
		WarehouseEntity warehouse = getExistingWarehouse(id);
		warehouseAccessScopeService.assertWarehouseAccess(warehouse.id());
		warehouseRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteWarehouse(Long id) {
		WarehouseEntity warehouse = getExistingWarehouse(id);
		warehouseAccessScopeService.assertWarehouseAccess(warehouse.id());

		if (warehouseRepository.countZoneReferences(id) > 0) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_HAS_ZONES);
		}

		warehouseRepository.deleteById(id);
	}

	private WarehouseEntity getExistingWarehouse(Long id) {
		return warehouseRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_NOT_FOUND));
	}

	private boolean matchesQuery(WarehouseEntity entity, WarehouseListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.warehouseCode())
				&& !entity.warehouseCode().contains(queryRequest.warehouseCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.warehouseName())
				&& !entity.warehouseName().contains(queryRequest.warehouseName().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.warehouseType())
				&& !entity.warehouseType().contains(queryRequest.warehouseType().trim())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(String warehouseName, Long excludeId) {
		if (warehouseRepository.existsByWarehouseName(warehouseName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_NAME_DUPLICATED);
		}
	}

	private String generateWarehouseCode() {
		for (int index = 0; index < 20; index += 1) {
			String warehouseCode = "WH-" + LocalDateTime.now().format(WAREHOUSE_CODE_FORMATTER);

			if (index > 0) {
				warehouseCode += "-" + index;
			}

			if (!warehouseRepository.existsByWarehouseCode(warehouseCode, null)) {
				return warehouseCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "仓库编号生成失败，请稍后重试");
	}

	private WarehouseListItemResponse toListItemResponse(WarehouseEntity entity) {
		return new WarehouseListItemResponse(
				entity.id(),
				entity.warehouseCode(),
				entity.warehouseName(),
				entity.warehouseType(),
				entity.managerName(),
				entity.contactPhone(),
				entity.address(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private WarehouseDetailResponse toDetailResponse(WarehouseEntity entity) {
		return new WarehouseDetailResponse(
				entity.id(),
				entity.warehouseCode(),
				entity.warehouseName(),
				entity.warehouseType(),
				entity.managerName(),
				entity.contactPhone(),
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
