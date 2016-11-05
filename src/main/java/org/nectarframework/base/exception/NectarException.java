package org.nectarframework.base.exception;

public class NectarException extends Exception {
	private static final long serialVersionUID = -1306827215695115371L;

	public NectarException() {
		super();
	}

	public NectarException(String msg) {
		super(msg);
	}

	public NectarException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public NectarException(Throwable cause) {
		super(cause);
	}
}
