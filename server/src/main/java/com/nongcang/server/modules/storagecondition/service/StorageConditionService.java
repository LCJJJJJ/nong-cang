package com.nongcang.server.modules.storagecondition.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionCreateRequest;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionListQueryRequest;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionStatusUpdateRequest;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionUpdateRequest;
import com.nongcang.server.modules.storagecondition.domain.entity.StorageConditionEntity;
import com.nongcang.server.modules.storagecondition.domain.vo.StorageConditionDetailResponse;
import com.nongcang.server.modules.storagecondition.domain.vo.StorageConditionListItemResponse;
import com.nongcang.server.modules.storagecondition.domain.vo.StorageConditionOptionResponse;
import com.nongcang.server.modules.storagecondition.repository.StorageConditionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class StorageConditionService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter STORAGE_CONDITION_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private static final Set<String> LIGHT_REQUIREMENTS = Set.of(
			"无特殊要求",
			"需避强光",
			"避免直射阳光",
			"全避光");

	private static final Set<String> VENTILATION_REQUIREMENTS = Set.of(
			"无特殊要求",
			"普通通风",
			"强通风",
			"密闭");

	private final StorageConditionRepository storageConditionRepository;

	public StorageConditionService(StorageConditionRepository storageConditionRepository) {
		this.storageConditionRepository = storageConditionRepository;
	}

	public List<StorageConditionListItemResponse> getStorageConditionList(StorageConditionListQueryRequest queryRequest) {
		return storageConditionRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<StorageConditionOptionResponse> getStorageConditionOptions() {
		return storageConditionRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new StorageConditionOptionResponse(
						entity.id(),
						entity.conditionName(),
						entity.storageType(),
						entity.status()))
				.toList();
	}

	public StorageConditionDetailResponse getStorageConditionDetail(Long id) {
		return toDetailResponse(getExistingStorageCondition(id));
	}

	@Transactional
	public StorageConditionDetailResponse createStorageCondition(StorageConditionCreateRequest request) {
		validateUniqueName(request.conditionName(), null);
		validateEnumSelections(request.lightRequirement(), request.ventilationRequirement());
		validateRanges(request.temperatureMin(), request.temperatureMax(), request.humidityMin(), request.humidityMax());

		StorageConditionEntity storageConditionEntity = new StorageConditionEntity(
				null,
				generateConditionCode(),
				request.conditionName().trim(),
				request.storageType().trim(),
				request.temperatureMin(),
				request.temperatureMax(),
				request.humidityMin(),
				request.humidityMax(),
				request.lightRequirement().trim(),
				request.ventilationRequirement().trim(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = storageConditionRepository.insert(storageConditionEntity);
		return getStorageConditionDetail(id);
	}

	@Transactional
	public StorageConditionDetailResponse updateStorageCondition(Long id, StorageConditionUpdateRequest request) {
		StorageConditionEntity currentStorageCondition = getExistingStorageCondition(id);

		validateUniqueName(request.conditionName(), id);
		validateEnumSelections(request.lightRequirement(), request.ventilationRequirement());
		validateRanges(request.temperatureMin(), request.temperatureMax(), request.humidityMin(), request.humidityMax());

		StorageConditionEntity updatedStorageCondition = new StorageConditionEntity(
				currentStorageCondition.id(),
				currentStorageCondition.conditionCode(),
				request.conditionName().trim(),
				request.storageType().trim(),
				request.temperatureMin(),
				request.temperatureMax(),
				request.humidityMin(),
				request.humidityMax(),
				request.lightRequirement().trim(),
				request.ventilationRequirement().trim(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentStorageCondition.createdAt(),
				currentStorageCondition.updatedAt());

		storageConditionRepository.update(updatedStorageCondition);
		return getStorageConditionDetail(id);
	}

	@Transactional
	public void updateStorageConditionStatus(Long id, StorageConditionStatusUpdateRequest request) {
		getExistingStorageCondition(id);
		storageConditionRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteStorageCondition(Long id) {
		getExistingStorageCondition(id);

		if (storageConditionRepository.countCategoryReferences(id) > 0) {
			throw new BusinessException(CommonErrorCode.STORAGE_CONDITION_IN_USE);
		}

		storageConditionRepository.deleteById(id);
	}

	private StorageConditionEntity getExistingStorageCondition(Long id) {
		return storageConditionRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.STORAGE_CONDITION_NOT_FOUND));
	}

	private boolean matchesQuery(StorageConditionEntity entity, StorageConditionListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.conditionCode())
				&& !entity.conditionCode().contains(queryRequest.conditionCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.conditionName())
				&& !entity.conditionName().contains(queryRequest.conditionName().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.storageType())
				&& !entity.storageType().contains(queryRequest.storageType().trim())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(String conditionName, Long excludeId) {
		if (storageConditionRepository.existsByConditionName(conditionName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.STORAGE_CONDITION_NAME_DUPLICATED);
		}
	}

	private void validateRanges(
			BigDecimal temperatureMin,
			BigDecimal temperatureMax,
			BigDecimal humidityMin,
			BigDecimal humidityMax) {
		if (temperatureMin != null && temperatureMax != null && temperatureMin.compareTo(temperatureMax) > 0) {
			throw new BusinessException(CommonErrorCode.STORAGE_CONDITION_TEMPERATURE_RANGE_INVALID);
		}

		if (humidityMin != null && humidityMax != null && humidityMin.compareTo(humidityMax) > 0) {
			throw new BusinessException(CommonErrorCode.STORAGE_CONDITION_HUMIDITY_RANGE_INVALID);
		}
	}

	private void validateEnumSelections(String lightRequirement, String ventilationRequirement) {
		if (!LIGHT_REQUIREMENTS.contains(lightRequirement.trim())) {
			throw new BusinessException(CommonErrorCode.STORAGE_CONDITION_LIGHT_REQUIREMENT_INVALID);
		}

		if (!VENTILATION_REQUIREMENTS.contains(ventilationRequirement.trim())) {
			throw new BusinessException(CommonErrorCode.STORAGE_CONDITION_VENTILATION_REQUIREMENT_INVALID);
		}
	}

	private String generateConditionCode() {
		for (int index = 0; index < 20; index += 1) {
			String conditionCode = "SC-" + LocalDateTime.now().format(STORAGE_CONDITION_CODE_FORMATTER);

			if (index > 0) {
				conditionCode += "-" + index;
			}

			if (!storageConditionRepository.existsByConditionCode(conditionCode, null)) {
				return conditionCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "储存条件编号生成失败，请稍后重试");
	}

	private StorageConditionListItemResponse toListItemResponse(StorageConditionEntity entity) {
		return new StorageConditionListItemResponse(
				entity.id(),
				entity.conditionCode(),
				entity.conditionName(),
				entity.storageType(),
				toDouble(entity.temperatureMin()),
					toDouble(entity.temperatureMax()),
					toDouble(entity.humidityMin()),
					toDouble(entity.humidityMax()),
					entity.lightRequirement(),
					entity.ventilationRequirement(),
					entity.status(),
					toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private StorageConditionDetailResponse toDetailResponse(StorageConditionEntity entity) {
		return new StorageConditionDetailResponse(
				entity.id(),
				entity.conditionCode(),
				entity.conditionName(),
				entity.storageType(),
				toDouble(entity.temperatureMin()),
					toDouble(entity.temperatureMax()),
					toDouble(entity.humidityMin()),
					toDouble(entity.humidityMax()),
					entity.lightRequirement(),
					entity.ventilationRequirement(),
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

	private Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
