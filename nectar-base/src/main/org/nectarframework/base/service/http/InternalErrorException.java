package org.nectarframework.base.service.http;

public class InternalErrorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4530996086039639069L;

	public InternalErrorException(Exception e) {
		super(e);
	}

}
