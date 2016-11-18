package org.nectarframework.base.form;

import java.util.Optional;

public class ValidationError {

	public enum ErrorType {
		NULL_NOT_ALLOWED, NUMBER_PARSING_ERROR;
	}

	private final String key;
	private final ErrorType type;
	private final String message;

	public ValidationError(String key, ErrorType type) {
		this.key = key;
		this.type = type;
		this.message = null;
	}

	public ValidationError(String key, ErrorType type, String message) {
		this.key = key;
		this.type = type;
		this.message = message;
	}

	public String getKey() {
		return key;
	}
	
	public ErrorType getErrorType() {
		return type;
	}

	public Optional<String> getMessage() {
		return Optional.ofNullable(message);
	}
}
