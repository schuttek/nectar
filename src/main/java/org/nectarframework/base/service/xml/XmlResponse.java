package org.nectarframework.base.service.xml;

import org.nectarframework.base.element.Element;
import org.nectarframework.base.service.thread.ThreadServiceTask;

public class XmlResponse extends ThreadServiceTask {
	final private XmlResponseHandler xmlResponseHandler;
	final private Element request;
	final private String id;
	final private Connection connection;
	private Element response = null;

	public XmlResponse(XmlResponseHandler xmlResponseHandler, Element request, String id, Connection connection) {
		this.xmlResponseHandler = xmlResponseHandler;
		this.request = request;
		this.id = id;
		this.connection = connection;
	}

	public XmlResponseHandler getXmlResponseHandler() {
		return xmlResponseHandler;
	}

	public Element getRequest() {
		return request;
	}

	public String getId() {
		return id;
	}

	public Connection getConnection() {
		return connection;
	}

	public Element getResponse() {
		return response;
	}

	public void setResponse(Element response) {
		this.response = response;
	}

	@Override
	public void execute() throws Exception {
		xmlResponseHandler.handleResponse(this);
	}

}
