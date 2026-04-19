package com.nongcang.server.modules.inboundorder.controller;

import java.util.List;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.inboundorder.domain.dto.InboundOrderCreateRequest;
import com.nongcang.server.modules.inboundorder.domain.dto.InboundOrderListQueryRequest;
import com.nongcang.server.modules.inboundorder.domain.dto.InboundOrderUpdateRequest;
import com.nongcang.server.modules.inboundorder.domain.vo.InboundOrderDetailResponse;
import com.nongcang.server.modules.inboundorder.domain.vo.InboundOrderListItemResponse;
import com.nongcang.server.modules.inboundorder.service.InboundOrderService;
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
@RequestMapping("/api/inbound-order")
public class InboundOrderController {

	private final InboundOrderService inboundOrderService;

	public InboundOrderController(InboundOrderService inboundOrderService) {
		this.inboundOrderService = inboundOrderService;
	}

	@GetMapping("/list")
	public ApiResponse<List<InboundOrderListItemResponse>> getInboundOrderList(
			@Valid @ModelAttribute InboundOrderListQueryRequest queryRequest) {
		return ApiResponse.success(inboundOrderService.getInboundOrderList(queryRequest));
	}

	@GetMapping("/{id}")
	public ApiResponse<InboundOrderDetailResponse> getInboundOrderDetail(@PathVariable Long id) {
		return ApiResponse.success(inboundOrderService.getInboundOrderDetail(id));
	}

	@PostMapping
	public ApiResponse<InboundOrderDetailResponse> createInboundOrder(
			@Valid @RequestBody InboundOrderCreateRequest request) {
		return ApiResponse.success("新增成功", inboundOrderService.createInboundOrder(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<InboundOrderDetailResponse> updateInboundOrder(
			@PathVariable Long id,
			@Valid @RequestBody InboundOrderUpdateRequest request) {
		return ApiResponse.success("更新成功", inboundOrderService.updateInboundOrder(id, request));
	}

	@PatchMapping("/{id}/arrive")
	public ApiResponse<Void> confirmArrival(@PathVariable Long id) {
		inboundOrderService.confirmArrival(id);
		return ApiResponse.success("到货确认成功", null);
	}

	@PatchMapping("/{id}/cancel")
	public ApiResponse<Void> cancelInboundOrder(@PathVariable Long id) {
		inboundOrderService.cancelInboundOrder(id);
		return ApiResponse.success("入库单已取消", null);
	}
}
