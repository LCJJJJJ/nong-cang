package com.nongcang.server.modules.assistant.service;

import java.util.List;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.outboundorder.domain.dto.OutboundOrderListQueryRequest;
import com.nongcang.server.modules.outboundorder.service.OutboundOrderService;
import com.nongcang.server.modules.outboundrecord.domain.dto.OutboundRecordListQueryRequest;
import com.nongcang.server.modules.outboundrecord.service.OutboundRecordService;
import com.nongcang.server.modules.outboundtask.domain.dto.OutboundTaskListQueryRequest;
import com.nongcang.server.modules.outboundtask.service.OutboundTaskService;
import org.springframework.stereotype.Service;

@Service
public class AssistantOutboundQueryService {

	private final OutboundOrderService outboundOrderService;
	private final OutboundTaskService outboundTaskService;
	private final OutboundRecordService outboundRecordService;
	private final AssistantQuerySupport assistantQuerySupport;

	public AssistantOutboundQueryService(
			OutboundOrderService outboundOrderService,
			OutboundTaskService outboundTaskService,
			OutboundRecordService outboundRecordService,
			AssistantQuerySupport assistantQuerySupport) {
		this.outboundOrderService = outboundOrderService;
		this.outboundTaskService = outboundTaskService;
		this.outboundRecordService = outboundRecordService;
		this.assistantQuerySupport = assistantQuerySupport;
	}

	public AssistantToolExecutionResult execute(AssistantToolArguments arguments, int maxRows) {
		int limit = arguments.resolveLimit(10, maxRows);
		return switch (normalizeEntityType(arguments.entityType())) {
			case "outbound_order" -> createResult(
					"出库单查询",
					"/outbound-orders",
					"出库单管理",
					assistantQuerySupport.filterItems(
							outboundOrderService.getOutboundOrderList(new OutboundOrderListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("orderCode", "customerName", "warehouseName", "totalQuantity", "statusLabel", "createdAt"),
					limit);
			case "outbound_task" -> createResult(
					"拣货出库任务查询",
					"/outbound-tasks",
					"拣货出库任务管理",
					assistantQuerySupport.filterItems(
							outboundTaskService.getOutboundTaskList(new OutboundTaskListQueryRequest(null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("taskCode", "warehouseName", "zoneName", "locationName", "productName", "quantity",
							"statusLabel"),
					limit);
			case "outbound_record" -> createResult(
					"出库记录查询",
					"/outbound-records",
					"出库记录查询",
					assistantQuerySupport.filterItems(
							outboundRecordService.getOutboundRecordList(new OutboundRecordListQueryRequest(null, null, null, null)),
							arguments.keyword(),
							arguments.relatedKeyword()),
					List.of("recordCode", "outboundOrderCode", "customerName", "warehouseName", "zoneName",
							"locationName", "productName", "quantity", "occurredAt"),
					limit);
			default -> throw new BusinessException(CommonErrorCode.ASSISTANT_TOOL_INVALID, "出库查询实体不正确");
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
		return new AssistantToolExecutionResult("query_outbound_data", block.summary(), List.of(block));
	}

	private String normalizeEntityType(String entityType) {
		return entityType == null ? "" : entityType.trim().toLowerCase();
	}
}
