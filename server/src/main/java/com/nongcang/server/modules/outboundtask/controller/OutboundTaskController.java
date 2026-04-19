package com.nongcang.server.modules.outboundtask.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.outboundtask.domain.dto.OutboundAssignRequest;
import com.nongcang.server.modules.outboundtask.domain.dto.OutboundTaskListQueryRequest;
import com.nongcang.server.modules.outboundtask.domain.vo.OutboundTaskDetailResponse;
import com.nongcang.server.modules.outboundtask.domain.vo.OutboundTaskListItemResponse;
import com.nongcang.server.modules.outboundtask.domain.vo.OutboundTaskStockOptionResponse;
import com.nongcang.server.modules.outboundtask.service.OutboundTaskService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/outbound-task")
public class OutboundTaskController {

	private final OutboundTaskService outboundTaskService;

	public OutboundTaskController(OutboundTaskService outboundTaskService) {
		this.outboundTaskService = outboundTaskService;
	}

	@GetMapping("/list")
	public ApiResponse<List<OutboundTaskListItemResponse>> getOutboundTaskList(
			@Valid @ModelAttribute OutboundTaskListQueryRequest queryRequest) {
		return ApiResponse.success(outboundTaskService.getOutboundTaskList(queryRequest));
	}

	@GetMapping("/{id}")
	public ApiResponse<OutboundTaskDetailResponse> getOutboundTaskDetail(@PathVariable Long id) {
		return ApiResponse.success(outboundTaskService.getOutboundTaskDetail(id));
	}

	@GetMapping("/{id}/stock-options")
	public ApiResponse<List<OutboundTaskStockOptionResponse>> getStockOptions(@PathVariable Long id) {
		return ApiResponse.success(outboundTaskService.getStockOptions(id));
	}

	@PatchMapping("/{id}/assign")
	public ApiResponse<OutboundTaskDetailResponse> assignStock(
			@PathVariable Long id,
			@Valid @RequestBody OutboundAssignRequest request) {
		return ApiResponse.success("分配库存成功", outboundTaskService.assignStock(id, request));
	}

	@PatchMapping("/{id}/pick")
	public ApiResponse<Void> confirmPick(@PathVariable Long id) {
		outboundTaskService.confirmPick(id);
		return ApiResponse.success("拣货完成", null);
	}

	@PatchMapping("/{id}/complete")
	public ApiResponse<Void> completeTask(@PathVariable Long id) {
		outboundTaskService.completeTask(id);
		return ApiResponse.success("出库完成", null);
	}

	@PatchMapping("/{id}/cancel")
	public ApiResponse<Void> cancelTask(@PathVariable Long id) {
		outboundTaskService.cancelTask(id);
		return ApiResponse.success("任务已取消", null);
	}
}
