package com.nongcang.server.common.exception;

import java.util.List;

import com.nongcang.server.common.response.FieldErrorItem;

public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	private final List<FieldErrorItem> fieldErrors;

	public BusinessException(ErrorCode errorCode) {
		this(errorCode, errorCode.message(), List.of());
	}

	public BusinessException(ErrorCode errorCode, String message) {
		this(errorCode, message, List.of());
	}

	public BusinessException(ErrorCode errorCode, String message, List<FieldErrorItem> fieldErrors) {
		super(message);
		this.errorCode = errorCode;
		this.fieldErrors = fieldErrors;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public List<FieldErrorItem> getFieldErrors() {
		return fieldErrors;
	}
}
