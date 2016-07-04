package org.nectarframework.base.form;

public class ValidationError {

	public enum ErrorType {
		NULL_NOT_ALLOWED, NUMBER_PARSING_ERROR;
	}

	private String key;
	private ErrorType type;
	private String message;

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

	public String getMessage() {
		return message;
	}
}
