package com.nongcang.server.modules.qualitygrade.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeCreateRequest;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeListQueryRequest;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeStatusUpdateRequest;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeUpdateRequest;
import com.nongcang.server.modules.qualitygrade.domain.entity.QualityGradeEntity;
import com.nongcang.server.modules.qualitygrade.domain.vo.QualityGradeDetailResponse;
import com.nongcang.server.modules.qualitygrade.domain.vo.QualityGradeListItemResponse;
import com.nongcang.server.modules.qualitygrade.domain.vo.QualityGradeOptionResponse;
import com.nongcang.server.modules.qualitygrade.repository.QualityGradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class QualityGradeService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter QUALITY_GRADE_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final QualityGradeRepository qualityGradeRepository;

	public QualityGradeService(QualityGradeRepository qualityGradeRepository) {
		this.qualityGradeRepository = qualityGradeRepository;
	}

	public List<QualityGradeListItemResponse> getQualityGradeList(QualityGradeListQueryRequest queryRequest) {
		return qualityGradeRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<QualityGradeOptionResponse> getQualityGradeOptions() {
		return qualityGradeRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new QualityGradeOptionResponse(
						entity.id(),
						entity.gradeName(),
						entity.status()))
				.toList();
	}

	public QualityGradeDetailResponse getQualityGradeDetail(Long id) {
		return toDetailResponse(getExistingQualityGrade(id));
	}

	@Transactional
	public QualityGradeDetailResponse createQualityGrade(QualityGradeCreateRequest request) {
		validateUniqueName(request.gradeName(), null);
		validateScoreRange(request.scoreMin(), request.scoreMax());

		QualityGradeEntity qualityGradeEntity = new QualityGradeEntity(
				null,
				generateGradeCode(),
				request.gradeName().trim(),
				request.scoreMin(),
				request.scoreMax(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = qualityGradeRepository.insert(qualityGradeEntity);
		return getQualityGradeDetail(id);
	}

	@Transactional
	public QualityGradeDetailResponse updateQualityGrade(Long id, QualityGradeUpdateRequest request) {
		QualityGradeEntity currentQualityGrade = getExistingQualityGrade(id);

		validateUniqueName(request.gradeName(), id);
		validateScoreRange(request.scoreMin(), request.scoreMax());

		QualityGradeEntity updatedQualityGrade = new QualityGradeEntity(
				currentQualityGrade.id(),
				currentQualityGrade.gradeCode(),
				request.gradeName().trim(),
				request.scoreMin(),
				request.scoreMax(),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentQualityGrade.createdAt(),
				currentQualityGrade.updatedAt());

		qualityGradeRepository.update(updatedQualityGrade);
		return getQualityGradeDetail(id);
	}

	@Transactional
	public void updateQualityGradeStatus(Long id, QualityGradeStatusUpdateRequest request) {
		getExistingQualityGrade(id);
		qualityGradeRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteQualityGrade(Long id) {
		getExistingQualityGrade(id);

		if (qualityGradeRepository.countProductArchiveReferences(id) > 0) {
			throw new BusinessException(CommonErrorCode.QUALITY_GRADE_IN_USE);
		}

		qualityGradeRepository.deleteById(id);
	}

	private QualityGradeEntity getExistingQualityGrade(Long id) {
		return qualityGradeRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.QUALITY_GRADE_NOT_FOUND));
	}

	private boolean matchesQuery(QualityGradeEntity entity, QualityGradeListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.gradeCode())
				&& !entity.gradeCode().contains(queryRequest.gradeCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.gradeName())
				&& !entity.gradeName().contains(queryRequest.gradeName().trim())) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(String gradeName, Long excludeId) {
		if (qualityGradeRepository.existsByGradeName(gradeName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.QUALITY_GRADE_NAME_DUPLICATED);
		}
	}

	private void validateScoreRange(BigDecimal scoreMin, BigDecimal scoreMax) {
		if (scoreMin != null && scoreMax != null && scoreMin.compareTo(scoreMax) > 0) {
			throw new BusinessException(CommonErrorCode.QUALITY_GRADE_SCORE_RANGE_INVALID);
		}
	}

	private String generateGradeCode() {
		for (int index = 0; index < 20; index += 1) {
			String gradeCode = "GRADE-" + LocalDateTime.now().format(QUALITY_GRADE_CODE_FORMATTER);

			if (index > 0) {
				gradeCode += "-" + index;
			}

			if (!qualityGradeRepository.existsByGradeCode(gradeCode, null)) {
				return gradeCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "品质等级编号生成失败，请稍后重试");
	}

	private QualityGradeListItemResponse toListItemResponse(QualityGradeEntity entity) {
		return new QualityGradeListItemResponse(
				entity.id(),
				entity.gradeCode(),
				entity.gradeName(),
				toDouble(entity.scoreMin()),
				toDouble(entity.scoreMax()),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private QualityGradeDetailResponse toDetailResponse(QualityGradeEntity entity) {
		return new QualityGradeDetailResponse(
				entity.id(),
				entity.gradeCode(),
				entity.gradeName(),
				toDouble(entity.scoreMin()),
				toDouble(entity.scoreMax()),
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
