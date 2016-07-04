package org.nectarframework.base.exception;

public class ConfigurationException extends NectarException {
	private static final long serialVersionUID = 4401992341883767509L;

	public ConfigurationException() {
		super();
	}

	public ConfigurationException(String msg) {
		super(msg);
	}

	public ConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}
}
