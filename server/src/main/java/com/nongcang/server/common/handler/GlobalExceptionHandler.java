package com.nongcang.server.common.handler;

import java.util.List;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.common.exception.ErrorCode;
import com.nongcang.server.common.response.ApiResponse;
import com.nongcang.server.common.response.FieldErrorItem;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		log.warn("Business exception raised. code={}, message={}", errorCode.code(), exception.getMessage());

		return ResponseEntity.status(errorCode.status())
				.body(ApiResponse.failure(errorCode, exception.getMessage(), exception.getFieldErrors()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException exception) {
		List<FieldErrorItem> fieldErrors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::mapFieldError)
				.toList();

		return ResponseEntity.status(CommonErrorCode.VALIDATION_FAILED.status())
				.body(ApiResponse.failure(CommonErrorCode.VALIDATION_FAILED,
						CommonErrorCode.VALIDATION_FAILED.message(), fieldErrors));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
		List<FieldErrorItem> fieldErrors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::mapFieldError)
				.toList();

		return ResponseEntity.status(CommonErrorCode.VALIDATION_FAILED.status())
				.body(ApiResponse.failure(CommonErrorCode.VALIDATION_FAILED,
						CommonErrorCode.VALIDATION_FAILED.message(), fieldErrors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
			ConstraintViolationException exception) {
		List<FieldErrorItem> fieldErrors = exception.getConstraintViolations()
				.stream()
				.map(violation -> new FieldErrorItem(resolveFieldName(violation.getPropertyPath().toString()),
						violation.getMessage()))
				.toList();

		return ResponseEntity.status(CommonErrorCode.VALIDATION_FAILED.status())
				.body(ApiResponse.failure(CommonErrorCode.VALIDATION_FAILED,
						CommonErrorCode.VALIDATION_FAILED.message(), fieldErrors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
			HttpMessageNotReadableException exception) {
		return ResponseEntity.status(CommonErrorCode.BAD_REQUEST.status())
				.body(ApiResponse.failure(CommonErrorCode.BAD_REQUEST));
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException exception) {
		return ResponseEntity.status(CommonErrorCode.RESOURCE_NOT_FOUND.status())
				.body(ApiResponse.failure(CommonErrorCode.RESOURCE_NOT_FOUND));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException exception) {
		return ResponseEntity.status(CommonErrorCode.RESOURCE_NOT_FOUND.status())
				.body(ApiResponse.failure(CommonErrorCode.RESOURCE_NOT_FOUND));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
		log.error("Unexpected system exception", exception);
		return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.status())
				.body(ApiResponse.failure(CommonErrorCode.INTERNAL_SERVER_ERROR));
	}

	private FieldErrorItem mapFieldError(FieldError fieldError) {
		return new FieldErrorItem(fieldError.getField(), fieldError.getDefaultMessage());
	}

	private String resolveFieldName(String propertyPath) {
		int lastDotIndex = propertyPath.lastIndexOf('.');
		return lastDotIndex >= 0 ? propertyPath.substring(lastDotIndex + 1) : propertyPath;
	}
}
