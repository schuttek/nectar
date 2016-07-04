package org.nectarframework.base.service.xml;

public class BlockingResponseHandler implements XmlResponseHandler {

	private XmlResponse response = null;
	private long timeout = 0;
	
	public BlockingResponseHandler(long timeout) {
		this.timeout = timeout;
	}
	
	public synchronized XmlResponse waitForResponse() {
		if (response == null) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {
			}
		}
		return response;
	}
	
	public void handleResponse(XmlResponse xmlResponse) {
		synchronized(this) {
			response = xmlResponse;
			notify();
		}
	}
}
