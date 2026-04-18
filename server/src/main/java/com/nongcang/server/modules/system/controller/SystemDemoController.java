package com.nongcang.server.modules.system.controller;

import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.modules.system.domain.dto.SystemEchoRequest;
import com.nongcang.server.modules.system.domain.vo.SystemEchoResponse;
import com.nongcang.server.modules.system.domain.vo.SystemPingResponse;
import com.nongcang.server.modules.system.service.SystemDemoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemDemoController {

	private final SystemDemoService systemDemoService;

	public SystemDemoController(SystemDemoService systemDemoService) {
		this.systemDemoService = systemDemoService;
	}

	@GetMapping("/ping")
	public ApiResponse<SystemPingResponse> ping() {
		return ApiResponse.success(systemDemoService.ping());
	}

	@PostMapping("/echo")
	public ApiResponse<SystemEchoResponse> echo(@Valid @RequestBody SystemEchoRequest request) {
		return ApiResponse.success(systemDemoService.echo(request));
	}

	@GetMapping("/business-error")
	public ApiResponse<Void> businessError() {
		systemDemoService.triggerBusinessError();
		return ApiResponse.success(null);
	}
}
