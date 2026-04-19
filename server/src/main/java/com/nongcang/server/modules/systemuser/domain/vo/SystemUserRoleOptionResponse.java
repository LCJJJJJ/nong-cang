package com.nongcang.server.modules.systemuser.domain.vo;

public record SystemUserRoleOptionResponse(
		String roleCode,
		String roleName,
		String description,
		boolean warehouseRequired) {
}
