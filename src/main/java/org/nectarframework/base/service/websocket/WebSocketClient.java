package org.nectarframework.base.service.websocket;

import org.java_websocket.WebSocket;
import org.nectarframework.base.service.session.Session;

public class WebSocketClient {
	private WebSocket webSocket;
	private Session session;
	
	public WebSocketClient(WebSocket ws) {
		this.webSocket = ws;
	}

	public WebSocket getWebSocket() {
		return webSocket;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}
}
