package org.nectarframework.base.service.http;

import org.nectarframework.base.exception.NectarException;

public class InternalErrorException extends NectarException {
	private static final long serialVersionUID = -3911331336494880191L;

	public InternalErrorException() {
		super();
	}

	public InternalErrorException(String msg) {
		super(msg);
	}

	public InternalErrorException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public InternalErrorException(Throwable cause) {
		super(cause);
	}

}
