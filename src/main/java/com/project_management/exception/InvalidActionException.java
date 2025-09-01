package com.project_management.exception;

public class InvalidActionException extends BaseException {
	public InvalidActionException(String message) {
		super(message, "INVALID_ACTION");
	}

	public InvalidActionException(String message, Throwable cause) {
		super(message, "INVALID_ACTION", cause);
	}
}
