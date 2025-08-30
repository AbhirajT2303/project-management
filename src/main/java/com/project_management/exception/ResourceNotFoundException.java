package com.project_management.exception;

public class ResourceNotFoundException extends BaseException {
	public ResourceNotFoundException(String message) {
		super(message, "RESOURCE_NOT_FOUND");
	}

	public ResourceNotFoundException(String resourceName, String field, Object value) {
		super(String.format("%s not found with %s: %s", resourceName, field, value), "RESOURCE_NOT_FOUND");
	}
}
