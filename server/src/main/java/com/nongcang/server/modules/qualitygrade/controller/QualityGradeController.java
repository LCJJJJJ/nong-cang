package com.nongcang.server.modules.qualitygrade.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeCreateRequest;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeListQueryRequest;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeStatusUpdateRequest;
import com.nongcang.server.modules.qualitygrade.domain.dto.QualityGradeUpdateRequest;
import com.nongcang.server.modules.qualitygrade.domain.vo.QualityGradeDetailResponse;
import com.nongcang.server.modules.qualitygrade.domain.vo.QualityGradeListItemResponse;
import com.nongcang.server.modules.qualitygrade.domain.vo.QualityGradeOptionResponse;
import com.nongcang.server.modules.qualitygrade.service.QualityGradeService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/quality-grade")
public class QualityGradeController {

	private final QualityGradeService qualityGradeService;

	public QualityGradeController(QualityGradeService qualityGradeService) {
		this.qualityGradeService = qualityGradeService;
	}

	@GetMapping("/list")
	public ApiResponse<List<QualityGradeListItemResponse>> getQualityGradeList(
			@Valid @ModelAttribute QualityGradeListQueryRequest queryRequest) {
		return ApiResponse.success(qualityGradeService.getQualityGradeList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<QualityGradeOptionResponse>> getQualityGradeOptions() {
		return ApiResponse.success(qualityGradeService.getQualityGradeOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<QualityGradeDetailResponse> getQualityGradeDetail(@PathVariable Long id) {
		return ApiResponse.success(qualityGradeService.getQualityGradeDetail(id));
	}

	@PostMapping
	public ApiResponse<QualityGradeDetailResponse> createQualityGrade(
			@Valid @RequestBody QualityGradeCreateRequest request) {
		return ApiResponse.success("新增成功", qualityGradeService.createQualityGrade(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<QualityGradeDetailResponse> updateQualityGrade(
			@PathVariable Long id,
			@Valid @RequestBody QualityGradeUpdateRequest request) {
		return ApiResponse.success("更新成功", qualityGradeService.updateQualityGrade(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateQualityGradeStatus(
			@PathVariable Long id,
			@Valid @RequestBody QualityGradeStatusUpdateRequest request) {
		qualityGradeService.updateQualityGradeStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteQualityGrade(@PathVariable Long id) {
		qualityGradeService.deleteQualityGrade(id);
		return ApiResponse.success("删除成功", null);
	}
}
