package org.nectarframework.base.service.http;

import java.util.HashMap;
import java.util.List;

import org.nectarframework.base.form.Form;
import org.nectarframework.base.service.directory.DirForm;
import org.simpleframework.http.Request;

public class SimpleHttpForm extends Form {

	private Request request;

	public SimpleHttpForm(DirForm dirForm, HashMap<String, List<String>> parameters, Request request) {
		super(dirForm, parameters);
		this.request = request;
	}
	
	public Request getHttpRequest() {
		return request;
	}
}
