package com.nongcang.server.modules.systemuser.domain.entity;

import java.time.LocalDateTime;

public record SystemUserEntity(
		Long id,
		String userCode,
		String username,
		String passwordHash,
		String displayName,
		String phone,
		String roleCode,
		Long warehouseId,
		String warehouseName,
		Integer status,
		String remarks,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
