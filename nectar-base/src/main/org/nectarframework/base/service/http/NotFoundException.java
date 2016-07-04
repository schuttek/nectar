package org.nectarframework.base.service.http;

public class NotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8273747284847929162L;

	public NotFoundException(Exception e) {
		super(e);
	}

	public NotFoundException() {
		super();
	}

}
