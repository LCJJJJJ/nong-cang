package com.nongcang.server.modules.alertrule.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.alertrule.domain.dto.AlertRuleListQueryRequest;
import com.nongcang.server.modules.alertrule.domain.dto.AlertRuleStatusUpdateRequest;
import com.nongcang.server.modules.alertrule.domain.dto.AlertRuleUpdateRequest;
import com.nongcang.server.modules.alertrule.domain.entity.AlertRuleEntity;
import com.nongcang.server.modules.alertrule.domain.vo.AlertRuleDetailResponse;
import com.nongcang.server.modules.alertrule.domain.vo.AlertRuleListItemResponse;
import com.nongcang.server.modules.alertrule.repository.AlertRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AlertRuleService {

	private static final Set<String> TIMEOUT_ALERT_TYPES = Set.of(
			"PUTAWAY_TIMEOUT",
			"OUTBOUND_PICK_TIMEOUT",
			"OUTBOUND_SHIP_TIMEOUT",
			"ABNORMAL_STOCK_STAGNANT",
			"STOCKTAKING_CONFIRM_TIMEOUT",
			"INBOUND_PENDING_INSPECTION");

	private final AlertRuleRepository alertRuleRepository;

	public AlertRuleService(AlertRuleRepository alertRuleRepository) {
		this.alertRuleRepository = alertRuleRepository;
	}

	public List<AlertRuleListItemResponse> getAlertRuleList(AlertRuleListQueryRequest queryRequest) {
		return alertRuleRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public AlertRuleDetailResponse getAlertRuleDetail(Long id) {
		return toDetailResponse(getExistingRule(id));
	}

	@Transactional
	public AlertRuleDetailResponse updateAlertRule(Long id, AlertRuleUpdateRequest request) {
		AlertRuleEntity currentRule = getExistingRule(id);
		validateSeverity(request.severity());
		validateThreshold(request.thresholdValue());
		String thresholdUnit = normalizeThresholdUnit(request.thresholdUnit(), currentRule.alertType());

		AlertRuleEntity updated = new AlertRuleEntity(
				currentRule.id(),
				currentRule.ruleCode(),
				currentRule.ruleName(),
				currentRule.alertType(),
				request.severity().trim().toUpperCase(),
				request.thresholdValue(),
				thresholdUnit,
				currentRule.enabled(),
				trimToNull(request.description()),
				request.sortOrder() == null ? currentRule.sortOrder() : request.sortOrder(),
				currentRule.createdAt(),
				currentRule.updatedAt());

		alertRuleRepository.update(updated);
		return getAlertRuleDetail(id);
	}

	@Transactional
	public void updateAlertRuleStatus(Long id, AlertRuleStatusUpdateRequest request) {
		getExistingRule(id);
		alertRuleRepository.updateEnabled(id, request.enabled());
	}

	private AlertRuleEntity getExistingRule(Long id) {
		return alertRuleRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.ALERT_RULE_NOT_FOUND));
	}

	private boolean matchesQuery(AlertRuleEntity entity, AlertRuleListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.ruleCode())
				&& !entity.ruleCode().contains(queryRequest.ruleCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.ruleName())
				&& !entity.ruleName().contains(queryRequest.ruleName().trim())) {
			return false;
		}

		if (queryRequest.enabled() != null && !entity.enabled().equals(queryRequest.enabled())) {
			return false;
		}

		return true;
	}

	private void validateSeverity(String severity) {
		String value = severity == null ? "" : severity.trim().toUpperCase();
		if (!List.of("LOW", "MEDIUM", "HIGH").contains(value)) {
			throw new BusinessException(CommonErrorCode.ALERT_RULE_SEVERITY_INVALID);
		}
	}

	private void validateThreshold(BigDecimal thresholdValue) {
		if (thresholdValue == null || thresholdValue.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException(CommonErrorCode.ALERT_RULE_THRESHOLD_INVALID);
		}
	}

	private String normalizeThresholdUnit(String thresholdUnit, String alertType) {
		String normalized = thresholdUnit == null ? "" : thresholdUnit.trim().toUpperCase();

		if (TIMEOUT_ALERT_TYPES.contains(alertType)) {
			if (!Set.of("MINUTE", "HOUR").contains(normalized)) {
				throw new BusinessException(CommonErrorCode.ALERT_RULE_THRESHOLD_UNIT_INVALID);
			}
			return normalized;
		}

		if ("LOW_STOCK".equals(alertType)) {
			if (!"QUANTITY".equals(normalized)) {
				throw new BusinessException(CommonErrorCode.ALERT_RULE_THRESHOLD_UNIT_INVALID);
			}
			return normalized;
		}

		if (Set.of("NEAR_EXPIRY", "EXPIRED").contains(alertType)) {
			if (!"DAY".equals(normalized)) {
				throw new BusinessException(CommonErrorCode.ALERT_RULE_THRESHOLD_UNIT_INVALID);
			}
			return normalized;
		}

		throw new BusinessException(CommonErrorCode.ALERT_RULE_THRESHOLD_UNIT_INVALID);
	}

	private AlertRuleListItemResponse toListItemResponse(AlertRuleEntity entity) {
		return new AlertRuleListItemResponse(
				entity.id(),
				entity.ruleCode(),
				entity.ruleName(),
				entity.alertType(),
				entity.severity(),
				AlertRuleRepository.toDouble(entity.thresholdValue()),
				entity.thresholdUnit(),
				entity.enabled(),
				entity.enabled() == 1 ? "启用" : "停用",
				entity.description(),
				entity.sortOrder(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private AlertRuleDetailResponse toDetailResponse(AlertRuleEntity entity) {
		return new AlertRuleDetailResponse(
				entity.id(),
				entity.ruleCode(),
				entity.ruleName(),
				entity.alertType(),
				entity.severity(),
				AlertRuleRepository.toDouble(entity.thresholdValue()),
				entity.thresholdUnit(),
				entity.enabled(),
				entity.enabled() == 1 ? "启用" : "停用",
				entity.description(),
				entity.sortOrder(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
