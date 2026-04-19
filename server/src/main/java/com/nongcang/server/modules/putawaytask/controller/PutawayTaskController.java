package com.nongcang.server.modules.putawaytask.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.putawaytask.domain.dto.PutawayAssignRequest;
import com.nongcang.server.modules.putawaytask.domain.dto.PutawayTaskListQueryRequest;
import com.nongcang.server.modules.putawaytask.domain.vo.PutawayTaskDetailResponse;
import com.nongcang.server.modules.putawaytask.domain.vo.PutawayTaskListItemResponse;
import com.nongcang.server.modules.putawaytask.service.PutawayTaskService;
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
@RequestMapping("/api/putaway-task")
public class PutawayTaskController {

	private final PutawayTaskService putawayTaskService;

	public PutawayTaskController(PutawayTaskService putawayTaskService) {
		this.putawayTaskService = putawayTaskService;
	}

	@GetMapping("/list")
	public ApiResponse<List<PutawayTaskListItemResponse>> getPutawayTaskList(
			@Valid @ModelAttribute PutawayTaskListQueryRequest queryRequest) {
		return ApiResponse.success(putawayTaskService.getPutawayTaskList(queryRequest));
	}

	@GetMapping("/{id}")
	public ApiResponse<PutawayTaskDetailResponse> getPutawayTaskDetail(@PathVariable Long id) {
		return ApiResponse.success(putawayTaskService.getPutawayTaskDetail(id));
	}

	@PatchMapping("/{id}/assign")
	public ApiResponse<PutawayTaskDetailResponse> assignLocation(
			@PathVariable Long id,
			@Valid @RequestBody PutawayAssignRequest request) {
		return ApiResponse.success("分配库位成功", putawayTaskService.assignLocation(id, request));
	}

	@PatchMapping("/{id}/complete")
	public ApiResponse<Void> completeTask(@PathVariable Long id) {
		putawayTaskService.completeTask(id);
		return ApiResponse.success("上架完成", null);
	}

	@PatchMapping("/{id}/cancel")
	public ApiResponse<Void> cancelTask(@PathVariable Long id) {
		putawayTaskService.cancelTask(id);
		return ApiResponse.success("任务已取消", null);
	}
}
