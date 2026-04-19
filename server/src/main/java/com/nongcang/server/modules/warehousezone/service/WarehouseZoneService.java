package com.nongcang.server.modules.warehousezone.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.warehouse.repository.WarehouseRepository;
import com.nongcang.server.modules.warehousezone.domain.dto.WarehouseZoneCreateRequest;
import com.nongcang.server.modules.warehousezone.domain.dto.WarehouseZoneListQueryRequest;
import com.nongcang.server.modules.warehousezone.domain.dto.WarehouseZoneStatusUpdateRequest;
import com.nongcang.server.modules.warehousezone.domain.dto.WarehouseZoneUpdateRequest;
import com.nongcang.server.modules.warehousezone.domain.entity.WarehouseZoneEntity;
import com.nongcang.server.modules.warehousezone.domain.vo.WarehouseZoneDetailResponse;
import com.nongcang.server.modules.warehousezone.domain.vo.WarehouseZoneListItemResponse;
import com.nongcang.server.modules.warehousezone.domain.vo.WarehouseZoneOptionResponse;
import com.nongcang.server.modules.warehousezone.repository.WarehouseZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class WarehouseZoneService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter WAREHOUSE_ZONE_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final WarehouseZoneRepository warehouseZoneRepository;
	private final WarehouseRepository warehouseRepository;

	public WarehouseZoneService(
			WarehouseZoneRepository warehouseZoneRepository,
			WarehouseRepository warehouseRepository) {
		this.warehouseZoneRepository = warehouseZoneRepository;
		this.warehouseRepository = warehouseRepository;
	}

	public List<WarehouseZoneListItemResponse> getWarehouseZoneList(WarehouseZoneListQueryRequest queryRequest) {
		return warehouseZoneRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<WarehouseZoneOptionResponse> getWarehouseZoneOptions() {
		return warehouseZoneRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new WarehouseZoneOptionResponse(
						entity.id(),
						entity.zoneName(),
						entity.warehouseName(),
						entity.status()))
				.toList();
	}

	public WarehouseZoneDetailResponse getWarehouseZoneDetail(Long id) {
		return toDetailResponse(getExistingWarehouseZone(id));
	}

	@Transactional
	public WarehouseZoneDetailResponse createWarehouseZone(WarehouseZoneCreateRequest request) {
		resolveWarehouseId(request.warehouseId());
		validateUniqueName(request.warehouseId(), request.zoneName(), null);
		validateTemperatureRange(request.temperatureMin(), request.temperatureMax());

		WarehouseZoneEntity warehouseZoneEntity = new WarehouseZoneEntity(
				null,
				generateWarehouseZoneCode(),
				request.warehouseId(),
				null,
				request.zoneName().trim(),
				request.zoneType().trim(),
				request.temperatureMin(),
				request.temperatureMax(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = warehouseZoneRepository.insert(warehouseZoneEntity);
		return getWarehouseZoneDetail(id);
	}

	@Transactional
	public WarehouseZoneDetailResponse updateWarehouseZone(Long id, WarehouseZoneUpdateRequest request) {
		WarehouseZoneEntity currentWarehouseZone = getExistingWarehouseZone(id);

		resolveWarehouseId(request.warehouseId());
		validateUniqueName(request.warehouseId(), request.zoneName(), id);
		validateTemperatureRange(request.temperatureMin(), request.temperatureMax());

		WarehouseZoneEntity updatedWarehouseZone = new WarehouseZoneEntity(
				currentWarehouseZone.id(),
				currentWarehouseZone.zoneCode(),
				request.warehouseId(),
				null,
				request.zoneName().trim(),
				request.zoneType().trim(),
				request.temperatureMin(),
				request.temperatureMax(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentWarehouseZone.createdAt(),
				currentWarehouseZone.updatedAt());

		warehouseZoneRepository.update(updatedWarehouseZone);
		return getWarehouseZoneDetail(id);
	}

	@Transactional
	public void updateWarehouseZoneStatus(Long id, WarehouseZoneStatusUpdateRequest request) {
		getExistingWarehouseZone(id);
		warehouseZoneRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteWarehouseZone(Long id) {
		getExistingWarehouseZone(id);

		if (warehouseZoneRepository.countLocationReferences(id) > 0) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_ZONE_HAS_LOCATIONS);
		}

		warehouseZoneRepository.deleteById(id);
	}

	private WarehouseZoneEntity getExistingWarehouseZone(Long id) {
		return warehouseZoneRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_ZONE_NOT_FOUND));
	}

	private boolean matchesQuery(WarehouseZoneEntity entity, WarehouseZoneListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.zoneCode())
				&& !entity.zoneCode().contains(queryRequest.zoneCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.zoneName())
				&& !entity.zoneName().contains(queryRequest.zoneName().trim())) {
			return false;
		}

		if (queryRequest.warehouseId() != null
				&& !Objects.equals(entity.warehouseId(), queryRequest.warehouseId())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(Long warehouseId, String zoneName, Long excludeId) {
		if (warehouseZoneRepository.existsByWarehouseAndZoneName(warehouseId, zoneName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_ZONE_NAME_DUPLICATED);
		}
	}

	private void validateTemperatureRange(BigDecimal temperatureMin, BigDecimal temperatureMax) {
		if (temperatureMin != null && temperatureMax != null && temperatureMin.compareTo(temperatureMax) > 0) {
			throw new BusinessException(CommonErrorCode.STORAGE_CONDITION_TEMPERATURE_RANGE_INVALID);
		}
	}

	private Long resolveWarehouseId(Long warehouseId) {
		return warehouseRepository.findById(warehouseId)
				.map(warehouse -> warehouse.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.WAREHOUSE_NOT_FOUND));
	}

	private String generateWarehouseZoneCode() {
		for (int index = 0; index < 20; index += 1) {
			String zoneCode = "ZONE-" + LocalDateTime.now().format(WAREHOUSE_ZONE_CODE_FORMATTER);

			if (index > 0) {
				zoneCode += "-" + index;
			}

			if (!warehouseZoneRepository.existsByZoneCode(zoneCode, null)) {
				return zoneCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "库区编号生成失败，请稍后重试");
	}

	private WarehouseZoneListItemResponse toListItemResponse(WarehouseZoneEntity entity) {
		return new WarehouseZoneListItemResponse(
				entity.id(),
				entity.zoneCode(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneName(),
				entity.zoneType(),
				toDouble(entity.temperatureMin()),
				toDouble(entity.temperatureMax()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private WarehouseZoneDetailResponse toDetailResponse(WarehouseZoneEntity entity) {
		return new WarehouseZoneDetailResponse(
				entity.id(),
				entity.zoneCode(),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.zoneName(),
				entity.zoneType(),
				toDouble(entity.temperatureMin()),
				toDouble(entity.temperatureMax()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
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
