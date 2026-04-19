package com.nongcang.server.modules.warehouselocation.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.warehouse.repository.WarehouseRepository;
import com.nongcang.server.modules.warehousezone.domain.entity.WarehouseZoneEntity;
import com.nongcang.server.modules.warehousezone.repository.WarehouseZoneRepository;
import com.nongcang.server.modules.warehouselocation.domain.dto.WarehouseLocationCreateRequest;
import com.nongcang.server.modules.warehouselocation.domain.dto.WarehouseLocationListQueryRequest;
import com.nongcang.server.modules.warehouselocation.domain.dto.WarehouseLocationStatusUpdateRequest;
import com.nongcang.server.modules.warehouselocation.domain.dto.WarehouseLocationUpdateRequest;
import com.nongcang.server.modules.warehouselocation.domain.entity.WarehouseLocationEntity;
import com.nongcang.server.modules.warehouselocation.domain.vo.WarehouseLocationDetailResponse;
import com.nongcang.server.modules.warehouselocation.domain.vo.WarehouseLocationListItemResponse;
import com.nongcang.server.modules.warehouselocation.domain.vo.WarehouseLocationOptionResponse;
import com.nongcang.server.modules.warehouselocation.repository.WarehouseLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class WarehouseLocationService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter WAREHOUSE_LOCATION_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final WarehouseLocationRepository warehouseLocationRepository;
	private final WarehouseRepository warehouseRepository;
	private final WarehouseZoneRepository warehouseZoneRepository;

	public WarehouseLocationService(
			WarehouseLocationRepository warehouseLocationRepository,
			WarehouseRepository warehouseRepository,
			WarehouseZoneRepository warehouseZoneRepository) {
		this.warehouseLocationRepository = warehouseLocationRepository;
		this.warehouseRepository = warehouseRepository;
		this.warehouseZoneRepository = warehouseZoneRepository;
	}

	public List<WarehouseLocationListItemResponse> getWarehouseLocationList(
			WarehouseLocationListQueryRequest queryRequest) {
		return warehouseLocationRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<WarehouseLocationOptionResponse> getWarehouseLocationOptions() {
		return warehouseLocationRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new WarehouseLocationOptionResponse(
						entity.id(),
						entity.warehouseId(),
						entity.zoneId(),
						entity.locationName(),
						entity.zoneName(),
						entity.status()))
				.toList();
	}

	public WarehouseLocationDetailResponse getWarehouseLocationDetail(Long id) {
		return toDetailResponse(getExistingWarehouseLocation(id));
	}

	@Transactional
	public WarehouseLocationDetailResponse createWarehouseLocation(WarehouseLocationCreateRequest request) {
		resolveWarehouseId(request.warehouseId());
		WarehouseZoneEntity warehouseZone = resolveZoneId(request.zoneId());
		validateWarehouseZoneRelation(request.warehouseId(), warehouseZone);
		validateUniqueName(request.zoneId(), request.locationName(), null);

		WarehouseLocationEntity warehouseLocationEntity = new WarehouseLocationEntity(
				null,
				generateWarehouseLocationCode(),
				request.warehouseId(),
				null,
				request.zoneId(),
				null,
				request.locationName().trim(),
				request.locationType().trim(),
				request.capacity(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = warehouseLocationRepository.insert(warehouseLocationEntity);
		return getWarehouseLocationDetail(id);
	}

	@Transactional
	public WarehouseLocationDetailResponse updateWarehouseLocation(Long id, WarehouseLocationUpdateRequest request) {
		WarehouseLocationEntity currentWarehouseLocation = getExistingWarehouseLocation(id);

		resolveWarehouseId(request.warehouseId());
		WarehouseZoneEntity warehouseZone = resolveZoneId(request.zoneId());
		validateWarehouseZoneRelation(request.warehouseId(), warehouseZone);
		validateUniqueName(request.zoneId(), request.locationName(), id);

		WarehouseLocationEntity updatedWarehouseLocation = new WarehouseLocationEntity(
				currentWarehouseLocation.id(),
				currentWarehouseLocation.locationCode(),
				request.warehouseId(),
				null,
				request.zoneId(),
				null,
				request.locationName().trim(),
				request.locationType().trim(),
				request.capacity(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentWarehouseLocation.createdAt(),
				currentWarehouseLocation.updatedAt());

		warehouseLocationRepository.update(updatedWarehouseLocation);
		return getWarehouseLocationDetail(id);
	}

	@Transactional
	public void updateWarehouseLocationStatus(Long id, WarehouseLocationStatusUpdateRequest request) {
		getExistingWarehouseLocation(id);
		warehouseLocationRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteWarehouseLocation(Long id) {
		getExistingWarehouseLocation(id);
		warehouseLocationRepository.deleteById(id);
	}

	private WarehouseLocationEntity getExistingWarehouseLocation(Long id) {
		return warehouseLocationRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_NOT_FOUND));
	}

	private boolean matchesQuery(WarehouseLocationEntity entity, WarehouseLocationListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.locationCode())
				&& !entity.locationCode().contains(queryRequest.locationCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.locationName())
				&& !entity.locationName().contains(queryRequest.locationName().trim())) {
			return false;
		}

		if (queryRequest.warehouseId() != null
				&& !Objects.equals(entity.warehouseId(), queryRequest.warehouseId())) {
			return false;
		}

		if (queryRequest.zoneId() != null && !Objects.equals(entity.zoneId(), queryRequest.zoneId())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(Long zoneId, String locationName, Long excludeId) {
		if (warehouseLocationRepository.existsByZoneAndLocationName(zoneId, locationName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_NAME_DUPLICATED);
		}
	}

	private Long resolveWarehouseId(Long warehouseId) {
		return warehouseRepository.findById(warehouseId)
				.map(warehouse -> warehouse.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_NOT_FOUND));
	}

	private WarehouseZoneEntity resolveZoneId(Long zoneId) {
		return warehouseZoneRepository.findById(zoneId)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_ZONE_NOT_FOUND));
	}

	private void validateWarehouseZoneRelation(Long warehouseId, WarehouseZoneEntity warehouseZone) {
		if (!Objects.equals(warehouseZone.warehouseId(), warehouseId)) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_LOCATION_ZONE_MISMATCH);
		}
	}

	private String generateWarehouseLocationCode() {
		for (int index = 0; index < 20; index += 1) {
			String locationCode = "LOC-" + LocalDateTime.now().format(WAREHOUSE_LOCATION_CODE_FORMATTER);

			if (index > 0) {
				locationCode += "-" + index;
			}

			if (!warehouseLocationRepository.existsByLocationCode(locationCode, null)) {
				return locationCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "库位编号生成失败，请稍后重试");
	}

	private WarehouseLocationListItemResponse toListItemResponse(WarehouseLocationEntity entity) {
		return new WarehouseLocationListItemResponse(
				entity.id(),
				entity.locationCode(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationName(),
				entity.locationType(),
				entity.capacity(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private WarehouseLocationDetailResponse toDetailResponse(WarehouseLocationEntity entity) {
		return new WarehouseLocationDetailResponse(
				entity.id(),
				entity.locationCode(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneId(),
				entity.zoneName(),
				entity.locationName(),
				entity.locationType(),
				entity.capacity(),
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
