package com.nongcang.server.modules.abnormalstock.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.abnormalstock.domain.dto.AbnormalStockLossRequest;
import com.nongcang.server.modules.abnormalstock.domain.dto.AbnormalStockListQueryRequest;
import com.nongcang.server.modules.abnormalstock.domain.vo.AbnormalStockDetailResponse;
import com.nongcang.server.modules.abnormalstock.domain.vo.AbnormalStockListItemResponse;
import com.nongcang.server.modules.abnormalstock.domain.vo.AbnormalStockOptionResponse;
import com.nongcang.server.modules.abnormalstock.service.AbnormalStockService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/abnormal-stock")
public class AbnormalStockController {

	private final AbnormalStockService abnormalStockService;

	public AbnormalStockController(AbnormalStockService abnormalStockService) {
		this.abnormalStockService = abnormalStockService;
	}

	@GetMapping("/list")
	public ApiResponse<List<AbnormalStockListItemResponse>> getAbnormalStockList(
			@Valid @ModelAttribute AbnormalStockListQueryRequest queryRequest) {
		return ApiResponse.success(abnormalStockService.getAbnormalStockList(queryRequest));
	}

	@GetMapping("/options")
	public ApiResponse<List<AbnormalStockOptionResponse>> getAbnormalStockOptions() {
		return ApiResponse.success(abnormalStockService.getAbnormalStockOptions());
	}

	@GetMapping("/{id}")
	public ApiResponse<AbnormalStockDetailResponse> getAbnormalStockDetail(@PathVariable Long id) {
		return ApiResponse.success(abnormalStockService.getAbnormalStockDetail(id));
	}

	@PatchMapping("/{id}/release")
	public ApiResponse<Void> release(@PathVariable Long id) {
		abnormalStockService.release(id);
		return ApiResponse.success("异常库存已释放", null);
	}

	@PostMapping("/{id}/dispose-loss")
	public ApiResponse<Void> disposeToLoss(
			@PathVariable Long id,
			@Valid @RequestBody AbnormalStockLossRequest request) {
		abnormalStockService.disposeToLoss(id, request);
		return ApiResponse.success("异常库存已转损耗", null);
	}
}
