package com.nongcang.server.common.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.common.exception.ErrorCode;
import com.nongcang.server.common.trace.TraceIdContext;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
		boolean success,
		String code,
		String message,
		T data,
		List<FieldErrorItem> errors,
		String traceId) {

	public static <T> ApiResponse<T> success(T data) {
		return success(CommonErrorCode.OK.message(), data);
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, CommonErrorCode.OK.code(), message, data, null, TraceIdContext.getTraceId());
	}

	public static ApiResponse<Void> failure(ErrorCode errorCode) {
		return failure(errorCode, errorCode.message(), List.of());
	}

	public static ApiResponse<Void> failure(ErrorCode errorCode, String message) {
		return failure(errorCode, message, List.of());
	}

	public static ApiResponse<Void> failure(ErrorCode errorCode, String message, List<FieldErrorItem> errors) {
		return new ApiResponse<>(false, errorCode.code(), message, null, errors.isEmpty() ? null : errors,
				TraceIdContext.getTraceId());
	}
}
