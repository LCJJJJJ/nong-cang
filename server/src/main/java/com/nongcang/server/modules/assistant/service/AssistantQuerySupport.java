package com.nongcang.server.modules.assistant.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AssistantQuerySupport {

	private static final Set<String> HIDDEN_KEYS = Set.of(
			"id",
			"children",
			"parentId",
			"ancestorPath",
			"categoryId",
			"warehouseId",
			"zoneId",
			"locationId",
			"supplierId",
			"customerId",
			"productId",
			"unitId",
			"originId",
			"storageConditionId",
			"qualityGradeId",
			"alertRecordId",
			"sourceId",
			"outboundOrderId",
			"outboundTaskId",
			"ruleId");

	private static final Map<String, String> LABEL_MAPPINGS = Map.ofEntries(
			Map.entry("sessionCode", "会话编号"),
			Map.entry("categoryCode", "分类编号"),
			Map.entry("categoryName", "分类名称"),
			Map.entry("categoryLevel", "层级"),
			Map.entry("productCode", "商品编号"),
			Map.entry("productName", "商品名称"),
			Map.entry("productSpecification", "规格"),
			Map.entry("warehouseCode", "仓库编号"),
			Map.entry("warehouseName", "仓库名称"),
			Map.entry("warehouseType", "仓库类型"),
			Map.entry("zoneCode", "库区编号"),
			Map.entry("zoneName", "库区名称"),
			Map.entry("zoneType", "库区类型"),
			Map.entry("locationCode", "库位编号"),
			Map.entry("locationName", "库位名称"),
			Map.entry("supplierCode", "供应商编号"),
			Map.entry("supplierName", "供应商名称"),
			Map.entry("customerCode", "客户编号"),
			Map.entry("customerName", "客户名称"),
			Map.entry("contactName", "联系人"),
			Map.entry("contactPhone", "联系电话"),
			Map.entry("originCode", "产地编号"),
			Map.entry("originName", "产地名称"),
			Map.entry("provinceName", "省份"),
			Map.entry("unitCode", "单位编号"),
			Map.entry("unitName", "单位名称"),
			Map.entry("unitSymbol", "单位符号"),
			Map.entry("unitType", "单位类型"),
			Map.entry("conditionCode", "条件编号"),
			Map.entry("conditionName", "条件名称"),
			Map.entry("storageType", "储存类型"),
			Map.entry("lightRequirement", "避光要求"),
			Map.entry("ventilationRequirement", "通风要求"),
			Map.entry("storageConditionName", "储存条件"),
			Map.entry("gradeCode", "等级编号"),
			Map.entry("gradeName", "等级名称"),
			Map.entry("orderCode", "单据编号"),
			Map.entry("outboundOrderCode", "出库单编号"),
			Map.entry("taskCode", "任务编号"),
			Map.entry("recordCode", "记录编号"),
			Map.entry("stocktakingCode", "盘点单编号"),
			Map.entry("transactionCode", "流水编号"),
			Map.entry("adjustmentCode", "调整单编号"),
			Map.entry("inspectionCode", "质检单编号"),
			Map.entry("abnormalCode", "异常库存编号"),
			Map.entry("lossCode", "损耗编号"),
			Map.entry("ruleCode", "规则编号"),
			Map.entry("ruleName", "规则名称"),
			Map.entry("alertCode", "预警编号"),
			Map.entry("noticeCode", "消息编号"),
			Map.entry("statusLabel", "状态"),
			Map.entry("enabledLabel", "启用状态"),
			Map.entry("resultStatusLabel", "结果"),
			Map.entry("severity", "严重级别"),
			Map.entry("severityLabel", "严重级别"),
			Map.entry("sourceType", "来源类型"),
			Map.entry("sourceCode", "来源编号"),
			Map.entry("sourceLabel", "来源"),
			Map.entry("title", "标题"),
			Map.entry("content", "内容"),
			Map.entry("reason", "原因"),
			Map.entry("remarks", "备注"),
			Map.entry("stockQuantity", "现存数量"),
			Map.entry("reservedQuantity", "预留数量"),
			Map.entry("lockedQuantity", "锁定数量"),
			Map.entry("availableQuantity", "可用数量"),
			Map.entry("inspectQuantity", "送检数量"),
			Map.entry("qualifiedQuantity", "合格数量"),
			Map.entry("unqualifiedQuantity", "不合格数量"),
			Map.entry("lossQuantity", "损耗数量"),
			Map.entry("quantity", "数量"),
			Map.entry("totalQuantity", "总数量"),
			Map.entry("thresholdValue", "阈值"),
			Map.entry("precisionDigits", "小数位"),
			Map.entry("countedItemCount", "已录入明细"),
			Map.entry("totalDifferenceQuantity", "差异数量"),
			Map.entry("lossReason", "损耗原因"),
			Map.entry("expectedArrivalAt", "预计到货时间"),
			Map.entry("actualArrivalAt", "实际到货时间"),
			Map.entry("occurredAt", "发生时间"),
			Map.entry("createdAt", "创建时间"),
			Map.entry("updatedAt", "更新时间"),
			Map.entry("readAt", "阅读时间"));

	private final ObjectMapper objectMapper;

	public AssistantQuerySupport(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public <T> List<T> filterItems(List<T> items, String keyword, String relatedKeyword) {
		return items.stream()
				.filter(item -> matchesAllKeywords(toMap(item), keyword, relatedKeyword))
				.toList();
	}

	public AssistantResultBlock toTableBlock(
			String title,
			String routePath,
			String routeLabel,
			List<?> items,
			List<String> preferredKeys,
			int limit,
			String emptySummaryPrefix) {
		List<Map<String, Object>> maps = items.stream()
				.map(this::toMap)
				.toList();

		if (maps.isEmpty()) {
			return new AssistantResultBlock(
					title,
					emptySummaryPrefix + "未找到匹配结果",
					routePath,
					routeLabel,
					List.of(),
					List.of());
		}

		List<String> selectedKeys = selectKeys(maps.get(0), preferredKeys);
		List<AssistantColumn> columns = selectedKeys.stream()
				.map(key -> new AssistantColumn(key, LABEL_MAPPINGS.getOrDefault(key, humanizeLabel(key))))
				.toList();

		List<Map<String, String>> rows = new ArrayList<>();
		for (Map<String, Object> map : maps.stream().limit(limit).toList()) {
			Map<String, String> row = new LinkedHashMap<>();
			for (String key : selectedKeys) {
				row.put(key, stringify(map.get(key)));
			}
			rows.add(row);
		}

		return new AssistantResultBlock(
				title,
				emptySummaryPrefix + "共匹配到 " + maps.size() + " 条记录",
				routePath,
				routeLabel,
				columns,
				rows);
	}

	public String blocksToModelContent(List<AssistantResultBlock> resultBlocks) {
		try {
			return objectMapper.writeValueAsString(resultBlocks);
		} catch (Exception exception) {
			return "[]";
		}
	}

	public String serialize(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception exception) {
			return "{}";
		}
	}

	private Map<String, Object> toMap(Object value) {
		return objectMapper.convertValue(value, new TypeReference<>() {
		});
	}

	private boolean matchesAllKeywords(Map<String, Object> valueMap, String keyword, String relatedKeyword) {
		return matchesKeyword(valueMap, keyword) && matchesKeyword(valueMap, relatedKeyword);
	}

	private boolean matchesKeyword(Map<String, Object> valueMap, String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return true;
		}

		String normalizedKeyword = normalize(keyword);
		return valueMap.values()
				.stream()
				.filter(Objects::nonNull)
				.map(this::stringify)
				.map(this::normalize)
				.anyMatch(value -> value.contains(normalizedKeyword));
	}

	private List<String> selectKeys(Map<String, Object> map, List<String> preferredKeys) {
		List<String> selectedKeys = preferredKeys.stream()
				.filter(map::containsKey)
				.filter(key -> !HIDDEN_KEYS.contains(key))
				.toList();
		if (!selectedKeys.isEmpty()) {
			return selectedKeys;
		}

		return map.keySet()
				.stream()
				.filter(key -> !HIDDEN_KEYS.contains(key))
				.filter(key -> !key.endsWith("Id"))
				.limit(8)
				.toList();
	}

	private String stringify(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof List<?> list) {
			return list.toString();
		}
		return String.valueOf(value);
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	private String humanizeLabel(String key) {
		StringBuilder labelBuilder = new StringBuilder();
		for (char character : key.toCharArray()) {
			if (Character.isUpperCase(character) && !labelBuilder.isEmpty()) {
				labelBuilder.append(' ');
			}
			labelBuilder.append(character);
		}
		return labelBuilder.toString();
	}
}
