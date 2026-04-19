package com.nongcang.server.modules.customer.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.customer.domain.dto.CustomerCreateRequest;
import com.nongcang.server.modules.customer.domain.dto.CustomerListQueryRequest;
import com.nongcang.server.modules.customer.domain.dto.CustomerStatusUpdateRequest;
import com.nongcang.server.modules.customer.domain.dto.CustomerUpdateRequest;
import com.nongcang.server.modules.customer.domain.entity.CustomerEntity;
import com.nongcang.server.modules.customer.domain.vo.CustomerDetailResponse;
import com.nongcang.server.modules.customer.domain.vo.CustomerListItemResponse;
import com.nongcang.server.modules.customer.domain.vo.CustomerOptionResponse;
import com.nongcang.server.modules.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CustomerService {

	private static final int ENABLED = 1;

	private static final DateTimeFormatter CUSTOMER_CODE_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	public List<CustomerListItemResponse> getCustomerList(CustomerListQueryRequest queryRequest) {
		return customerRepository.findAll()
				.stream()
				.filter(entity -> matchesQuery(entity, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<CustomerOptionResponse> getCustomerOptions() {
		return customerRepository.findAll()
				.stream()
				.filter(entity -> ENABLED == entity.status())
				.map(entity -> new CustomerOptionResponse(
						entity.id(),
						entity.customerName(),
						entity.customerType(),
						entity.status()))
				.toList();
	}

	public CustomerDetailResponse getCustomerDetail(Long id) {
		return toDetailResponse(getExistingCustomer(id));
	}

	@Transactional
	public CustomerDetailResponse createCustomer(CustomerCreateRequest request) {
		validateUniqueName(request.customerName(), null);

		CustomerEntity customerEntity = new CustomerEntity(
				null,
				generateCustomerCode(),
				request.customerName().trim(),
				request.customerType().trim(),
				trimToNull(request.contactName()),
				trimToNull(request.contactPhone()),
				trimToNull(request.regionName()),
				trimToNull(request.address()),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				null,
				null);

		long id = customerRepository.insert(customerEntity);
		return getCustomerDetail(id);
	}

	@Transactional
	public CustomerDetailResponse updateCustomer(Long id, CustomerUpdateRequest request) {
		CustomerEntity currentCustomer = getExistingCustomer(id);

		validateUniqueName(request.customerName(), id);

		CustomerEntity updatedCustomer = new CustomerEntity(
				currentCustomer.id(),
				currentCustomer.customerCode(),
				request.customerName().trim(),
				request.customerType().trim(),
				trimToNull(request.contactName()),
				trimToNull(request.contactPhone()),
				trimToNull(request.regionName()),
				trimToNull(request.address()),
				request.status(),
				request.sortOrder(),
				trimToNull(request.remarks()),
				currentCustomer.createdAt(),
				currentCustomer.updatedAt());

		customerRepository.update(updatedCustomer);
		return getCustomerDetail(id);
	}

	@Transactional
	public void updateCustomerStatus(Long id, CustomerStatusUpdateRequest request) {
		getExistingCustomer(id);
		customerRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void deleteCustomer(Long id) {
		getExistingCustomer(id);
		customerRepository.deleteById(id);
	}

	private CustomerEntity getExistingCustomer(Long id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.CUSTOMER_NOT_FOUND));
	}

	private boolean matchesQuery(CustomerEntity entity, CustomerListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.customerCode())
				&& !entity.customerCode().contains(queryRequest.customerCode().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.customerName())
				&& !entity.customerName().contains(queryRequest.customerName().trim())) {
			return false;
		}

		if (StringUtils.hasText(queryRequest.contactName())
				&& (entity.contactName() == null
					|| !entity.contactName().contains(queryRequest.contactName().trim()))) {
			return false;
		}

		if (queryRequest.status() != null && !Objects.equals(entity.status(), queryRequest.status())) {
			return false;
		}

		return true;
	}

	private void validateUniqueName(String customerName, Long excludeId) {
		if (customerRepository.existsByCustomerName(customerName.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.CUSTOMER_NAME_DUPLICATED);
		}
	}

	private String generateCustomerCode() {
		for (int index = 0; index < 20; index += 1) {
			String customerCode = "CUS-" + LocalDateTime.now().format(CUSTOMER_CODE_FORMATTER);

			if (index > 0) {
				customerCode += "-" + index;
			}

			if (!customerRepository.existsByCustomerCode(customerCode, null)) {
				return customerCode;
			}
		}

		throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "客户编号生成失败，请稍后重试");
	}

	private CustomerListItemResponse toListItemResponse(CustomerEntity entity) {
		return new CustomerListItemResponse(
				entity.id(),
				entity.customerCode(),
				entity.customerName(),
				entity.customerType(),
				entity.contactName(),
				entity.contactPhone(),
				entity.regionName(),
				entity.address(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.sortOrder(),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private CustomerDetailResponse toDetailResponse(CustomerEntity entity) {
		return new CustomerDetailResponse(
				entity.id(),
				entity.customerCode(),
				entity.customerName(),
				entity.customerType(),
				entity.contactName(),
				entity.contactPhone(),
				entity.regionName(),
				entity.address(),
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

	private String toIsoDateTime(LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
