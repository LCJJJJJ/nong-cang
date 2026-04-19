package com.nongcang.server.modules.systemuser.domain.vo;

public record SystemUserListItemResponse(
		Long id,
		String userCode,
		String username,
		String displayName,
		String phone,
		String roleCode,
		String roleName,
		Long warehouseId,
		String warehouseName,
		Integer status,
		String statusLabel,
		String remarks,
		String createdAt,
		String updatedAt) {
}
