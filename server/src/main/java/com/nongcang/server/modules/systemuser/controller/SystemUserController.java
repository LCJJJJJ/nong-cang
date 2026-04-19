package com.nongcang.server.modules.systemuser.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserCreateRequest;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserListQueryRequest;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserResetPasswordRequest;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserStatusUpdateRequest;
import com.nongcang.server.modules.systemuser.domain.dto.SystemUserUpdateRequest;
import com.nongcang.server.modules.systemuser.domain.vo.SystemUserDetailResponse;
import com.nongcang.server.modules.systemuser.domain.vo.SystemUserListItemResponse;
import com.nongcang.server.modules.systemuser.domain.vo.SystemUserRoleOptionResponse;
import com.nongcang.server.modules.systemuser.service.SystemUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/system-user")
public class SystemUserController {

	private final SystemUserService systemUserService;

	public SystemUserController(SystemUserService systemUserService) {
		this.systemUserService = systemUserService;
	}

	@GetMapping("/list")
	public ApiResponse<List<SystemUserListItemResponse>> getSystemUserList(
			@Valid @ModelAttribute SystemUserListQueryRequest queryRequest,
			Authentication authentication) {
		systemUserService.assertAdmin(authentication);
		return ApiResponse.success(systemUserService.getSystemUserList(queryRequest));
	}

	@GetMapping("/role-options")
	public ApiResponse<List<SystemUserRoleOptionResponse>> getRoleOptions(Authentication authentication) {
		systemUserService.assertAdmin(authentication);
		return ApiResponse.success(systemUserService.getRoleOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<SystemUserDetailResponse> getSystemUserDetail(
			@PathVariable Long id,
			Authentication authentication) {
		systemUserService.assertAdmin(authentication);
		return ApiResponse.success(systemUserService.getSystemUserDetail(id));
	}

	@PostMapping
	public ApiResponse<SystemUserDetailResponse> createSystemUser(
			@Valid @RequestBody SystemUserCreateRequest request,
			Authentication authentication) {
		systemUserService.assertAdmin(authentication);
		return ApiResponse.success("新增成功", systemUserService.createSystemUser(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<SystemUserDetailResponse> updateSystemUser(
			@PathVariable Long id,
			@Valid @RequestBody SystemUserUpdateRequest request,
			Authentication authentication) {
		systemUserService.assertAdmin(authentication);
		return ApiResponse.success("更新成功", systemUserService.updateSystemUser(id, request));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<Void> updateSystemUserStatus(
			@PathVariable Long id,
			@Valid @RequestBody SystemUserStatusUpdateRequest request,
			Authentication authentication) {
		systemUserService.assertAdmin(authentication);
		systemUserService.updateSystemUserStatus(id, request);
		return ApiResponse.success("状态更新成功", null);
	}

	@PatchMapping("/{id}/reset-password")
	public ApiResponse<Void> resetPassword(
			@PathVariable Long id,
			@Valid @RequestBody SystemUserResetPasswordRequest request,
			Authentication authentication) {
		systemUserService.assertAdmin(authentication);
		systemUserService.resetPassword(id, request);
		return ApiResponse.success("密码重置成功", null);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteSystemUser(@PathVariable Long id, Authentication authentication) {
		systemUserService.assertAdmin(authentication);
		systemUserService.deleteSystemUser(id);
		return ApiResponse.success("删除成功", null);
	}
}
