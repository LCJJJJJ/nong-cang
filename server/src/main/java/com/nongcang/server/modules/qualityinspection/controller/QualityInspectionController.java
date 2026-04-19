package com.nongcang.server.modules.qualityinspection.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.qualityinspection.domain.dto.QualityInspectionCreateRequest;
import com.nongcang.server.modules.qualityinspection.domain.dto.QualityInspectionListQueryRequest;
import com.nongcang.server.modules.qualityinspection.domain.vo.QualityInspectionDetailResponse;
import com.nongcang.server.modules.qualityinspection.domain.vo.QualityInspectionListItemResponse;
import com.nongcang.server.modules.qualityinspection.service.QualityInspectionService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/quality-inspection")
public class QualityInspectionController {

	private final QualityInspectionService qualityInspectionService;

	public QualityInspectionController(QualityInspectionService qualityInspectionService) {
		this.qualityInspectionService = qualityInspectionService;
	}

	@GetMapping("/list")
	public ApiResponse<List<QualityInspectionListItemResponse>> getQualityInspectionList(
			@Valid @ModelAttribute QualityInspectionListQueryRequest queryRequest) {
		return ApiResponse.success(qualityInspectionService.getQualityInspectionList(queryRequest));
	}

	@GetMapping("/{id}")
	public ApiResponse<QualityInspectionDetailResponse> getQualityInspectionDetail(@PathVariable Long id) {
		return ApiResponse.success(qualityInspectionService.getQualityInspectionDetail(id));
	}

	@PostMapping
	public ApiResponse<QualityInspectionDetailResponse> createQualityInspection(
			@Valid @RequestBody QualityInspectionCreateRequest request) {
		return ApiResponse.success("新增成功", qualityInspectionService.createQualityInspection(request));
	}
}
