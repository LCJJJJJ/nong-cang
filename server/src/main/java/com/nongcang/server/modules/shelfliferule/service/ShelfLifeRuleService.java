package com.nongcang.server.modules.shelfliferule.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.category.repository.CategoryRepository;
import com.nongcang.server.modules.shelfliferule.domain.dto.ShelfLifeRuleCreateRequest;
import com.nongcang.server.modules.shelfliferule.domain.dto.ShelfLifeRuleListQueryRequest;
import com.nongcang.server.modules.shelfliferule.domain.dto.ShelfLifeRuleStatusUpdateRequest;
import com.nongcang.server.modules.shelfliferule.domain.dto.ShelfLifeRuleUpdateRequest;
import com.nongcang.server.modules.shelfliferule.domain.entity.ShelfLifeRuleEntity;
import com.nongcang.server.modules.shelfliferule.domain.vo.ShelfLifeRuleDetailResponse;
import com.nongcang.server.modules.shelfliferule.domain.vo.ShelfLifeRuleListItemResponse;
import com.nongcang.server.modules.shelfliferule.domain.vo.ShelfLifeRuleOptionResponse;
import com.nongcang.server.modules.shelfliferule.repository.ShelfLifeRuleRepository;
import com.nongcang.server.modules.storagecondition.repository.StorageConditionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ShelfLifeRuleService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter SHELF_LIFE_RULE_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final ShelfLifeRuleRepository shelfLifeRuleRepository;
	private final CategoryRepository categoryRepository;
	private final StorageConditionRepository storageConditionRepository;

	public ShelfLifeRuleService(
			ShelfLifeRuleRepository shelfLifeRuleRepository,
			CategoryRepository categoryRepository,
			StorageConditionRepository storageConditionRepository) {
		this.shelfLifeRuleRepository = shelfLifeRuleRepository;
		this.categoryRepository = categoryRepository;
		this.storageConditionRepository = storageConditionRepository;
	}

	public List<ShelfLifeRuleListItemResponse> getShelfLifeRuleList(ShelfLifeRuleListQueryRequest queryRequest) {
		return shelfLifeRuleRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<ShelfLifeRuleOptionResponse> getShelfLifeRuleOptions() {
		return shelfLifeRuleRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new ShelfLifeRuleOptionResponse(
						entity.id(),
						entity.ruleName(),
						entity.shelfLifeDays(),
						entity.warningDays(),
						entity.status()))
				.toList();
	}

	public ShelfLifeRuleDetailResponse getShelfLifeRuleDetail(Long id) {
		return toDetailResponse(getExistingShelfLifeRule(id));
	}

	@Transactional
	public ShelfLifeRuleDetailResponse createShelfLifeRule(ShelfLifeRuleCreateRequest request) {
		validateUniqueName(request.ruleName(), null);
		resolveCategoryId(request.categoryId());
		resolveStorageConditionId(request.storageConditionId());

		ShelfLifeRuleEntity shelfLifeRuleEntity = new ShelfLifeRuleEntity(
				null,
				generateRuleCode(),
				request.ruleName().trim(),
				request.categoryId(),
				null,
				request.storageConditionId(),
				null,
				null,
				request.shelfLifeDays(),
				request.warningDays(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = shelfLifeRuleRepository.insert(shelfLifeRuleEntity);
		return getShelfLifeRuleDetail(id);
	}

	@Transactional
	public ShelfLifeRuleDetailResponse updateShelfLifeRule(Long id, ShelfLifeRuleUpdateRequest request) {
		ShelfLifeRuleEntity currentShelfLifeRule = getExistingShelfLifeRule(id);

		validateUniqueName(request.ruleName(), id);
		resolveCategoryId(request.categoryId());
		resolveStorageConditionId(request.storageConditionId());

		ShelfLifeRuleEntity updatedShelfLifeRule = new ShelfLifeRuleEntity(
				currentShelfLifeRule.id(),
				currentShelfLifeRule.ruleCode(),
				request.ruleName().trim(),
				request.categoryId(),
				null,
				request.storageConditionId(),
				null,
				null,
				request.shelfLifeDays(),
				request.warningDays(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentShelfLifeRule.createdAt(),
				currentShelfLifeRule.updatedAt());

		shelfLifeRuleRepository.update(updatedShelfLifeRule);
		return getShelfLifeRuleDetail(id);
	}

	@Transactional
	public void updateShelfLifeRuleStatus(Long id, ShelfLifeRuleStatusUpdateRequest request) {
		getExistingShelfLifeRule(id);
		shelfLifeRuleRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteShelfLifeRule(Long id) {
		getExistingShelfLifeRule(id);
		shelfLifeRuleRepository.deleteById(id);
	}

	private ShelfLifeRuleEntity getExistingShelfLifeRule(Long id) {
		return shelfLifeRuleRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.SHELF_LIFE_RULE_NOT_FOUND));
	}

	private boolean matchesQuery(ShelfLifeRuleEntity entity, ShelfLifeRuleListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.ruleCode())
				&& !entity.ruleCode().contains(queryRequest.ruleCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.ruleName())
				&& !entity.ruleName().contains(queryRequest.ruleName().trim())) {
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

	private void validateUniqueName(String ruleName, Long excludeId) {
		if (shelfLifeRuleRepository.existsByRuleName(ruleName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.SHELF_LIFE_RULE_NAME_DUPLICATED);
		}
	}

	private Long resolveCategoryId(Long categoryId) {
		if (categoryId == null) {
			return null;
		}

		return categoryRepository.findById(categoryId)
				.map(category -> category.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.CATEGORY_NOT_FOUND));
	}

	private Long resolveStorageConditionId(Long storageConditionId) {
		if (storageConditionId == null) {
			return null;
		}

		return storageConditionRepository.findById(storageConditionId)
				.map(storageCondition -> storageCondition.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.STORAGE_CONDITION_NOT_FOUND));
	}

	private String generateRuleCode() {
		for (int index = 0; index < 20; index += 1) {
			String ruleCode = "RULE-" + LocalDateTime.now().format(SHELF_LIFE_RULE_CODE_FORMATTER);

			if (index > 0) {
				ruleCode += "-" + index;
			}

			if (!shelfLifeRuleRepository.existsByRuleCode(ruleCode, null)) {
				return ruleCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "保质期规则编号生成失败，请稍后重试");
	}

	private ShelfLifeRuleListItemResponse toListItemResponse(ShelfLifeRuleEntity entity) {
		return new ShelfLifeRuleListItemResponse(
				entity.id(),
				entity.ruleCode(),
				entity.ruleName(),
				entity.categoryId(),
				entity.categoryName(),
				entity.storageConditionId(),
				entity.storageConditionName(),
				entity.storageType(),
				entity.shelfLifeDays(),
				entity.warningDays(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private ShelfLifeRuleDetailResponse toDetailResponse(ShelfLifeRuleEntity entity) {
		return new ShelfLifeRuleDetailResponse(
				entity.id(),
				entity.ruleCode(),
				entity.ruleName(),
				entity.categoryId(),
				entity.categoryName(),
				entity.storageConditionId(),
				entity.storageConditionName(),
				entity.storageType(),
				entity.shelfLifeDays(),
				entity.warningDays(),
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
