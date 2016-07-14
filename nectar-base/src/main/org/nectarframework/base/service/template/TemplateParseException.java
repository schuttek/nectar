package org.nectarframework.base.service.template;

import org.nectarframework.base.exception.NectarException;

public class TemplateParseException extends NectarException {

	public TemplateParseException() {
		super();
	}

	public TemplateParseException(String msg) {
		super(msg);
	}

	public TemplateParseException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public TemplateParseException(Throwable cause) {
		super(cause);
	}
	private static final long serialVersionUID = -1649594084833436997L;
}
