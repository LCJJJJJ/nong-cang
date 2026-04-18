package com.nongcang.server.modules.category.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.category.domain.dto.CategoryCreateRequest;
import com.nongcang.server.modules.category.domain.dto.CategoryStatusUpdateRequest;
import com.nongcang.server.modules.category.domain.dto.CategoryTreeQueryRequest;
import com.nongcang.server.modules.category.domain.dto.CategoryUpdateRequest;
import com.nongcang.server.modules.category.domain.vo.CategoryDetailResponse;
import com.nongcang.server.modules.category.domain.vo.CategoryOptionResponse;
import com.nongcang.server.modules.category.domain.vo.CategoryTreeItemResponse;
import com.nongcang.server.modules.category.service.CategoryService;
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
@RequestMapping("/api/category")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping("/tree")
	public ApiResponse<List<CategoryTreeItemResponse>> getCategoryTree(
			@Valid @ModelAttribute CategoryTreeQueryRequest queryRequest) {
		return ApiResponse.success(categoryService.getCategoryTree(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<CategoryOptionResponse>> getCategoryOptions() {
		return ApiResponse.success(categoryService.getCategoryOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<CategoryDetailResponse> getCategoryDetail(@PathVariable Long id) {
		return ApiResponse.success(categoryService.getCategoryDetail(id));
	}

	@PostMapping
	public ApiResponse<CategoryDetailResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
		return ApiResponse.success("新增成功", categoryService.createCategory(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<CategoryDetailResponse> updateCategory(
			@PathVariable Long id,
			@Valid @RequestBody CategoryUpdateRequest request) {
		return ApiResponse.success("更新成功", categoryService.updateCategory(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateCategoryStatus(
			@PathVariable Long id,
			@Valid @RequestBody CategoryStatusUpdateRequest request) {
		categoryService.updateCategoryStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
		categoryService.deleteCategory(id);
		return ApiResponse.success("删除成功", null);
	}
}
