package com.nongcang.server.modules.productorigin.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginCreateRequest;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginListQueryRequest;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginStatusUpdateRequest;
import com.nongcang.server.modules.productorigin.domain.dto.ProductOriginUpdateRequest;
import com.nongcang.server.modules.productorigin.domain.vo.ProductOriginDetailResponse;
import com.nongcang.server.modules.productorigin.domain.vo.ProductOriginListItemResponse;
import com.nongcang.server.modules.productorigin.domain.vo.ProductOriginOptionResponse;
import com.nongcang.server.modules.productorigin.service.ProductOriginService;
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
@RequestMapping("/api/product-origin")
public class ProductOriginController {

	private final ProductOriginService productOriginService;

	public ProductOriginController(ProductOriginService productOriginService) {
		this.productOriginService = productOriginService;
	}

	@GetMapping("/list")
	public ApiResponse<List<ProductOriginListItemResponse>> getProductOriginList(
			@Valid @ModelAttribute ProductOriginListQueryRequest queryRequest) {
		return ApiResponse.success(productOriginService.getProductOriginList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<ProductOriginOptionResponse>> getProductOriginOptions() {
		return ApiResponse.success(productOriginService.getProductOriginOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<ProductOriginDetailResponse> getProductOriginDetail(@PathVariable Long id) {
		return ApiResponse.success(productOriginService.getProductOriginDetail(id));
	}

	@PostMapping
	public ApiResponse<ProductOriginDetailResponse> createProductOrigin(
			@Valid @RequestBody ProductOriginCreateRequest request) {
		return ApiResponse.success("新增成功", productOriginService.createProductOrigin(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<ProductOriginDetailResponse> updateProductOrigin(
			@PathVariable Long id,
			@Valid @RequestBody ProductOriginUpdateRequest request) {
		return ApiResponse.success("更新成功", productOriginService.updateProductOrigin(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateProductOriginStatus(
			@PathVariable Long id,
			@Valid @RequestBody ProductOriginStatusUpdateRequest request) {
		productOriginService.updateProductOriginStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteProductOrigin(@PathVariable Long id) {
		productOriginService.deleteProductOrigin(id);
		return ApiResponse.success("删除成功", null);
	}
}
