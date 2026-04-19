package com.nongcang.server.modules.assistant.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.warehouse.domain.dto.WarehouseListQueryRequest;
import com.nongcang.server.modules.warehouse.domain.vo.WarehouseListItemResponse;
import com.nongcang.server.modules.warehouse.service.WarehouseService;
import com.nongcang.server.modules.warehouselocation.domain.dto.WarehouseLocationListQueryRequest;
import com.nongcang.server.modules.warehouselocation.domain.vo.WarehouseLocationListItemResponse;
import com.nongcang.server.modules.warehouselocation.service.WarehouseLocationService;
import com.nongcang.server.modules.warehousezone.domain.dto.WarehouseZoneListQueryRequest;
import com.nongcang.server.modules.warehousezone.domain.vo.WarehouseZoneListItemResponse;
import com.nongcang.server.modules.warehousezone.service.WarehouseZoneService;
import org.springframework.stereotype.Service;

@Service
public class AssistantWarehouseQueryService {

	private final WarehouseService warehouseService;
	private final WarehouseZoneService warehouseZoneService;
	private final WarehouseLocationService warehouseLocationService;
	private final AssistantQuerySupport assistantQuerySupport;

	public AssistantWarehouseQueryService(
			WarehouseService warehouseService,
			WarehouseZoneService warehouseZoneService,
			WarehouseLocationService warehouseLocationService,
			AssistantQuerySupport assistantQuerySupport) {
		this.warehouseService = warehouseService;
		this.warehouseZoneService = warehouseZoneService;
		this.warehouseLocationService = warehouseLocationService;
		this.assistantQuerySupport = assistantQuerySupport;
	}

	public AssistantToolExecutionResult execute(AssistantToolArguments arguments, int maxRows) {
		int limit = arguments.resolveLimit(10, maxRows);
		return switch (normalizeEntityType(arguments.entityType())) {
			case "warehouse" -> createResult(
					"仓库查询",
					"/warehouses",
					"仓库信息管理",
					assistantQuerySupport.filterItems(
							warehouseService.getWarehouseList(new WarehouseListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("warehouseCode", "warehouseName", "warehouseType", "managerName", "contactPhone",
							"statusLabel"),
					limit);
			case "zone" -> createResult(
					"库区查询",
					"/warehouse-zones",
					"库区管理",
					assistantQuerySupport.filterItems(
							warehouseZoneService.getWarehouseZoneList(new WarehouseZoneListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("zoneCode", "warehouseName", "zoneName", "zoneType", "statusLabel"),
					limit);
			case "location" -> createResult(
					"库位查询",
					"/warehouse-locations",
					"库位管理",
					assistantQuerySupport.filterItems(
							warehouseLocationService.getWarehouseLocationList(
									new WarehouseLocationListQueryRequest(null, null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("locationCode", "warehouseName", "zoneName", "locationName", "statusLabel"),
					limit);
			case "structure" -> createStructureResult(arguments, limit);
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "仓储查询实体不正确");
		};
	}

	private AssistantToolExecutionResult createResult(
			String title,
			String routePath,
			String routeLabel,
			List<?> items,
			List<String> preferredKeys,
			int limit) {
		AssistantResultBlock block = assistantQuerySupport.toTableBlock(
				title,
				routePath,
				routeLabel,
				items,
				preferredKeys,
				limit,
				title + "：");
		return new AssistantToolExecutionResult("query_warehouse_data", block.summary(), List.of(block));
	}

	private AssistantToolExecutionResult createStructureResult(AssistantToolArguments arguments, int limit) {
		List<WarehouseListItemResponse> warehouses = assistantQuerySupport.filterItems(
				warehouseService.getWarehouseList(new WarehouseListQueryRequest(null, null, null, null)),
				arguments.keyword(),
				arguments.relatedKeyword());
		List<WarehouseZoneListItemResponse> allZones = warehouseZoneService
				.getWarehouseZoneList(new WarehouseZoneListQueryRequest(null, null, null, null));
		List<WarehouseLocationListItemResponse> allLocations = warehouseLocationService
				.getWarehouseLocationList(new WarehouseLocationListQueryRequest(null, null, null, null, null));

		List<Map<String, String>> rows = new ArrayList<>();
		for (WarehouseListItemResponse warehouse : warehouses.stream().limit(limit).toList()) {
			List<WarehouseZoneListItemResponse> matchedZones = allZones.stream()
					.filter(zone -> zone.warehouseId().equals(warehouse.id()))
					.toList();
			for (WarehouseZoneListItemResponse zone : matchedZones) {
				List<WarehouseLocationListItemResponse> matchedLocations = allLocations.stream()
						.filter(location -> location.zoneId().equals(zone.id()))
						.toList();
				if (matchedLocations.isEmpty()) {
					Map<String, String> row = new LinkedHashMap<>();
					row.put("warehouseName", warehouse.warehouseName());
					row.put("zoneName", zone.zoneName());
					row.put("locationName", "暂无库位");
					row.put("statusLabel", zone.statusLabel());
					rows.add(row);
					continue;
				}
				for (WarehouseLocationListItemResponse location : matchedLocations) {
					Map<String, String> row = new LinkedHashMap<>();
					row.put("warehouseName", warehouse.warehouseName());
					row.put("zoneName", zone.zoneName());
					row.put("locationName", location.locationName());
					row.put("statusLabel", location.statusLabel());
					rows.add(row);
				}
			}
		}

		AssistantResultBlock block = new AssistantResultBlock(
				"仓库结构查询",
				rows.isEmpty() ? "仓库结构查询：未找到匹配结果" : "仓库结构查询：共匹配到 " + rows.size() + " 条结构记录",
				"/warehouse-locations",
				"库位管理",
				List.of(
						new AssistantColumn("warehouseName", "仓库名称"),
						new AssistantColumn("zoneName", "库区名称"),
						new AssistantColumn("locationName", "库位名称"),
						new AssistantColumn("statusLabel", "状态")),
				rows);
		return new AssistantToolExecutionResult("query_warehouse_data", block.summary(), List.of(block));
	}

	private String normalizeEntityType(String entityType) {
		return entityType == null ? "" : entityType.trim().toLowerCase();
	}
}
