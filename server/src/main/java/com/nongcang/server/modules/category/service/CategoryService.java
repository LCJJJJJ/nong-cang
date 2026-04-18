package com.nongcang.server.modules.category.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.category.domain.dto.CategoryCreateRequest;
import com.nongcang.server.modules.category.domain.dto.CategoryStatusUpdateRequest;
import com.nongcang.server.modules.category.domain.dto.CategoryTreeQueryRequest;
import com.nongcang.server.modules.category.domain.dto.CategoryUpdateRequest;
import com.nongcang.server.modules.category.domain.entity.CategoryEntity;
import com.nongcang.server.modules.category.domain.vo.CategoryDetailResponse;
import com.nongcang.server.modules.category.domain.vo.CategoryOptionResponse;
import com.nongcang.server.modules.category.domain.vo.CategoryTreeItemResponse;
import com.nongcang.server.modules.category.repository.CategoryRepository;
import com.nongcang.server.modules.storagecondition.repository.StorageConditionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CategoryService {

	private static final int ENABLED = 1;

	private static final int MAX_CATEGORY_LEVEL = 3;

	private static final DateTimeFormatter CATEGORY_CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final CategoryRepository categoryRepository;
	private final StorageConditionRepository storageConditionRepository;

	public CategoryService(
			CategoryRepository categoryRepository,
			StorageConditionRepository storageConditionRepository) {
		this.categoryRepository = categoryRepository;
		this.storageConditionRepository = storageConditionRepository;
	}

	public List<CategoryTreeItemResponse> getCategoryTree(CategoryTreeQueryRequest queryRequest) {
		List<CategoryEntity> categoryEntities = categoryRepository.findAll();
		Map<Long, List<CategoryEntity>> childrenMap = groupChildren(categoryEntities);
		Predicate<CategoryEntity> filter = buildFilter(queryRequest);

		List<CategoryEntity> roots = resolveRootCategories(categoryEntities, childrenMap, queryRequest.parentId());
		return roots.stream()
				.map(categoryEntity -> buildTreeNode(categoryEntity, childrenMap, filter))
				.filter(Objects::nonNull)
				.toList();
	}

	public List<CategoryOptionResponse> getCategoryOptions() {
		List<CategoryEntity> categoryEntities = categoryRepository.findAll();
		Map<Long, List<CategoryEntity>> childrenMap = groupChildren(categoryEntities);
		return childrenMap.getOrDefault(null, List.of())
				.stream()
				.map(categoryEntity -> buildOptionNode(categoryEntity, childrenMap))
				.toList();
	}

	public CategoryDetailResponse getCategoryDetail(Long id) {
		CategoryEntity categoryEntity = getExistingCategory(id);
		String parentName = resolveParentName(categoryEntity.parentId());
		return toDetailResponse(categoryEntity, parentName);
	}

	@Transactional
	public CategoryDetailResponse createCategory(CategoryCreateRequest request) {
		validateUniqueSiblingName(request.parentId(), request.categoryName(), null);

		CategoryEntity parentCategory = resolveParentCategory(request.parentId());
		int categoryLevel = parentCategory == null ? 1 : parentCategory.categoryLevel() + 1;
		validateCategoryLevel(categoryLevel);

		CategoryEntity categoryEntity = new CategoryEntity(
				null,
				generateCategoryCode(),
				request.categoryName().trim(),
				request.parentId(),
				categoryLevel,
				buildAncestorPath(parentCategory),
				request.sortOrder(),
				request.status(),
				resolveStorageConditionId(request.defaultStorageConditionId()),
				null,
				null,
				request.shelfLifeDays(),
				request.warningDays(),
				trimToNull(request.remarks()),
				null,
				null);

		long categoryId = categoryRepository.insert(categoryEntity);
		return getCategoryDetail(categoryId);
	}

	@Transactional
	public CategoryDetailResponse updateCategory(Long id, CategoryUpdateRequest request) {
		CategoryEntity currentCategory = getExistingCategory(id);

		validateUniqueSiblingName(request.parentId(), request.categoryName(), id);
		validateParentRelation(currentCategory, request.parentId());

		CategoryEntity parentCategory = resolveParentCategory(request.parentId());
		int newLevel = parentCategory == null ? 1 : parentCategory.categoryLevel() + 1;
		validateCategoryLevel(newLevel);

		String newAncestorPath = buildAncestorPath(parentCategory);
		CategoryEntity updatedCategory = new CategoryEntity(
				currentCategory.id(),
				currentCategory.categoryCode(),
				request.categoryName().trim(),
				request.parentId(),
				newLevel,
				newAncestorPath,
				request.sortOrder(),
				request.status(),
				resolveStorageConditionId(request.defaultStorageConditionId()),
				null,
				null,
				request.shelfLifeDays(),
				request.warningDays(),
				trimToNull(request.remarks()),
				currentCategory.createdAt(),
				currentCategory.updatedAt());

		categoryRepository.update(updatedCategory);

		if (!Objects.equals(currentCategory.parentId(), request.parentId())) {
			int levelDelta = newLevel - currentCategory.categoryLevel();
			String oldPrefix = currentCategory.ancestorPath() + currentCategory.id() + "/";
			String newPrefix = newAncestorPath + currentCategory.id() + "/";
			categoryRepository.updateSubtreePathAndLevel(oldPrefix, newPrefix, levelDelta, currentCategory.id());
		}

		return getCategoryDetail(id);
	}

	@Transactional
	public void updateCategoryStatus(Long id, CategoryStatusUpdateRequest request) {
		getExistingCategory(id);
		categoryRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteCategory(Long id) {
		getExistingCategory(id);

		if (categoryRepository.countChildren(id) > 0) {
			throw new BusinessException(CommonErrorCode.CATEGORY_HAS_CHILDREN);
		}

		categoryRepository.deleteById(id);
	}

	private Map<Long, List<CategoryEntity>> groupChildren(List<CategoryEntity> categoryEntities) {
		Map<Long, List<CategoryEntity>> groupedChildren = new LinkedHashMap<>();

		for (CategoryEntity categoryEntity : categoryEntities) {
			groupedChildren.computeIfAbsent(categoryEntity.parentId(), key -> new ArrayList<>()).add(categoryEntity);
		}

		groupedChildren.values()
				.forEach(children -> children.sort(Comparator.comparing(CategoryEntity::sortOrder).thenComparing(CategoryEntity::id)));

		return groupedChildren;
	}

	private Predicate<CategoryEntity> buildFilter(CategoryTreeQueryRequest queryRequest) {
		return categoryEntity -> {
			if (StringUtils.hasText(queryRequest.categoryCode())
					&& !categoryEntity.categoryCode().contains(queryRequest.categoryCode().trim())) {
				return false;
			}

			if (StringUtils.hasText(queryRequest.categoryName())
					&& !categoryEntity.categoryName().contains(queryRequest.categoryName().trim())) {
				return false;
			}

			if (queryRequest.level() != null && !queryRequest.level().equals(categoryEntity.categoryLevel())) {
				return false;
			}

			if (queryRequest.status() != null && !queryRequest.status().equals(categoryEntity.status())) {
				return false;
			}

			return true;
		};
	}

	private List<CategoryEntity> resolveRootCategories(
			List<CategoryEntity> categoryEntities,
			Map<Long, List<CategoryEntity>> childrenMap,
			Long parentId) {
		if (parentId == null) {
			return childrenMap.getOrDefault(null, List.of());
		}

		Optional<CategoryEntity> parentCategory = categoryEntities.stream()
				.filter(categoryEntity -> parentId.equals(categoryEntity.id()))
				.findFirst();

		if (parentCategory.isEmpty()) {
			return List.of();
		}

		return childrenMap.getOrDefault(parentId, List.of());
	}

	private CategoryTreeItemResponse buildTreeNode(
			CategoryEntity categoryEntity,
			Map<Long, List<CategoryEntity>> childrenMap,
			Predicate<CategoryEntity> filter) {
		List<CategoryTreeItemResponse> children = childrenMap.getOrDefault(categoryEntity.id(), List.of())
				.stream()
				.map(child -> buildTreeNode(child, childrenMap, filter))
				.filter(Objects::nonNull)
				.toList();

		boolean selfMatched = filter.test(categoryEntity);
		if (!selfMatched && children.isEmpty()) {
			return null;
		}

		return new CategoryTreeItemResponse(
				categoryEntity.id(),
				categoryEntity.categoryCode(),
				categoryEntity.categoryName(),
				categoryEntity.parentId(),
				categoryEntity.categoryLevel(),
				categoryEntity.ancestorPath(),
				categoryEntity.sortOrder(),
				categoryEntity.status(),
				toStatusLabel(categoryEntity.status()),
				categoryEntity.defaultStorageConditionId(),
				categoryEntity.defaultStorageType(),
				categoryEntity.defaultStorageCondition(),
				categoryEntity.shelfLifeDays(),
				categoryEntity.warningDays(),
				categoryEntity.remarks(),
				toIsoDateTime(categoryEntity.createdAt()),
				toIsoDateTime(categoryEntity.updatedAt()),
				children);
	}

	private CategoryOptionResponse buildOptionNode(
			CategoryEntity categoryEntity,
			Map<Long, List<CategoryEntity>> childrenMap) {
		List<CategoryOptionResponse> children = childrenMap.getOrDefault(categoryEntity.id(), List.of())
				.stream()
				.map(child -> buildOptionNode(child, childrenMap))
				.toList();

		return new CategoryOptionResponse(
				categoryEntity.id(),
				categoryEntity.categoryName(),
				categoryEntity.categoryLevel(),
				categoryEntity.ancestorPath(),
				children);
	}

	private CategoryEntity getExistingCategory(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.CATEGORY_NOT_FOUND));
	}

	private CategoryEntity resolveParentCategory(Long parentId) {
		if (parentId == null) {
			return null;
		}

		return categoryRepository.findById(parentId)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.CATEGORY_PARENT_NOT_FOUND));
	}

	private void validateUniqueSiblingName(Long parentId, String categoryName, Long excludeId) {
		if (categoryRepository.existsSiblingName(parentId, categoryName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.CATEGORY_NAME_DUPLICATED);
		}
	}

	private void validateParentRelation(CategoryEntity currentCategory, Long nextParentId) {
		if (nextParentId == null) {
			return;
		}

		if (nextParentId.equals(currentCategory.id())) {
			throw new BusinessException(CommonErrorCode.CATEGORY_PARENT_INVALID);
		}

		CategoryEntity nextParentCategory = resolveParentCategory(nextParentId);
		String currentPathPrefix = currentCategory.ancestorPath() + currentCategory.id() + "/";

		if (nextParentCategory.ancestorPath().startsWith(currentPathPrefix)) {
			throw new BusinessException(CommonErrorCode.CATEGORY_PARENT_INVALID);
		}
	}

	private void validateCategoryLevel(int categoryLevel) {
		if (categoryLevel > MAX_CATEGORY_LEVEL) {
			throw new BusinessException(CommonErrorCode.CATEGORY_LEVEL_EXCEEDED);
		}
	}

	private String buildAncestorPath(CategoryEntity parentCategory) {
		if (parentCategory == null) {
			return "/";
		}

		return parentCategory.ancestorPath() + parentCategory.id() + "/";
	}

	private String resolveParentName(Long parentId) {
		if (parentId == null) {
			return null;
		}

		return categoryRepository.findById(parentId).map(CategoryEntity::categoryName).orElse(null);
	}

	private CategoryDetailResponse toDetailResponse(CategoryEntity categoryEntity, String parentName) {
		return new CategoryDetailResponse(
				categoryEntity.id(),
				categoryEntity.categoryCode(),
				categoryEntity.categoryName(),
				categoryEntity.parentId(),
				parentName,
				categoryEntity.categoryLevel(),
				categoryEntity.ancestorPath(),
				categoryEntity.sortOrder(),
				categoryEntity.status(),
				toStatusLabel(categoryEntity.status()),
				categoryEntity.defaultStorageConditionId(),
				categoryEntity.defaultStorageType(),
				categoryEntity.defaultStorageCondition(),
				categoryEntity.shelfLifeDays(),
				categoryEntity.warningDays(),
				categoryEntity.remarks(),
				toIsoDateTime(categoryEntity.createdAt()),
				toIsoDateTime(categoryEntity.updatedAt()));
	}

	private Long resolveStorageConditionId(Long storageConditionId) {
		if (storageConditionId == null) {
			return null;
		}

		return storageConditionRepository.findById(storageConditionId)
				.map(storageCondition -> storageCondition.id())
				.orElseThrow(() -> new BusinessException(CommonErrorCode.STORAGE_CONDITION_NOT_FOUND));
	}

	private String toStatusLabel(Integer status) {
		return ENABLED == status ? "启用" : "停用";
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}

		return localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private String generateCategoryCode() {
		for (int index = 0; index < 20; index += 1) {
			String categoryCode = "CAT-" + LocalDateTime.now().format(CATEGORY_CODE_FORMATTER);

			if (index > 0) {
				categoryCode += "-" + index;
			}

			if (!categoryRepository.existsByCategoryCode(categoryCode, null)) {
				return categoryCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "分类编号生成失败，请稍后重试");
	}
}
