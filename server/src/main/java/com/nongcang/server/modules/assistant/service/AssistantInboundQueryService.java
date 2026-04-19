package com.nongcang.server.modules.assistant.service;

import java.util.List;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.inboundorder.domain.dto.InboundOrderListQueryRequest;
import com.nongcang.server.modules.inboundorder.service.InboundOrderService;
import com.nongcang.server.modules.inboundrecord.domain.dto.InboundRecordListQueryRequest;
import com.nongcang.server.modules.inboundrecord.service.InboundRecordService;
import com.nongcang.server.modules.putawaytask.domain.dto.PutawayTaskListQueryRequest;
import com.nongcang.server.modules.putawaytask.service.PutawayTaskService;
import org.springframework.stereotype.Service;

@Service
public class AssistantInboundQueryService {

	private final InboundOrderService inboundOrderService;
	private final PutawayTaskService putawayTaskService;
	private final InboundRecordService inboundRecordService;
	private final AssistantQuerySupport assistantQuerySupport;

	public AssistantInboundQueryService(
			InboundOrderService inboundOrderService,
			PutawayTaskService putawayTaskService,
			InboundRecordService inboundRecordService,
			AssistantQuerySupport assistantQuerySupport) {
		this.inboundOrderService = inboundOrderService;
		this.putawayTaskService = putawayTaskService;
		this.inboundRecordService = inboundRecordService;
		this.assistantQuerySupport = assistantQuerySupport;
	}

	public AssistantToolExecutionResult execute(AssistantToolArguments arguments, int maxRows) {
		int limit = arguments.resolveLimit(10, maxRows);
		return switch (normalizeEntityType(arguments.entityType())) {
			case "inbound_order" -> createResult(
					"入库单查询",
					"/inbound-orders",
					"入库单管理",
					assistantQuerySupport.filterItems(
							inboundOrderService.getInboundOrderList(new InboundOrderListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("orderCode", "supplierName", "warehouseName", "expectedArrivalAt", "totalQuantity",
							"statusLabel"),
					limit);
			case "putaway_task" -> createResult(
					"上架任务查询",
					"/putaway-tasks",
					"上架任务管理",
					assistantQuerySupport.filterItems(
							putawayTaskService.getPutawayTaskList(new PutawayTaskListQueryRequest(null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("taskCode", "warehouseName", "zoneName", "locationName", "productName", "quantity",
							"statusLabel"),
					limit);
			case "inbound_record" -> createResult(
					"入库记录查询",
					"/inbound-records",
					"入库记录查询",
					assistantQuerySupport.filterItems(
							inboundRecordService.getInboundRecordList(new InboundRecordListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("recordCode", "orderCode", "warehouseName", "zoneName", "locationName", "productName",
							"quantity", "occurredAt"),
					limit);
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "入库查询实体不正确");
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
		return new AssistantToolExecutionResult("query_inbound_data", block.summary(), List.of(block));
	}

	private String normalizeEntityType(String entityType) {
		return entityType == null ? "" : entityType.trim().toLowerCase();
	}
}
