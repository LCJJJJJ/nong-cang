package com.nongcang.server.modules.supplier.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.supplier.domain.dto.SupplierCreateRequest;
import com.nongcang.server.modules.supplier.domain.dto.SupplierListQueryRequest;
import com.nongcang.server.modules.supplier.domain.dto.SupplierStatusUpdateRequest;
import com.nongcang.server.modules.supplier.domain.dto.SupplierUpdateRequest;
import com.nongcang.server.modules.supplier.domain.vo.SupplierDetailResponse;
import com.nongcang.server.modules.supplier.domain.vo.SupplierListItemResponse;
import com.nongcang.server.modules.supplier.domain.vo.SupplierOptionResponse;
import com.nongcang.server.modules.supplier.service.SupplierService;
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
@RequestMapping("/api/supplier")
public class SupplierController {

	private final SupplierService supplierService;

	public SupplierController(SupplierService supplierService) {
		this.supplierService = supplierService;
	}

	@GetMapping("/list")
	public ApiResponse<List<SupplierListItemResponse>> getSupplierList(
			@Valid @ModelAttribute SupplierListQueryRequest queryRequest) {
		return ApiResponse.success(supplierService.getSupplierList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<SupplierOptionResponse>> getSupplierOptions() {
		return ApiResponse.success(supplierService.getSupplierOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<SupplierDetailResponse> getSupplierDetail(@PathVariable Long id) {
		return ApiResponse.success(supplierService.getSupplierDetail(id));
	}

	@PostMapping
	public ApiResponse<SupplierDetailResponse> createSupplier(
			@Valid @RequestBody SupplierCreateRequest request) {
		return ApiResponse.success("新增成功", supplierService.createSupplier(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<SupplierDetailResponse> updateSupplier(
			@PathVariable Long id,
			@Valid @RequestBody SupplierUpdateRequest request) {
		return ApiResponse.success("更新成功", supplierService.updateSupplier(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateSupplierStatus(
			@PathVariable Long id,
			@Valid @RequestBody SupplierStatusUpdateRequest request) {
		supplierService.updateSupplierStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteSupplier(@PathVariable Long id) {
		supplierService.deleteSupplier(id);
		return ApiResponse.success("删除成功", null);
	}
}
