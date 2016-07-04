package org.nectarframework.base.service.http;

import org.nectarframework.base.form.Form;

public class FormValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6400887984034013813L;
	Form form;
	
	public FormValidationException(Form form) {
		super();
		this.form = form;
	}

	public Form getForm() {
		return form;
	}

}
