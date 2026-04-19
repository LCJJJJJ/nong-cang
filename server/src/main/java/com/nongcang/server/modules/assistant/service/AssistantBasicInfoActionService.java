package com.nongcang.server.modules.assistant.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.assistant.domain.entity.AssistantActionPlanEntity;
import com.nongcang.server.modules.assistant.repository.AssistantActionPlanRepository;
import com.nongcang.server.modules.category.domain.dto.CategoryCreateRequest;
import com.nongcang.server.modules.category.domain.dto.CategoryUpdateRequest;
import com.nongcang.server.modules.category.domain.entity.CategoryEntity;
import com.nongcang.server.modules.category.repository.CategoryRepository;
import com.nongcang.server.modules.category.service.CategoryService;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveCreateRequest;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveUpdateRequest;
import com.nongcang.server.modules.productarchive.domain.entity.ProductArchiveEntity;
import com.nongcang.server.modules.productarchive.repository.ProductArchiveRepository;
import com.nongcang.server.modules.productarchive.service.ProductArchiveService;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginCreateRequest;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginUpdateRequest;
import com.nongcang.server.modules.productorigin.domain.entity.ProductOriginEntity;
import com.nongcang.server.modules.productorigin.repository.ProductOriginRepository;
import com.nongcang.server.modules.productorigin.service.ProductOriginService;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitCreateRequest;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitUpdateRequest;
import com.nongcang.server.modules.productunit.domain.entity.ProductUnitEntity;
import com.nongcang.server.modules.productunit.repository.ProductUnitRepository;
import com.nongcang.server.modules.productunit.service.ProductUnitService;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeCreateRequest;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeUpdateRequest;
import com.nongcang.server.modules.qualitygrade.domain.entity.QualityGradeEntity;
import com.nongcang.server.modules.qualitygrade.repository.QualityGradeRepository;
import com.nongcang.server.modules.qualitygrade.service.QualityGradeService;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionCreateRequest;
import com.nongcang.server.modules.storagecondition.domain.dto.StorageConditionUpdateRequest;
import com.nongcang.server.modules.storagecondition.domain.entity.StorageConditionEntity;
import com.nongcang.server.modules.storagecondition.repository.StorageConditionRepository;
import com.nongcang.server.modules.storagecondition.service.StorageConditionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AssistantBasicInfoActionService {

	private static final DateTimeFormatter ACTION_CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final AssistantActionPlanRepository assistantActionPlanRepository;
	private final CategoryRepository categoryRepository;
	private final ProductArchiveRepository productArchiveRepository;
	private final ProductUnitRepository productUnitRepository;
	private final ProductOriginRepository productOriginRepository;
	private final StorageConditionRepository storageConditionRepository;
	private final QualityGradeRepository qualityGradeRepository;
	private final CategoryService categoryService;
	private final ProductArchiveService productArchiveService;
	private final ProductUnitService productUnitService;
	private final ProductOriginService productOriginService;
	private final StorageConditionService storageConditionService;
	private final QualityGradeService qualityGradeService;
	private final ObjectMapper objectMapper;

	public AssistantBasicInfoActionService(
			AssistantActionPlanRepository assistantActionPlanRepository,
			CategoryRepository categoryRepository,
			ProductArchiveRepository productArchiveRepository,
			ProductUnitRepository productUnitRepository,
			ProductOriginRepository productOriginRepository,
			StorageConditionRepository storageConditionRepository,
			QualityGradeRepository qualityGradeRepository,
			CategoryService categoryService,
			ProductArchiveService productArchiveService,
			ProductUnitService productUnitService,
			ProductOriginService productOriginService,
			StorageConditionService storageConditionService,
			QualityGradeService qualityGradeService,
			ObjectMapper objectMapper) {
		this.assistantActionPlanRepository = assistantActionPlanRepository;
		this.categoryRepository = categoryRepository;
		this.productArchiveRepository = productArchiveRepository;
		this.productUnitRepository = productUnitRepository;
		this.productOriginRepository = productOriginRepository;
		this.storageConditionRepository = storageConditionRepository;
		this.qualityGradeRepository = qualityGradeRepository;
		this.categoryService = categoryService;
		this.productArchiveService = productArchiveService;
		this.productUnitService = productUnitService;
		this.productOriginService = productOriginService;
		this.storageConditionService = storageConditionService;
		this.qualityGradeService = qualityGradeService;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public AssistantActionPlanResult prepareAction(
			Long sessionId,
			Long userId,
			AssistantWriteActionToolArguments arguments) {
		AssistantActionPlanEntity currentPlan = resolveCurrentPlan(sessionId, userId, arguments);
		String resourceType = normalizeResourceType(arguments.resourceType(), currentPlan);
		String actionType = normalizeActionType(arguments.actionType(), currentPlan);
		Map<String, Object> mergedFields = mergeFields(currentPlan, arguments.fields());
		String targetInput = StringUtils.hasText(arguments.target())
				? arguments.target().trim()
				: currentPlan == null ? null : currentPlan.targetLabel();

		ResolvedTarget resolvedTarget = resolveTarget(resourceType, targetInput);
		mergedFields = normalizeFields(resourceType, actionType, resolvedTarget, mergedFields);
		List<AssistantActionFieldPrompt> missingFields = resolveMissingFields(resourceType, actionType, mergedFields, resolvedTarget);

		String status;
		String summary;
		String riskLevel = "DELETE".equals(actionType) ? "HIGH" : "MEDIUM";
		String confirmationMode = "DELETE".equals(actionType) ? "CONFIRM_DELETE_TEXT" : "CONFIRM_CARD";
		String errorCode = null;
		String errorMessage = null;

		if ("DELETE".equals(actionType)) {
			String deletePrecheckMessage = precheckDelete(resourceType, resolvedTarget);
			if (deletePrecheckMessage != null) {
				status = "FAILED";
				summary = deletePrecheckMessage;
				errorCode = CommonErrorCode.BUSINESS_RULE_VIOLATION.code();
				errorMessage = deletePrecheckMessage;
			} else {
				status = "READY";
				summary = "已整理好删除操作预览，请输入“确认删除”后执行。";
			}
		} else if (!missingFields.isEmpty()) {
			status = "DRAFT";
			summary = "还缺少以下字段：" + String.join("、", missingFields.stream().map(AssistantActionFieldPrompt::label).toList());
		} else {
			status = "READY";
			summary = "已整理好" + actionLabel(actionType) + resourceLabel(resourceType) + "的操作预览，请确认执行。";
		}

		AssistantActionPlanEntity planToSave = new AssistantActionPlanEntity(
				currentPlan == null ? null : currentPlan.id(),
				currentPlan == null ? buildActionCode() : currentPlan.actionCode(),
				sessionId,
				userId,
				resourceType,
				actionType,
				resolvedTarget == null ? null : resolvedTarget.id(),
				resolveTargetLabel(resourceType, actionType, mergedFields, resolvedTarget),
				serialize(mergedFields),
				serialize(missingFields),
				riskLevel,
				confirmationMode,
				status,
				summary,
				errorCode,
				errorMessage,
				currentPlan == null ? null : currentPlan.createdAt(),
				currentPlan == null ? null : currentPlan.updatedAt(),
				null);

		if (currentPlan == null) {
			long id = assistantActionPlanRepository.insert(planToSave);
			planToSave = assistantActionPlanRepository.findByActionCodeAndUserId(planToSave.actionCode(), userId)
					.orElseThrow(() -> new BusinessException(CommonErrorCode.ASSISTANT_ACTION_PLAN_NOT_FOUND));
		} else {
			assistantActionPlanRepository.update(planToSave);
		}

		AssistantActionCard actionCard = buildActionCard(resourceType, actionType, planToSave.actionCode(), planToSave.targetLabel(),
				summary, riskLevel, confirmationMode, status, missingFields, buildPreviewFields(resourceType, actionType, mergedFields, resolvedTarget));

		return new AssistantActionPlanResult(
				planToSave.actionCode(),
				status,
				summary,
				actionCard,
				List.of());
	}

	@Transactional
	public AssistantActionExecutionResult executeAction(String actionCode, Long userId, String confirmationText) {
		AssistantActionPlanEntity plan = assistantActionPlanRepository.findByActionCodeAndUserId(actionCode, userId)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.ASSISTANT_ACTION_PLAN_NOT_FOUND));

		if (!"READY".equals(plan.status())) {
			throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, plan.summary());
		}

		if ("CONFIRM_DELETE_TEXT".equals(plan.confirmationMode())
				&& !"确认删除".equals(trimToNull(confirmationText))) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_ACTION_CONFIRMATION_INVALID);
		}

		Map<String, Object> fields = deserializeMap(plan.fieldsJson());
		String message = switch (plan.resourceType()) {
			case "category" -> executeCategory(plan, fields);
			case "product_archive" -> executeProductArchive(plan, fields);
			case "product_unit" -> executeProductUnit(plan, fields);
			case "product_origin" -> executeProductOrigin(plan, fields);
			case "storage_condition" -> executeStorageCondition(plan, fields);
			case "quality_grade" -> executeQualityGrade(plan, fields);
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "写操作资源类型不正确");
		};

		AssistantActionPlanEntity executedPlan = new AssistantActionPlanEntity(
				plan.id(),
				plan.actionCode(),
				plan.sessionId(),
				plan.userId(),
				plan.resourceType(),
				plan.actionType(),
				plan.targetId(),
				plan.targetLabel(),
				plan.fieldsJson(),
				plan.missingFieldsJson(),
				plan.riskLevel(),
				plan.confirmationMode(),
				"EXECUTED",
				message,
				null,
				null,
				plan.createdAt(),
				plan.updatedAt(),
				LocalDateTime.now());
		assistantActionPlanRepository.update(executedPlan);

		AssistantActionCard actionCard = buildActionCard(
				plan.resourceType(),
				plan.actionType(),
				plan.actionCode(),
				plan.targetLabel(),
				message,
				plan.riskLevel(),
				plan.confirmationMode(),
				"EXECUTED",
				List.of(),
				buildPreviewFields(plan.resourceType(), plan.actionType(), fields,
						new ResolvedTarget(plan.targetId(), plan.targetLabel())));

		return new AssistantActionExecutionResult("EXECUTED", message, actionCard);
	}

	public Optional<AssistantActionPlanEntity> getLatestOpenPlan(Long sessionId, Long userId) {
		return assistantActionPlanRepository.findLatestOpenPlan(sessionId, userId);
	}

	private AssistantActionPlanEntity resolveCurrentPlan(
			Long sessionId,
			Long userId,
			AssistantWriteActionToolArguments arguments) {
		if (StringUtils.hasText(arguments.actionCode())) {
			return assistantActionPlanRepository.findByActionCodeAndUserId(arguments.actionCode(), userId)
					.orElseThrow(() -> new BusinessException(CommonErrorCode.ASSISTANT_ACTION_PLAN_NOT_FOUND));
		}

		if (!StringUtils.hasText(arguments.resourceType()) || !StringUtils.hasText(arguments.actionType())) {
			return assistantActionPlanRepository.findLatestOpenPlan(sessionId, userId).orElse(null);
		}

		return null;
	}

	private String normalizeResourceType(String resourceType, AssistantActionPlanEntity currentPlan) {
		String value = StringUtils.hasText(resourceType) ? resourceType.trim().toLowerCase(Locale.ROOT) : null;
		if (!StringUtils.hasText(value) && currentPlan != null) {
			value = currentPlan.resourceType();
		}
		if (!StringUtils.hasText(value)) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "缺少资源类型");
		}
		return value;
	}

	private String normalizeActionType(String actionType, AssistantActionPlanEntity currentPlan) {
		String value = StringUtils.hasText(actionType) ? actionType.trim().toUpperCase(Locale.ROOT) : null;
		if (!StringUtils.hasText(value) && currentPlan != null) {
			value = currentPlan.actionType();
		}
		if (!StringUtils.hasText(value)) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "缺少动作类型");
		}
		return switch (value) {
			case "CREATE", "UPDATE", "DELETE" -> value;
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "动作类型不正确");
		};
	}

	private Map<String, Object> mergeFields(AssistantActionPlanEntity currentPlan, Map<String, Object> incomingFields) {
		Map<String, Object> mergedFields = currentPlan == null ? new LinkedHashMap<>() : deserializeMap(currentPlan.fieldsJson());
		if (incomingFields != null) {
			incomingFields.forEach((key, value) -> {
				if (value != null && StringUtils.hasText(String.valueOf(value))) {
					mergedFields.put(key, value);
				}
			});
		}
		return mergedFields;
	}

	private Map<String, Object> normalizeFields(
			String resourceType,
			String actionType,
			ResolvedTarget resolvedTarget,
			Map<String, Object> mergedFields) {
		Map<String, Object> normalizedFields = canonicalizeFields(resourceType, mergedFields);
		resolveReferenceFields(resourceType, normalizedFields);
		normalizePrimitiveValues(normalizedFields);

		if (!"UPDATE".equals(actionType) || resolvedTarget == null) {
			return normalizedFields;
		}

		return switch (resourceType) {
			case "category" -> mergeCategoryDefaults(resolvedTarget.id(), normalizedFields);
			case "product_archive" -> mergeProductArchiveDefaults(resolvedTarget.id(), normalizedFields);
			case "product_unit" -> mergeProductUnitDefaults(resolvedTarget.id(), normalizedFields);
			case "product_origin" -> mergeProductOriginDefaults(resolvedTarget.id(), normalizedFields);
			case "storage_condition" -> mergeStorageConditionDefaults(resolvedTarget.id(), normalizedFields);
			case "quality_grade" -> mergeQualityGradeDefaults(resolvedTarget.id(), normalizedFields);
			default -> normalizedFields;
		};
	}

	private void resolveReferenceFields(String resourceType, Map<String, Object> fields) {
		switch (resourceType) {
			case "category" -> {
				resolveReferenceField(fields, "parentId", this::resolveCategoryTarget);
				resolveReferenceField(fields, "defaultStorageConditionId", this::resolveStorageConditionTarget);
			}
			case "product_archive" -> {
				resolveReferenceField(fields, "categoryId", this::resolveCategoryTarget);
				resolveReferenceField(fields, "unitId", this::resolveProductUnitTarget);
				resolveReferenceField(fields, "originId", this::resolveProductOriginTarget);
				resolveReferenceField(fields, "storageConditionId", this::resolveStorageConditionTarget);
				resolveReferenceField(fields, "qualityGradeId", this::resolveQualityGradeTarget);
			}
			default -> {
			}
		}
	}

	private Map<String, Object> canonicalizeFields(String resourceType, Map<String, Object> sourceFields) {
		Map<String, Object> normalizedFields = new LinkedHashMap<>();
		sourceFields.forEach((rawKey, value) -> normalizedFields.put(aliasKey(resourceType, rawKey), value));
		return normalizedFields;
	}

	private String aliasKey(String resourceType, String rawKey) {
		String key = rawKey == null ? "" : rawKey.trim();
		return switch (resourceType) {
			case "category" -> switch (key) {
				case "name", "分类名称", "新名称", "新分类名称" -> "categoryName";
				case "parent", "parentCategory", "上级分类" -> "parentId";
				case "defaultStorageCondition", "储存条件", "默认储存条件" -> "defaultStorageConditionId";
				case "保质期天数" -> "shelfLifeDays";
				case "预警天数" -> "warningDays";
				case "排序" -> "sortOrder";
				case "状态" -> "status";
				case "remark" -> "remarks";
				case "备注" -> "remarks";
				default -> key;
			};
			case "product_archive" -> switch (key) {
				case "name", "产品名称", "新名称" -> "productName";
				case "specification", "规格", "产品规格" -> "productSpecification";
				case "category", "分类", "产品分类" -> "categoryId";
				case "unit", "单位", "产品单位" -> "unitId";
				case "origin", "产地", "产地信息" -> "originId";
				case "storageCondition", "储存条件" -> "storageConditionId";
				case "shelfLife", "保质期天数" -> "shelfLifeDays";
				case "warningDays", "预警天数", "预警提前天数" -> "warningDays";
				case "qualityGrade", "品质等级" -> "qualityGradeId";
				case "排序", "排序值" -> "sortOrder";
				case "状态" -> "status";
				case "remark" -> "remarks";
				case "备注" -> "remarks";
				default -> key;
			};
			case "product_unit" -> switch (key) {
				case "name", "名称", "单位名称", "新名称" -> "unitName";
				case "symbol", "单位符号" -> "unitSymbol";
				case "type", "单位类型" -> "unitType";
				case "precision", "精度", "精度位数" -> "precisionDigits";
				case "排序", "排序值" -> "sortOrder";
				case "状态" -> "status";
				case "remark" -> "remarks";
				case "备注" -> "remarks";
				default -> key;
			};
			case "product_origin" -> switch (key) {
				case "name", "名称", "产地名称", "新名称" -> "originName";
				case "country", "国家", "国家名称" -> "countryName";
				case "province", "省份", "省份名称" -> "provinceName";
				case "city", "城市", "城市名称" -> "cityName";
				case "排序", "排序值" -> "sortOrder";
				case "状态" -> "status";
				case "remark" -> "remarks";
				case "备注" -> "remarks";
				default -> key;
			};
			case "storage_condition" -> switch (key) {
				case "name", "名称", "条件名称", "新名称" -> "conditionName";
				case "type", "储存类型" -> "storageType";
				case "最低温度" -> "temperatureMin";
				case "最高温度" -> "temperatureMax";
				case "最低湿度" -> "humidityMin";
				case "最高湿度" -> "humidityMax";
				case "避光", "避光要求" -> "lightRequirement";
				case "通风", "通风要求" -> "ventilationRequirement";
				case "排序", "排序值" -> "sortOrder";
				case "状态" -> "status";
				case "remark" -> "remarks";
				case "备注" -> "remarks";
				default -> key;
			};
			case "quality_grade" -> switch (key) {
				case "name", "名称", "等级名称", "新名称" -> "gradeName";
				case "最低分", "最小分值", "minScore" -> "scoreMin";
				case "最高分", "最大分值", "maxScore" -> "scoreMax";
				case "排序", "排序值" -> "sortOrder";
				case "状态" -> "status";
				case "remark" -> "remarks";
				case "备注" -> "remarks";
				default -> key;
			};
			default -> key;
		};
	}

	private void normalizePrimitiveValues(Map<String, Object> fields) {
		if (fields.containsKey("status")) {
			fields.put("status", normalizeStatus(fields.get("status")));
		}
	}

	private Object normalizeStatus(Object rawValue) {
		if (rawValue == null) {
			return null;
		}
		if (rawValue instanceof Number) {
			return rawValue;
		}
		String text = String.valueOf(rawValue).trim();
		return switch (text) {
			case "启用", "enabled", "ENABLED" -> 1;
			case "停用", "禁用", "disabled", "DISABLED" -> 0;
			default -> rawValue;
		};
	}

	private void resolveReferenceField(
			Map<String, Object> fields,
			String field,
			java.util.function.Function<String, ResolvedTarget> resolver) {
		Object rawValue = fields.get(field);
		if (rawValue == null) {
			return;
		}
		if (rawValue instanceof Number) {
			return;
		}
		ResolvedTarget target = resolver.apply(String.valueOf(rawValue).trim());
		if (target != null) {
			fields.put(field, target.id());
		}
	}

	private Map<String, Object> mergeCategoryDefaults(Long id, Map<String, Object> fields) {
		CategoryEntity current = categoryRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.CATEGORY_NOT_FOUND));
		fields.putIfAbsent("categoryName", current.categoryName());
		fields.putIfAbsent("parentId", current.parentId());
		fields.putIfAbsent("sortOrder", current.sortOrder());
		fields.putIfAbsent("status", current.status());
		fields.putIfAbsent("defaultStorageConditionId", current.defaultStorageConditionId());
		fields.putIfAbsent("shelfLifeDays", current.shelfLifeDays());
		fields.putIfAbsent("warningDays", current.warningDays());
		fields.putIfAbsent("remarks", current.remarks());
		return fields;
	}

	private Map<String, Object> mergeProductArchiveDefaults(Long id, Map<String, Object> fields) {
		ProductArchiveEntity current = productArchiveRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ARCHIVE_NOT_FOUND));
		fields.putIfAbsent("productName", current.productName());
		fields.putIfAbsent("productSpecification", current.productSpecification());
		fields.putIfAbsent("categoryId", current.categoryId());
		fields.putIfAbsent("unitId", current.unitId());
		fields.putIfAbsent("originId", current.originId());
		fields.putIfAbsent("storageConditionId", current.storageConditionId());
		fields.putIfAbsent("shelfLifeDays", current.shelfLifeDays());
		fields.putIfAbsent("warningDays", current.warningDays());
		fields.putIfAbsent("qualityGradeId", current.qualityGradeId());
		fields.putIfAbsent("status", current.status());
		fields.putIfAbsent("sortOrder", current.sortOrder());
		fields.putIfAbsent("remarks", current.remarks());
		return fields;
	}

	private Map<String, Object> mergeProductUnitDefaults(Long id, Map<String, Object> fields) {
		ProductUnitEntity current = productUnitRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_UNIT_NOT_FOUND));
		fields.putIfAbsent("unitName", current.unitName());
		fields.putIfAbsent("unitSymbol", current.unitSymbol());
		fields.putIfAbsent("unitType", current.unitType());
		fields.putIfAbsent("precisionDigits", current.precisionDigits());
		fields.putIfAbsent("status", current.status());
		fields.putIfAbsent("sortOrder", current.sortOrder());
		fields.putIfAbsent("remarks", current.remarks());
		return fields;
	}

	private Map<String, Object> mergeProductOriginDefaults(Long id, Map<String, Object> fields) {
		ProductOriginEntity current = productOriginRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.PRODUCT_ORIGIN_NOT_FOUND));
		fields.putIfAbsent("originName", current.originName());
		fields.putIfAbsent("countryName", current.countryName());
		fields.putIfAbsent("provinceName", current.provinceName());
		fields.putIfAbsent("cityName", current.cityName());
		fields.putIfAbsent("status", current.status());
		fields.putIfAbsent("sortOrder", current.sortOrder());
		fields.putIfAbsent("remarks", current.remarks());
		return fields;
	}

	private Map<String, Object> mergeStorageConditionDefaults(Long id, Map<String, Object> fields) {
		StorageConditionEntity current = storageConditionRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.STORAGE_CONDITION_NOT_FOUND));
		fields.putIfAbsent("conditionName", current.conditionName());
		fields.putIfAbsent("storageType", current.storageType());
		fields.putIfAbsent("temperatureMin", current.temperatureMin());
		fields.putIfAbsent("temperatureMax", current.temperatureMax());
		fields.putIfAbsent("humidityMin", current.humidityMin());
		fields.putIfAbsent("humidityMax", current.humidityMax());
		fields.putIfAbsent("lightRequirement", current.lightRequirement());
		fields.putIfAbsent("ventilationRequirement", current.ventilationRequirement());
		fields.putIfAbsent("status", current.status());
		fields.putIfAbsent("sortOrder", current.sortOrder());
		fields.putIfAbsent("remarks", current.remarks());
		return fields;
	}

	private Map<String, Object> mergeQualityGradeDefaults(Long id, Map<String, Object> fields) {
		QualityGradeEntity current = qualityGradeRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.QUALITY_GRADE_NOT_FOUND));
		fields.putIfAbsent("gradeName", current.gradeName());
		fields.putIfAbsent("scoreMin", current.scoreMin());
		fields.putIfAbsent("scoreMax", current.scoreMax());
		fields.putIfAbsent("status", current.status());
		fields.putIfAbsent("sortOrder", current.sortOrder());
		fields.putIfAbsent("remarks", current.remarks());
		return fields;
	}

	private ResolvedTarget resolveTarget(String resourceType, String target) {
		if (!StringUtils.hasText(target)) {
			return null;
		}

		return switch (resourceType) {
			case "category" -> resolveCategoryTarget(target);
			case "product_archive" -> resolveProductArchiveTarget(target);
			case "product_unit" -> resolveProductUnitTarget(target);
			case "product_origin" -> resolveProductOriginTarget(target);
			case "storage_condition" -> resolveStorageConditionTarget(target);
			case "quality_grade" -> resolveQualityGradeTarget(target);
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "资源类型不正确");
		};
	}

	private List<AssistantActionFieldPrompt> resolveMissingFields(
			String resourceType,
			String actionType,
			Map<String, Object> fields,
			ResolvedTarget resolvedTarget) {
		List<AssistantActionFieldPrompt> missingFields = new ArrayList<>();

		if (("UPDATE".equals(actionType) || "DELETE".equals(actionType)) && resolvedTarget == null) {
			missingFields.add(new AssistantActionFieldPrompt("target", "操作目标", "请提供更明确的名称或编号"));
			return missingFields;
		}

		if ("DELETE".equals(actionType)) {
			return missingFields;
		}

		switch (resourceType) {
			case "category" -> {
				requireString(fields, missingFields, "categoryName", "分类名称");
				requireInteger(fields, missingFields, "sortOrder", "排序值");
				requireInteger(fields, missingFields, "status", "状态");
			}
			case "product_archive" -> {
				requireString(fields, missingFields, "productName", "产品名称");
				requireReference(fields, missingFields, "categoryId", "产品分类");
				requireReference(fields, missingFields, "unitId", "产品单位");
				requireReference(fields, missingFields, "originId", "产地信息");
				requireReference(fields, missingFields, "storageConditionId", "储存条件");
				requireInteger(fields, missingFields, "shelfLifeDays", "保质期天数");
				requireInteger(fields, missingFields, "warningDays", "预警提前天数");
				requireReference(fields, missingFields, "qualityGradeId", "品质等级");
				requireInteger(fields, missingFields, "status", "状态");
				requireInteger(fields, missingFields, "sortOrder", "排序值");
			}
			case "product_unit" -> {
				requireString(fields, missingFields, "unitName", "单位名称");
				requireString(fields, missingFields, "unitSymbol", "单位符号");
				requireString(fields, missingFields, "unitType", "单位类型");
				requireInteger(fields, missingFields, "precisionDigits", "精度位数");
				requireInteger(fields, missingFields, "status", "状态");
				requireInteger(fields, missingFields, "sortOrder", "排序值");
			}
			case "product_origin" -> {
				requireString(fields, missingFields, "originName", "产地名称");
				requireString(fields, missingFields, "countryName", "国家名称");
				requireString(fields, missingFields, "provinceName", "省份名称");
				requireInteger(fields, missingFields, "status", "状态");
				requireInteger(fields, missingFields, "sortOrder", "排序值");
			}
			case "storage_condition" -> {
				requireString(fields, missingFields, "conditionName", "条件名称");
				requireString(fields, missingFields, "storageType", "储存类型");
				requireString(fields, missingFields, "lightRequirement", "避光要求");
				requireString(fields, missingFields, "ventilationRequirement", "通风要求");
				requireInteger(fields, missingFields, "status", "状态");
				requireInteger(fields, missingFields, "sortOrder", "排序值");
			}
			case "quality_grade" -> {
				requireString(fields, missingFields, "gradeName", "等级名称");
				requireInteger(fields, missingFields, "status", "状态");
				requireInteger(fields, missingFields, "sortOrder", "排序值");
			}
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "资源类型不正确");
		}

		return missingFields;
	}

	private String precheckDelete(String resourceType, ResolvedTarget resolvedTarget) {
		if (resolvedTarget == null) {
			return "未找到待删除对象，请提供更明确的名称或编号。";
		}

		return switch (resourceType) {
			case "category" -> {
				if (categoryRepository.countChildren(resolvedTarget.id()) > 0) {
					yield CommonErrorCode.CATEGORY_HAS_CHILDREN.message();
				}
				if (categoryRepository.countProductArchiveReferences(resolvedTarget.id()) > 0) {
					yield CommonErrorCode.CATEGORY_IN_USE.message();
				}
				yield null;
			}
			case "product_archive" -> null;
			case "product_unit" -> productUnitRepository.countProductArchiveReferences(resolvedTarget.id()) > 0
					? CommonErrorCode.PRODUCT_UNIT_IN_USE.message()
					: null;
			case "product_origin" -> productOriginRepository.countProductArchiveReferences(resolvedTarget.id()) > 0
					? CommonErrorCode.PRODUCT_ORIGIN_IN_USE.message()
					: null;
			case "storage_condition" -> storageConditionRepository.countCategoryReferences(resolvedTarget.id()) > 0
							|| storageConditionRepository.countProductArchiveReferences(resolvedTarget.id()) > 0
					? CommonErrorCode.STORAGE_CONDITION_IN_USE.message()
					: null;
			case "quality_grade" -> qualityGradeRepository.countProductArchiveReferences(resolvedTarget.id()) > 0
					? CommonErrorCode.QUALITY_GRADE_IN_USE.message()
					: null;
			default -> null;
		};
	}

	private AssistantActionCard buildActionCard(
			String resourceType,
			String actionType,
			String actionCode,
			String targetLabel,
			String summary,
			String riskLevel,
			String confirmationMode,
			String status,
			List<AssistantActionFieldPrompt> missingFields,
			List<AssistantActionFieldValue> previewFields) {
		return new AssistantActionCard(
				actionCode,
				status,
				resourceType,
				resourceLabel(resourceType),
				actionType,
				actionLabel(actionType),
				targetLabel,
				summary,
				riskLevel,
				confirmationMode,
				"CONFIRM_DELETE_TEXT".equals(confirmationMode) ? "请输入“确认删除”" : null,
				missingFields,
				previewFields);
	}

	private List<AssistantActionFieldValue> buildPreviewFields(
			String resourceType,
			String actionType,
			Map<String, Object> fields,
			ResolvedTarget resolvedTarget) {
		List<AssistantActionFieldValue> previewFields = new ArrayList<>();
		if (resolvedTarget != null && ("UPDATE".equals(actionType) || "DELETE".equals(actionType))) {
			previewFields.add(new AssistantActionFieldValue("target", "目标对象", resolvedTarget.label()));
		}

		fields.forEach((key, value) -> {
			if (value == null || !StringUtils.hasText(String.valueOf(value))) {
				return;
			}
			previewFields.add(new AssistantActionFieldValue(key, fieldLabel(resourceType, key), String.valueOf(value)));
		});
		return previewFields;
	}

	private String executeCategory(AssistantActionPlanEntity plan, Map<String, Object> fields) {
		return switch (plan.actionType()) {
			case "CREATE" -> {
				categoryService.createCategory(new CategoryCreateRequest(
						stringValue(fields, "categoryName"),
						longValue(fields, "parentId"),
						intValue(fields, "sortOrder"),
						intValue(fields, "status"),
						longValue(fields, "defaultStorageConditionId"),
						nullableInteger(fields, "shelfLifeDays"),
						nullableInteger(fields, "warningDays"),
						nullableString(fields, "remarks")));
				yield "已新增产品分类：" + stringValue(fields, "categoryName");
			}
			case "UPDATE" -> {
				categoryService.updateCategory(plan.targetId(), new CategoryUpdateRequest(
						stringValue(fields, "categoryName"),
						longValue(fields, "parentId"),
						intValue(fields, "sortOrder"),
						intValue(fields, "status"),
						longValue(fields, "defaultStorageConditionId"),
						nullableInteger(fields, "shelfLifeDays"),
						nullableInteger(fields, "warningDays"),
						nullableString(fields, "remarks")));
				yield "已更新产品分类：" + plan.targetLabel();
			}
			case "DELETE" -> {
				categoryService.deleteCategory(plan.targetId());
				yield "已删除产品分类：" + plan.targetLabel();
			}
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "动作类型不正确");
		};
	}

	private String executeProductArchive(AssistantActionPlanEntity plan, Map<String, Object> fields) {
		return switch (plan.actionType()) {
			case "CREATE" -> {
				productArchiveService.createProductArchive(new ProductArchiveCreateRequest(
						stringValue(fields, "productName"),
						nullableString(fields, "productSpecification"),
						longValue(fields, "categoryId"),
						longValue(fields, "unitId"),
						longValue(fields, "originId"),
						longValue(fields, "storageConditionId"),
						intValue(fields, "shelfLifeDays"),
						intValue(fields, "warningDays"),
						longValue(fields, "qualityGradeId"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已新增产品档案：" + stringValue(fields, "productName");
			}
			case "UPDATE" -> {
				productArchiveService.updateProductArchive(plan.targetId(), new ProductArchiveUpdateRequest(
						stringValue(fields, "productName"),
						nullableString(fields, "productSpecification"),
						longValue(fields, "categoryId"),
						longValue(fields, "unitId"),
						longValue(fields, "originId"),
						longValue(fields, "storageConditionId"),
						intValue(fields, "shelfLifeDays"),
						intValue(fields, "warningDays"),
						longValue(fields, "qualityGradeId"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已更新产品档案：" + plan.targetLabel();
			}
			case "DELETE" -> {
				productArchiveService.deleteProductArchive(plan.targetId());
				yield "已删除产品档案：" + plan.targetLabel();
			}
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "动作类型不正确");
		};
	}

	private String executeProductUnit(AssistantActionPlanEntity plan, Map<String, Object> fields) {
		return switch (plan.actionType()) {
			case "CREATE" -> {
				productUnitService.createProductUnit(new ProductUnitCreateRequest(
						stringValue(fields, "unitName"),
						stringValue(fields, "unitSymbol"),
						stringValue(fields, "unitType"),
						intValue(fields, "precisionDigits"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已新增产品单位：" + stringValue(fields, "unitName");
			}
			case "UPDATE" -> {
				productUnitService.updateProductUnit(plan.targetId(), new ProductUnitUpdateRequest(
						stringValue(fields, "unitName"),
						stringValue(fields, "unitSymbol"),
						stringValue(fields, "unitType"),
						intValue(fields, "precisionDigits"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已更新产品单位：" + plan.targetLabel();
			}
			case "DELETE" -> {
				productUnitService.deleteProductUnit(plan.targetId());
				yield "已删除产品单位：" + plan.targetLabel();
			}
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "动作类型不正确");
		};
	}

	private String executeProductOrigin(AssistantActionPlanEntity plan, Map<String, Object> fields) {
		return switch (plan.actionType()) {
			case "CREATE" -> {
				productOriginService.createProductOrigin(new ProductOriginCreateRequest(
						stringValue(fields, "originName"),
						stringValue(fields, "countryName"),
						stringValue(fields, "provinceName"),
						nullableString(fields, "cityName"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已新增产地信息：" + stringValue(fields, "originName");
			}
			case "UPDATE" -> {
				productOriginService.updateProductOrigin(plan.targetId(), new ProductOriginUpdateRequest(
						stringValue(fields, "originName"),
						stringValue(fields, "countryName"),
						stringValue(fields, "provinceName"),
						nullableString(fields, "cityName"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已更新产地信息：" + plan.targetLabel();
			}
			case "DELETE" -> {
				productOriginService.deleteProductOrigin(plan.targetId());
				yield "已删除产地信息：" + plan.targetLabel();
			}
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "动作类型不正确");
		};
	}

	private String executeStorageCondition(AssistantActionPlanEntity plan, Map<String, Object> fields) {
		return switch (plan.actionType()) {
			case "CREATE" -> {
				storageConditionService.createStorageCondition(new StorageConditionCreateRequest(
						stringValue(fields, "conditionName"),
						stringValue(fields, "storageType"),
						nullableBigDecimal(fields, "temperatureMin"),
						nullableBigDecimal(fields, "temperatureMax"),
						nullableBigDecimal(fields, "humidityMin"),
						nullableBigDecimal(fields, "humidityMax"),
						stringValue(fields, "lightRequirement"),
						stringValue(fields, "ventilationRequirement"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已新增储存条件：" + stringValue(fields, "conditionName");
			}
			case "UPDATE" -> {
				storageConditionService.updateStorageCondition(plan.targetId(), new StorageConditionUpdateRequest(
						stringValue(fields, "conditionName"),
						stringValue(fields, "storageType"),
						nullableBigDecimal(fields, "temperatureMin"),
						nullableBigDecimal(fields, "temperatureMax"),
						nullableBigDecimal(fields, "humidityMin"),
						nullableBigDecimal(fields, "humidityMax"),
						stringValue(fields, "lightRequirement"),
						stringValue(fields, "ventilationRequirement"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已更新储存条件：" + plan.targetLabel();
			}
			case "DELETE" -> {
				storageConditionService.deleteStorageCondition(plan.targetId());
				yield "已删除储存条件：" + plan.targetLabel();
			}
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "动作类型不正确");
		};
	}

	private String executeQualityGrade(AssistantActionPlanEntity plan, Map<String, Object> fields) {
		return switch (plan.actionType()) {
			case "CREATE" -> {
				qualityGradeService.createQualityGrade(new QualityGradeCreateRequest(
						stringValue(fields, "gradeName"),
						nullableBigDecimal(fields, "scoreMin"),
						nullableBigDecimal(fields, "scoreMax"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已新增品质等级：" + stringValue(fields, "gradeName");
			}
			case "UPDATE" -> {
				qualityGradeService.updateQualityGrade(plan.targetId(), new QualityGradeUpdateRequest(
						stringValue(fields, "gradeName"),
						nullableBigDecimal(fields, "scoreMin"),
						nullableBigDecimal(fields, "scoreMax"),
						intValue(fields, "status"),
						intValue(fields, "sortOrder"),
						nullableString(fields, "remarks")));
				yield "已更新品质等级：" + plan.targetLabel();
			}
			case "DELETE" -> {
				qualityGradeService.deleteQualityGrade(plan.targetId());
				yield "已删除品质等级：" + plan.targetLabel();
			}
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "动作类型不正确");
		};
	}

	private ResolvedTarget resolveCategoryTarget(String target) {
		return resolveSingleTarget(
				categoryRepository.findAll().stream()
						.filter(entity -> matchesText(entity.categoryCode(), target) || matchesText(entity.categoryName(), target))
						.map(entity -> new ResolvedTarget(entity.id(), entity.categoryName()))
						.toList());
	}

	private ResolvedTarget resolveProductArchiveTarget(String target) {
		return resolveSingleTarget(
				productArchiveRepository.findAll().stream()
						.filter(entity -> matchesText(entity.productCode(), target) || matchesText(entity.productName(), target))
						.map(entity -> new ResolvedTarget(entity.id(), entity.productName()))
						.toList());
	}

	private ResolvedTarget resolveProductUnitTarget(String target) {
		return resolveSingleTarget(
				productUnitRepository.findAll().stream()
						.filter(entity -> matchesText(entity.unitCode(), target) || matchesText(entity.unitName(), target)
								|| matchesText(entity.unitSymbol(), target))
						.map(entity -> new ResolvedTarget(entity.id(), entity.unitName()))
						.toList());
	}

	private ResolvedTarget resolveProductOriginTarget(String target) {
		return resolveSingleTarget(
				productOriginRepository.findAll().stream()
						.filter(entity -> matchesText(entity.originCode(), target) || matchesText(entity.originName(), target))
						.map(entity -> new ResolvedTarget(entity.id(), entity.originName()))
						.toList());
	}

	private ResolvedTarget resolveStorageConditionTarget(String target) {
		return resolveSingleTarget(
				storageConditionRepository.findAll().stream()
						.filter(entity -> matchesText(entity.conditionCode(), target) || matchesText(entity.conditionName(), target))
						.map(entity -> new ResolvedTarget(entity.id(), entity.conditionName()))
						.toList());
	}

	private ResolvedTarget resolveQualityGradeTarget(String target) {
		return resolveSingleTarget(
				qualityGradeRepository.findAll().stream()
						.filter(entity -> matchesText(entity.gradeCode(), target) || matchesText(entity.gradeName(), target))
						.map(entity -> new ResolvedTarget(entity.id(), entity.gradeName()))
						.toList());
	}

	private ResolvedTarget resolveSingleTarget(List<ResolvedTarget> matches) {
		if (matches.isEmpty()) {
			return null;
		}
		if (matches.size() > 1) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_ACTION_TARGET_AMBIGUOUS);
		}
		return matches.get(0);
	}

	private String resolveTargetLabel(
			String resourceType,
			String actionType,
			Map<String, Object> fields,
			ResolvedTarget resolvedTarget) {
		if (resolvedTarget != null) {
			return resolvedTarget.label();
		}
		return switch (resourceType) {
			case "category" -> nullableString(fields, "categoryName");
			case "product_archive" -> nullableString(fields, "productName");
			case "product_unit" -> nullableString(fields, "unitName");
			case "product_origin" -> nullableString(fields, "originName");
			case "storage_condition" -> nullableString(fields, "conditionName");
			case "quality_grade" -> nullableString(fields, "gradeName");
			default -> null;
		};
	}

	private String resourceLabel(String resourceType) {
		return switch (resourceType) {
			case "category" -> "产品分类";
			case "product_archive" -> "产品档案";
			case "product_unit" -> "产品单位";
			case "product_origin" -> "产地信息";
			case "storage_condition" -> "储存条件";
			case "quality_grade" -> "品质等级";
			default -> resourceType;
		};
	}

	private String actionLabel(String actionType) {
		return switch (actionType) {
			case "CREATE" -> "新增";
			case "UPDATE" -> "修改";
			case "DELETE" -> "删除";
			default -> actionType;
		};
	}

	private String fieldLabel(String resourceType, String field) {
		return switch (field) {
			case "categoryName" -> "分类名称";
			case "parentId" -> "上级分类";
			case "defaultStorageConditionId" -> "默认储存条件";
			case "shelfLifeDays" -> "保质期天数";
			case "warningDays" -> "预警提前天数";
			case "sortOrder" -> "排序值";
			case "status" -> "状态";
			case "productName" -> "产品名称";
			case "productSpecification" -> "产品规格";
			case "categoryId" -> "产品分类";
			case "unitId" -> "产品单位";
			case "originId" -> "产地信息";
			case "storageConditionId" -> "储存条件";
			case "qualityGradeId" -> "品质等级";
			case "unitName" -> "单位名称";
			case "unitSymbol" -> "单位符号";
			case "unitType" -> "单位类型";
			case "precisionDigits" -> "精度位数";
			case "originName" -> "产地名称";
			case "countryName" -> "国家名称";
			case "provinceName" -> "省份名称";
			case "cityName" -> "城市名称";
			case "conditionName" -> "条件名称";
			case "storageType" -> "储存类型";
			case "temperatureMin" -> "最低温度";
			case "temperatureMax" -> "最高温度";
			case "humidityMin" -> "最低湿度";
			case "humidityMax" -> "最高湿度";
			case "lightRequirement" -> "避光要求";
			case "ventilationRequirement" -> "通风要求";
			case "gradeName" -> "等级名称";
			case "scoreMin" -> "最低分";
			case "scoreMax" -> "最高分";
			case "remarks" -> "备注";
			default -> field;
		};
	}

	private void requireString(
			Map<String, Object> fields,
			List<AssistantActionFieldPrompt> missingFields,
			String field,
			String label) {
		if (!StringUtils.hasText(nullableString(fields, field))) {
			missingFields.add(new AssistantActionFieldPrompt(field, label, null));
		}
	}

	private void requireInteger(
			Map<String, Object> fields,
			List<AssistantActionFieldPrompt> missingFields,
			String field,
			String label) {
		if (nullableInteger(fields, field) == null) {
			missingFields.add(new AssistantActionFieldPrompt(field, label, null));
		}
	}

	private void requireReference(
			Map<String, Object> fields,
			List<AssistantActionFieldPrompt> missingFields,
			String field,
			String label) {
		if (longValue(fields, field) == null) {
			missingFields.add(new AssistantActionFieldPrompt(field, label, "请提供名称或编号"));
		}
	}

	private String buildActionCode() {
		return "AWA-" + LocalDateTime.now().format(ACTION_CODE_FORMATTER);
	}

	private String serialize(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception exception) {
			return "{}";
		}
	}

	private Map<String, Object> deserializeMap(String rawJson) {
		if (!StringUtils.hasText(rawJson)) {
			return new LinkedHashMap<>();
		}
		try {
			return objectMapper.readValue(rawJson, new TypeReference<>() {
			});
		} catch (Exception exception) {
			return new LinkedHashMap<>();
		}
	}

	private boolean matchesText(String source, String keyword) {
		if (!StringUtils.hasText(source) || !StringUtils.hasText(keyword)) {
			return false;
		}
		String normalizedSource = source.trim().toLowerCase(Locale.ROOT);
		String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
		return normalizedSource.equals(normalizedKeyword) || normalizedSource.contains(normalizedKeyword);
	}

	private String stringValue(Map<String, Object> fields, String field) {
		String value = nullableString(fields, field);
		if (!StringUtils.hasText(value)) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "缺少字段：" + field);
		}
		return value;
	}

	private String nullableString(Map<String, Object> fields, String field) {
		Object rawValue = fields.get(field);
		if (rawValue == null) {
			return null;
		}
		String text = String.valueOf(rawValue).trim();
		return text.isEmpty() ? null : text;
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private Integer intValue(Map<String, Object> fields, String field) {
		Integer value = nullableInteger(fields, field);
		if (value == null) {
			throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "缺少字段：" + field);
		}
		return value;
	}

	private Integer nullableInteger(Map<String, Object> fields, String field) {
		Object rawValue = fields.get(field);
		if (rawValue == null || !StringUtils.hasText(String.valueOf(rawValue))) {
			return null;
		}
		if (rawValue instanceof Number number) {
			return number.intValue();
		}
		return Integer.parseInt(String.valueOf(rawValue).trim());
	}

	private Long longValue(Map<String, Object> fields, String field) {
		Object rawValue = fields.get(field);
		if (rawValue == null || !StringUtils.hasText(String.valueOf(rawValue))) {
			return null;
		}
		if (rawValue instanceof Number number) {
			return number.longValue();
		}
		return Long.parseLong(String.valueOf(rawValue).trim());
	}

	private BigDecimal nullableBigDecimal(Map<String, Object> fields, String field) {
		Object rawValue = fields.get(field);
		if (rawValue == null || !StringUtils.hasText(String.valueOf(rawValue))) {
			return null;
		}
		if (rawValue instanceof BigDecimal bigDecimal) {
			return bigDecimal;
		}
		if (rawValue instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		return new BigDecimal(String.valueOf(rawValue).trim());
	}

	private record ResolvedTarget(Long id, String label) {
	}
}
