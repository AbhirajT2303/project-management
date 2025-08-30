package com.project_management.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.project_management.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex,
			WebRequest request) {

		log.error("Resource not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, ex.getMessage(), null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		log.warn("Validation failed for fields: {}", errors.keySet());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiResponse<>(false, "Validation failed", errors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			WebRequest request) {

		String message = "Invalid JSON format";

		// Handle specific JSON parsing errors
		if (ex.getCause() instanceof InvalidFormatException) {
			InvalidFormatException ife = (InvalidFormatException) ex.getCause();
			String fieldName = ife.getPath().isEmpty() ? "unknown" : ife.getPath().get(0).getFieldName();
			String targetType = ife.getTargetType().getSimpleName();
			message = String.format("Invalid value for field '%s'. Expected format: %s", fieldName,
					getExpectedFormat(targetType));
		} else if (ex.getCause() instanceof MismatchedInputException) {
			MismatchedInputException mie = (MismatchedInputException) ex.getCause();
			String fieldName = mie.getPath().isEmpty() ? "unknown" : mie.getPath().get(0).getFieldName();
			message = String.format("Invalid or missing value for required field '%s'", fieldName);
		} else if (ex.getCause() instanceof JsonMappingException) {
			JsonMappingException jme = (JsonMappingException) ex.getCause();
			String fieldName = jme.getPath().isEmpty() ? "unknown" : jme.getPath().get(0).getFieldName();
			message = String.format("Invalid JSON mapping for field '%s'", fieldName);
		}

		log.warn("JSON parsing error: {}", message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, message, null));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
			WebRequest request) {

		String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", ex.getValue(),
				ex.getName(), ex.getRequiredType().getSimpleName());

		log.warn("Type mismatch error: {}", message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, message, null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, WebRequest request) {

		log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse<>(false, "An unexpected error occurred", null));
	}

	/**
	 * Helper method to provide user-friendly format descriptions
	 */
	private String getExpectedFormat(String targetType) {
		switch (targetType.toLowerCase()) {
		case "localdate":
			return "YYYY-MM-DD (e.g., 2025-09-15)";
		case "localdatetime":
			return "YYYY-MM-DDTHH:MM:SS (e.g., 2025-09-15T10:30:00)";
		case "status":
			return "TODO, IN_PROGRESS, or DONE";
		case "priority":
			return "LOW, MEDIUM, or HIGH";
		case "long":
			return "numeric value (e.g., 123)";
		case "integer":
			return "numeric value (e.g., 123)";
		case "boolean":
			return "true or false";
		default:
			return targetType;
		}
	}
}