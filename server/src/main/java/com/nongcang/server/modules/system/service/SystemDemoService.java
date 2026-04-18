package com.nongcang.server.modules.system.service;

import java.time.OffsetDateTime;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.system.domain.dto.SystemEchoRequest;
import com.nongcang.server.modules.system.domain.vo.SystemEchoResponse;
import com.nongcang.server.modules.system.domain.vo.SystemPingResponse;
import org.springframework.stereotype.Service;

@Service
public class SystemDemoService {

	public SystemPingResponse ping() {
		return new SystemPingResponse(20260418001L, "server", "UP", OffsetDateTime.now().toString());
	}

	public SystemEchoResponse echo(SystemEchoRequest request) {
		String normalizedContent = request.content().trim();
		return new SystemEchoResponse(normalizedContent, normalizedContent.length());
	}

	public void triggerBusinessError() {
		throw new BusinessException(CommonErrorCode.DEMO_BUSINESS_ERROR, "这是一个演示业务错误，用于验证前后端统一错误处理");
	}
}
