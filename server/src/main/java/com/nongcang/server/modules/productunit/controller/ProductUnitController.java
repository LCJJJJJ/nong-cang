package com.nongcang.server.modules.productunit.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitCreateRequest;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitListQueryRequest;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitStatusUpdateRequest;
import com.nongcang.server.modules.productunit.domain.dto.ProductUnitUpdateRequest;
import com.nongcang.server.modules.productunit.domain.vo.ProductUnitDetailResponse;
import com.nongcang.server.modules.productunit.domain.vo.ProductUnitListItemResponse;
import com.nongcang.server.modules.productunit.domain.vo.ProductUnitOptionResponse;
import com.nongcang.server.modules.productunit.service.ProductUnitService;
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
@RequestMapping("/api/product-unit")
public class ProductUnitController {

	private final ProductUnitService productUnitService;

	public ProductUnitController(ProductUnitService productUnitService) {
		this.productUnitService = productUnitService;
	}

	@GetMapping("/list")
	public ApiResponse<List<ProductUnitListItemResponse>> getProductUnitList(
			@Valid @ModelAttribute ProductUnitListQueryRequest queryRequest) {
		return ApiResponse.success(productUnitService.getProductUnitList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<ProductUnitOptionResponse>> getProductUnitOptions() {
		return ApiResponse.success(productUnitService.getProductUnitOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<ProductUnitDetailResponse> getProductUnitDetail(@PathVariable Long id) {
		return ApiResponse.success(productUnitService.getProductUnitDetail(id));
	}

	@PostMapping
	public ApiResponse<ProductUnitDetailResponse> createProductUnit(
			@Valid @RequestBody ProductUnitCreateRequest request) {
		return ApiResponse.success("新增成功", productUnitService.createProductUnit(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<ProductUnitDetailResponse> updateProductUnit(
			@PathVariable Long id,
			@Valid @RequestBody ProductUnitUpdateRequest request) {
		return ApiResponse.success("更新成功", productUnitService.updateProductUnit(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateProductUnitStatus(
			@PathVariable Long id,
			@Valid @RequestBody ProductUnitStatusUpdateRequest request) {
		productUnitService.updateProductUnitStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteProductUnit(@PathVariable Long id) {
		productUnitService.deleteProductUnit(id);
		return ApiResponse.success("删除成功", null);
	}
}
