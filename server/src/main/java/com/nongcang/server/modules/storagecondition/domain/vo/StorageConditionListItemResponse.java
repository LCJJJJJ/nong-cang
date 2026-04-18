package com.nongcang.server.modules.storagecondition.domain.vo;

public record StorageConditionListItemResponse(
		Long id,
		String conditionCode,
		String conditionName,
		String storageType,
		Double temperatureMin,
		Double temperatureMax,
		Double humidityMin,
		Double humidityMax,
		String lightRequirement,
		String ventilationRequirement,
		Integer status,
		String statusLabel,
		Integer sortOrder,
		String remarks,
		String createdAt,
		String updatedAt) {
}
