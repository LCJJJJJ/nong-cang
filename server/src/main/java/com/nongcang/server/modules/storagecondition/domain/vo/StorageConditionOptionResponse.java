package com.nongcang.server.modules.storagecondition.domain.vo;

public record StorageConditionOptionResponse(
		Long id,
		String label,
		String storageType,
		Integer status) {
}
