package com.nongcang.server.modules.alertrecord.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.alertrecord.domain.dto.AlertRecordListQueryRequest;
import com.nongcang.server.modules.alertrecord.domain.entity.AlertRecordEntity;
import com.nongcang.server.modules.alertrecord.domain.vo.AlertRecordListItemResponse;
import com.nongcang.server.modules.alertrecord.domain.vo.AlertRefreshResponse;
import com.nongcang.server.modules.alertrecord.repository.AlertRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AlertRecordService {

	private final AlertRecordRepository alertRecordRepository;
	private final AlertRefreshService alertRefreshService;

	public AlertRecordService(
			AlertRecordRepository alertRecordRepository,
			AlertRefreshService alertRefreshService) {
		this.alertRecordRepository = alertRecordRepository;
		this.alertRefreshService = alertRefreshService;
	}

	public List<AlertRecordListItemResponse> getAlertRecordList(AlertRecordListQueryRequest queryRequest) {
		return alertRecordRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	@Transactional
	public AlertRefreshResponse refreshAlertRecords() {
		return alertRefreshService.refreshAllAlerts();
	}

	@Transactional
	public void ignore(Long id) {
		getExistingAlert(id);
		alertRecordRepository.updateStatus(id, 2);
	}

	private AlertRecordEntity getExistingAlert(Long id) {
		return alertRecordRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.ALERT_RECORD_NOT_FOUND));
	}

	private boolean matchesQuery(AlertRecordEntity entity, AlertRecordListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.alertCode())
				&& !entity.alertCode().contains(queryRequest.alertCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.alertType())
				&& !entity.alertType().equals(queryRequest.alertType().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.severity())
				&& !entity.severity().equals(queryRequest.severity().trim().toUpperCase())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private AlertRecordListItemResponse toListItemResponse(AlertRecordEntity entity) {
		return new AlertRecordListItemResponse(
				entity.id(),
				entity.alertCode(),
				entity.ruleId(),
				entity.ruleCode(),
				entity.alertType(),
				entity.severity(),
				entity.sourceType(),
				entity.sourceId(),
				entity.sourceCode(),
				entity.title(),
				entity.content(),
				entity.status(),
				toStatusLabel(entity.status()),
				toIsoDateTime(entity.occurredAt()),
				toIsoDateTime(entity.resolvedAt()),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String toStatusLabel(Integer status) {
		return switch (status) {
			case 1 -> "活跃";
			case 2 -> "已忽略";
			case 3 -> "已恢复";
			default -> "未知状态";
		};
	}

	private String toIsoDateTime(LocalDateTime value) {
		return value == null ? null : value.atOffset(ZoneOffset.ofHours(8)).toString();
	}
}
