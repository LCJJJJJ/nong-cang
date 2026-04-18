package com.nongcang.server.modules.system.domain.vo;

public record SystemPingResponse(Long demoId, String service, String status, String serverTime) {
}
