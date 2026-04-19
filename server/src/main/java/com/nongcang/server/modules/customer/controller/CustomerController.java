package com.nongcang.server.modules.customer.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.customer.domain.dto.CustomerCreateRequest;
import com.nongcang.server.modules.customer.domain.dto.CustomerListQueryRequest;
import com.nongcang.server.modules.customer.domain.dto.CustomerStatusUpdateRequest;
import com.nongcang.server.modules.customer.domain.dto.CustomerUpdateRequest;
import com.nongcang.server.modules.customer.domain.vo.CustomerDetailResponse;
import com.nongcang.server.modules.customer.domain.vo.CustomerListItemResponse;
import com.nongcang.server.modules.customer.domain.vo.CustomerOptionResponse;
import com.nongcang.server.modules.customer.service.CustomerService;
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
@RequestMapping("/api/customer")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@GetMapping("/list")
	public ApiResponse<List<CustomerListItemResponse>> getCustomerList(
			@Valid @ModelAttribute CustomerListQueryRequest queryRequest) {
		return ApiResponse.success(customerService.getCustomerList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<CustomerOptionResponse>> getCustomerOptions() {
		return ApiResponse.success(customerService.getCustomerOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<CustomerDetailResponse> getCustomerDetail(@PathVariable Long id) {
		return ApiResponse.success(customerService.getCustomerDetail(id));
	}

	@PostMapping
	public ApiResponse<CustomerDetailResponse> createCustomer(
			@Valid @RequestBody CustomerCreateRequest request) {
		return ApiResponse.success("新增成功", customerService.createCustomer(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<CustomerDetailResponse> updateCustomer(
			@PathVariable Long id,
			@Valid @RequestBody CustomerUpdateRequest request) {
		return ApiResponse.success("更新成功", customerService.updateCustomer(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateCustomerStatus(
			@PathVariable Long id,
			@Valid @RequestBody CustomerStatusUpdateRequest request) {
		customerService.updateCustomerStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteCustomer(@PathVariable Long id) {
		customerService.deleteCustomer(id);
		return ApiResponse.success("删除成功", null);
	}
}
