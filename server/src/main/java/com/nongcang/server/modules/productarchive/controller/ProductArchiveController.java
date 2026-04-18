package com.nongcang.server.modules.productarchive.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveCreateRequest;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveListQueryRequest;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveStatusUpdateRequest;
import com.nongcang.server.modules.productarchive.domain.dto.ProductArchiveUpdateRequest;
import com.nongcang.server.modules.productarchive.domain.vo.ProductArchiveDetailResponse;
import com.nongcang.server.modules.productarchive.domain.vo.ProductArchiveListItemResponse;
import com.nongcang.server.modules.productarchive.service.ProductArchiveService;
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
@RequestMapping("/api/product-archive")
public class ProductArchiveController {

	private final ProductArchiveService productArchiveService;

	public ProductArchiveController(ProductArchiveService productArchiveService) {
		this.productArchiveService = productArchiveService;
	}

	@GetMapping("/list")
	public ApiResponse<List<ProductArchiveListItemResponse>> getProductArchiveList(
			@Valid @ModelAttribute ProductArchiveListQueryRequest queryRequest) {
		return ApiResponse.success(productArchiveService.getProductArchiveList(queryRequest));
	}

	@GetMapping("/{id}")
	public ApiResponse<ProductArchiveDetailResponse> getProductArchiveDetail(@PathVariable Long id) {
		return ApiResponse.success(productArchiveService.getProductArchiveDetail(id));
	}

	@PostMapping
	public ApiResponse<ProductArchiveDetailResponse> createProductArchive(
			@Valid @RequestBody ProductArchiveCreateRequest request) {
		return ApiResponse.success("新增成功", productArchiveService.createProductArchive(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<ProductArchiveDetailResponse> updateProductArchive(
			@PathVariable Long id,
			@Valid @RequestBody ProductArchiveUpdateRequest request) {
		return ApiResponse.success("更新成功", productArchiveService.updateProductArchive(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateProductArchiveStatus(
			@PathVariable Long id,
			@Valid @RequestBody ProductArchiveStatusUpdateRequest request) {
		productArchiveService.updateProductArchiveStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteProductArchive(@PathVariable Long id) {
		productArchiveService.deleteProductArchive(id);
		return ApiResponse.success("删除成功", null);
	}
}
