package com.nongcang.server.common.exception;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {

	OK("OK", "操作成功", HttpStatus.OK),
	BAD_REQUEST("BAD_REQUEST", "请求参数格式错误", HttpStatus.BAD_REQUEST),
	VALIDATION_FAILED("VALIDATION_FAILED", "请求参数校验失败", HttpStatus.UNPROCESSABLE_ENTITY),
	UNAUTHORIZED("UNAUTHORIZED", "登录已失效，请重新登录", HttpStatus.UNAUTHORIZED),
	FORBIDDEN("FORBIDDEN", "当前账号无权执行该操作", HttpStatus.FORBIDDEN),
	INVALID_CREDENTIALS("INVALID_CREDENTIALS", "账号或密码错误", HttpStatus.UNAUTHORIZED),
	INVALID_ACCESS_TOKEN("INVALID_ACCESS_TOKEN", "访问令牌无效，请重新登录", HttpStatus.UNAUTHORIZED),
	INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "刷新令牌无效，请重新登录", HttpStatus.UNAUTHORIZED),
	RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "请求的资源不存在", HttpStatus.NOT_FOUND),
	BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "业务处理失败", HttpStatus.CONFLICT),
	DEMO_BUSINESS_ERROR("DEMO_BUSINESS_ERROR", "这是一个演示业务错误", HttpStatus.CONFLICT),
	INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "系统繁忙，请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR),
	SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "服务暂时不可用，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE);

	private final String code;

	private final String message;

	private final HttpStatus status;

	CommonErrorCode(String code, String message, HttpStatus status) {
		this.code = code;
		this.message = message;
		this.status = status;
	}

	@Override
	public String code() {
		return code;
	}

	@Override
	public String message() {
		return message;
	}

	@Override
	public HttpStatus status() {
		return status;
	}
}
