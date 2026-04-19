package com.nongcang.server.modules.systemuser.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserCreateRequest;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserListQueryRequest;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserResetPasswordRequest;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserStatusUpdateRequest;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserUpdateRequest;
import com.nongcang.server.modules.systemuser.domain.entity.SystemUserEntity;
import com.nongcang.server.modules.systemuser.domain.vo.SystemUserDetailResponse;
import com.nongcang.server.modules.systemuser.domain.vo.SystemUserListItemResponse;
import com.nongcang.server.modules.systemuser.domain.vo.SystemUserRoleOptionResponse;
import com.nongcang.server.modules.systemuser.repository.SystemUserRepository;
import com.nongcang.server.modules.warehouse.repository.WarehouseRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SystemUserService {

	private static final DateTimeFormatter USER_CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	private final SystemUserRepository systemUserRepository;
	private final WarehouseRepository warehouseRepository;
	private final PasswordEncoder passwordEncoder;

	public SystemUserService(
			SystemUserRepository systemUserRepository,
			WarehouseRepository warehouseRepository,
			PasswordEncoder passwordEncoder) {
		this.systemUserRepository = systemUserRepository;
		this.warehouseRepository = warehouseRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<SystemUserListItemResponse> getSystemUserList(SystemUserListQueryRequest queryRequest) {
		return systemUserRepository.findAll().stream()
				.filter(user -> matchesQuery(user, queryRequest))
				.map(this::toListItemResponse)
				.toList();
	}

	public List<SystemUserRoleOptionResponse> getRoleOptions() {
		return List.of(
				new SystemUserRoleOptionResponse("ADMIN", "管理员", "系统全局配置与全部业务操作", false),
				new SystemUserRoleOptionResponse("WAREHOUSE_ADMIN", "仓库管理员", "负责仓库、入库、出库和现场仓储作业", true),
				new SystemUserRoleOptionResponse("INVENTORY_ADMIN", "库存管理员", "负责库存准确性、调整和盘点", true),
				new SystemUserRoleOptionResponse("QUALITY_ADMIN", "质检管理员", "负责质检、异常库存和损耗处理", true));
	}

	public SystemUserDetailResponse getSystemUserDetail(Long id) {
		return toDetailResponse(getExistingUser(id));
	}

	@Transactional
	public SystemUserDetailResponse createSystemUser(SystemUserCreateRequest request) {
		validateUniqueFields(request.username(), request.phone(), null);
		validateRoleWarehouseConstraint(request.roleCode(), request.warehouseId());

		SystemUserEntity entity = new SystemUserEntity(
				null,
				generateUserCode(),
				request.username().trim(),
				passwordEncoder.encode(request.initialPassword()),
				request.displayName().trim(),
				request.phone().trim(),
				request.roleCode().trim().toUpperCase(),
				request.warehouseId(),
				null,
				request.status(),
				trimToNull(request.remarks()),
				null,
				null);
		long id = systemUserRepository.insert(entity);
		return getSystemUserDetail(id);
	}

	@Transactional
	public SystemUserDetailResponse updateSystemUser(Long id, SystemUserUpdateRequest request) {
		SystemUserEntity currentUser = getExistingUser(id);
		validateUniqueFields(currentUser.username(), request.phone(), id);
		validateRoleWarehouseConstraint(request.roleCode(), request.warehouseId());

		SystemUserEntity entity = new SystemUserEntity(
				currentUser.id(),
				currentUser.userCode(),
				currentUser.username(),
				currentUser.passwordHash(),
				request.displayName().trim(),
				request.phone().trim(),
				request.roleCode().trim().toUpperCase(),
				request.warehouseId(),
				null,
				request.status(),
				trimToNull(request.remarks()),
				currentUser.createdAt(),
				currentUser.updatedAt());
		systemUserRepository.update(entity);
		return getSystemUserDetail(id);
	}

	@Transactional
	public void updateSystemUserStatus(Long id, SystemUserStatusUpdateRequest request) {
		getExistingUser(id);
		systemUserRepository.updateStatus(id, request.status());
	}

	@Transactional
	public void resetPassword(Long id, SystemUserResetPasswordRequest request) {
		getExistingUser(id);
		systemUserRepository.updatePassword(id, passwordEncoder.encode(request.newPassword()));
	}

	@Transactional
	public void deleteSystemUser(Long id) {
		getExistingUser(id);
		systemUserRepository.deleteById(id);
	}

	public void assertAdmin(Authentication authentication) {
		if (authentication == null || authentication.getAuthorities().stream()
				.noneMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
			throw new BusinessException(CommonErrorCode.FORBIDDEN);
		}
	}

	private SystemUserEntity getExistingUser(Long id) {
		return systemUserRepository.findById(id)
				.orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
	}

	private boolean matchesQuery(SystemUserEntity user, SystemUserListQueryRequest queryRequest) {
		if (StringUtils.hasText(queryRequest.username())
				&& !user.username().contains(queryRequest.username().trim())) {
			return false;
		}
		if (StringUtils.hasText(queryRequest.displayName())
				&& !user.displayName().contains(queryRequest.displayName().trim())) {
			return false;
		}
		if (StringUtils.hasText(queryRequest.roleCode())
				&& !user.roleCode().equals(queryRequest.roleCode().trim().toUpperCase())) {
			return false;
		}
		if (queryRequest.warehouseId() != null && !Objects.equals(user.warehouseId(), queryRequest.warehouseId())) {
			return false;
		}
		if (queryRequest.status() != null && !Objects.equals(user.status(), queryRequest.status())) {
			return false;
		}
		return true;
	}

	private void validateUniqueFields(String username, String phone, Long excludeId) {
		if (systemUserRepository.existsByUsername(username.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "登录账号已存在");
		}
		if (systemUserRepository.existsByPhone(phone.trim(), excludeId)) {
			throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "手机号已存在");
		}
	}

	private void validateRoleWarehouseConstraint(String roleCode, Long warehouseId) {
		String normalizedRoleCode = roleCode == null ? "" : roleCode.trim().toUpperCase();
		boolean validRole = getRoleOptions().stream().anyMatch(option -> option.roleCode().equals(normalizedRoleCode));
		if (!validRole) {
			throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "角色编码不正确");
		}

		boolean warehouseRequired = !"ADMIN".equals(normalizedRoleCode);
		if (warehouseRequired && warehouseId == null) {
			throw new BusinessException(CommonErrorCode.BUSINESS_RULE_VIOLATION, "该角色必须指定负责仓库");
		}
		if (warehouseId != null && warehouseRepository.findById(warehouseId).isEmpty()) {
			throw new BusinessException(CommonErrorCode.WAREHOUSE_NOT_FOUND);
		}
	}

	private String generateUserCode() {
		return "USER-" + LocalDateTime.now().format(USER_CODE_FORMATTER);
	}

	private SystemUserListItemResponse toListItemResponse(SystemUserEntity entity) {
		return new SystemUserListItemResponse(
				entity.id(),
				entity.userCode(),
				entity.username(),
				entity.displayName(),
				entity.phone(),
				entity.roleCode(),
				resolveRoleName(entity.roleCode()),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private SystemUserDetailResponse toDetailResponse(SystemUserEntity entity) {
		return new SystemUserDetailResponse(
				entity.id(),
				entity.userCode(),
				entity.username(),
				entity.displayName(),
				entity.phone(),
				entity.roleCode(),
				resolveRoleName(entity.roleCode()),
				entity.warehouseId(),
				entity.warehouseName(),
				entity.status(),
				toStatusLabel(entity.status()),
				entity.remarks(),
				toIsoDateTime(entity.createdAt()),
				toIsoDateTime(entity.updatedAt()));
	}

	private String resolveRoleName(String roleCode) {
		return switch (roleCode) {
			case "ADMIN" -> "管理员";
			case "WAREHOUSE_ADMIN" -> "仓库管理员";
			case "INVENTORY_ADMIN" -> "库存管理员";
			case "QUALITY_ADMIN" -> "质检管理员";
			default -> roleCode;
		};
	}

	private String toStatusLabel(Integer status) {
		return status != null && status == 1 ? "启用" : "停用";
	}

	private String toIsoDateTime(LocalDateTime value) {
		return value == null ? null : value.atOffset(ZoneOffset.ofHours(8)).toString();
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
