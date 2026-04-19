package com.nongcang.server.modules.outboundorder.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.outboundorder.domain.dto.OutboundOrderCreateRequest;
import com.nongcang.server.modules.outboundorder.domain.dto.OutboundOrderListQueryRequest;
import com.nongcang.server.modules.outboundorder.domain.dto.OutboundOrderUpdateRequest;
import com.nongcang.server.modules.outboundorder.domain.vo.OutboundOrderDetailResponse;
import com.nongcang.server.modules.outboundorder.domain.vo.OutboundOrderListItemResponse;
import com.nongcang.server.modules.outboundorder.service.OutboundOrderService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/outbound-order")
public class OutboundOrderController {

	private final OutboundOrderService outboundOrderService;

	public OutboundOrderController(OutboundOrderService outboundOrderService) {
		this.outboundOrderService = outboundOrderService;
	}

	@GetMapping("/list")
	public ApiResponse<List<OutboundOrderListItemResponse>> getOutboundOrderList(
			@Valid @ModelAttribute OutboundOrderListQueryRequest queryRequest) {
		return ApiResponse.success(outboundOrderService.getOutboundOrderList(queryRequest));
	}

	@GetMapping("/{id}")
	public ApiResponse<OutboundOrderDetailResponse> getOutboundOrderDetail(@PathVariable Long id) {
		return ApiResponse.success(outboundOrderService.getOutboundOrderDetail(id));
	}

	@PostMapping
	public ApiResponse<OutboundOrderDetailResponse> createOutboundOrder(
			@Valid @RequestBody OutboundOrderCreateRequest request) {
		return ApiResponse.success("新增成功", outboundOrderService.createOutboundOrder(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<OutboundOrderDetailResponse> updateOutboundOrder(
			@PathVariable Long id,
			@Valid @RequestBody OutboundOrderUpdateRequest request) {
		return ApiResponse.success("更新成功", outboundOrderService.updateOutboundOrder(id, request));
	}

	@PatchMapping("/{id}/cancel")
	public ApiResponse<Void> cancelOutboundOrder(@PathVariable Long id) {
		outboundOrderService.cancelOutboundOrder(id);
		return ApiResponse.success("出库单已取消", null);
	}
}
