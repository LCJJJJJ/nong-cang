package com.nongcang.server.modules.assistant.service;

import java.util.List;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.abnormalstock.domain.dto.AbnormalStockListQueryRequest;
import com.nongcang.server.modules.abnormalstock.service.AbnormalStockService;
import com.nongcang.server.modules.lossrecord.domain.dto.LossRecordListQueryRequest;
import com.nongcang.server.modules.lossrecord.service.LossRecordService;
import com.nongcang.server.modules.qualityinspection.domain.dto.QualityInspectionListQueryRequest;
import com.nongcang.server.modules.qualityinspection.service.QualityInspectionService;
import org.springframework.stereotype.Service;

@Service
public class AssistantQualityQueryService {

	private final QualityInspectionService qualityInspectionService;
	private final AbnormalStockService abnormalStockService;
	private final LossRecordService lossRecordService;
	private final AssistantQuerySupport assistantQuerySupport;

	public AssistantQualityQueryService(
			QualityInspectionService qualityInspectionService,
			AbnormalStockService abnormalStockService,
			LossRecordService lossRecordService,
			AssistantQuerySupport assistantQuerySupport) {
		this.qualityInspectionService = qualityInspectionService;
		this.abnormalStockService = abnormalStockService;
		this.lossRecordService = lossRecordService;
		this.assistantQuerySupport = assistantQuerySupport;
	}

	public AssistantToolExecutionResult execute(AssistantToolArguments arguments, int maxRows) {
		int limit = arguments.resolveLimit(10, maxRows);
		return switch (normalizeEntityType(arguments.entityType())) {
			case "inspection" -> createResult(
					"质检单查询",
					"/quality-inspections",
					"质检单管理",
					assistantQuerySupport.filterItems(
							qualityInspectionService.getQualityInspectionList(
									new QualityInspectionListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("inspectionCode", "sourceLabel", "sourceCode", "productName", "warehouseName",
							"inspectQuantity", "unqualifiedQuantity", "resultStatusLabel"),
					limit);
			case "abnormal_stock" -> createResult(
					"异常库存查询",
					"/abnormal-stocks",
					"异常库存管理",
					assistantQuerySupport.filterItems(
							abnormalStockService.getAbnormalStockList(new AbnormalStockListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("abnormalCode", "productName", "warehouseName", "zoneName", "locationName",
							"lockedQuantity", "statusLabel", "createdAt"),
					limit);
			case "loss_record" -> createResult(
					"损耗记录查询",
					"/loss-records",
					"损耗登记管理",
					assistantQuerySupport.filterItems(
							lossRecordService.getLossRecordList(new LossRecordListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("lossCode", "sourceType", "productName", "warehouseName", "zoneName", "locationName",
							"quantity", "lossReason", "createdAt"),
					limit);
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "质检损耗查询实体不正确");
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
		return new AssistantToolExecutionResult("query_quality_loss_data", block.summary(), List.of(block));
	}

	private String normalizeEntityType(String entityType) {
		return entityType == null ? "" : entityType.trim().toLowerCase();
	}
}
