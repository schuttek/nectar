package org.nectarframework.base.service.websocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.directory.DirectoryService;
import org.nectarframework.base.service.event.EventService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.xml.XmlService;

public class WebSocketRequestService extends Service {

	private int listeningPort;
	private InetAddress listeningHost;
	private int compressionMinSize;

	private InetSocketAddress address;
	private WebSocketServer webSocketServer;
	private HashMap<WebSocket, WebSocketClient> clientMap;
	
	private EventService eventService;
	private ThreadService threadService;
	private DirectoryService directoryService;
	private XmlService xmlService;



	@Override
	public void checkParameters() throws ConfigurationException {
		compressionMinSize = this.serviceParameters.getInt("compressionMinSize", 0, Integer.MAX_VALUE, 5000);
		
		this.listeningPort = serviceParameters.getInt("listeningPort", 1, Short.MAX_VALUE, 8001);
		this.listeningHost = null;
		String listeningHostStr = this.serviceParameters.getValue("listeningHost");

		if (this.listeningHost == null) {
			try {
				this.listeningHost = InetAddress.getByName(listeningHostStr);
			} catch (UnknownHostException e) {
				Log.warn("SimpleHttpService: Listening host could not be resoloved, reverting to local host.", e);
			}
		}
		if (this.listeningHost == null) {
			try {
				this.listeningHost = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				throw new ConfigurationException("SimpleHttpService: Couldn't resolve local host!", e);
			}
		}
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		eventService = (EventService)dependancy(EventService.class);
		threadService = (ThreadService)dependancy(ThreadService.class);
		directoryService = (DirectoryService)dependancy(DirectoryService.class);
		xmlService = (XmlService)dependancy(XmlService.class);
		
		return true;
	}

	@Override
	protected boolean init() {
		address = new InetSocketAddress(listeningHost, listeningPort);
		clientMap = new HashMap<WebSocket, WebSocketClient>();
		return true;
	}

	@Override
	protected boolean run() {
		try {
			webSocketServer = new WebSocketServer(this);
			webSocketServer.start();
		} catch (UnknownHostException e) {
			Log.fatal("WebSocketRequestService.run() failed.", e);
			return false;
		}
		Log.trace("WebSocketRequestService listening on " + address.toString());
		return true;
	}

	@Override
	protected boolean shutdown() {
		try {
			webSocketServer.stop();
		} catch (IOException e) {
			Log.fatal("WebSocketRequestService.shutdown failed", e);
		} catch (InterruptedException e) {
			Log.fatal("WebSocketRequestService.shutdown failed", e);
		}
		return true;
	}

	public int getCompressionMinSize() {
		return compressionMinSize;
	}

	public void setCompressionMinSize(int compressionMinSize) {
		this.compressionMinSize = compressionMinSize;
	}

	public InetSocketAddress getListeningAddress() {
		return address;
	}

	public HashMap<WebSocket, WebSocketClient> getConnections() {
		return clientMap;
	}

	public void handleOnClose(WebSocket conn, int code, String reason, boolean remote) {
		Log.trace("WebSocketRequestService.handleOnClose() " + conn.toString() + " " + code + " " + reason + " " + (remote ? "true" : "false"));
		
		WebSocketClient wsc = this.clientMap.get(conn);
		if (wsc != null) {
			eventService.unregisterListener(wsc); 
		}
	}

	public void handleOnError(WebSocket conn, Exception ex) {
		if (ex instanceof IOException && ex.getMessage().compareTo("An existing connection was forcibly closed by the remote host") == 0) {
			Log.trace("WebSocketRequestService / " + conn.toString() + ": Client forced disconnect."); 
		}
		Log.info("WebSocketRequestService.handleOnError() "+conn.toString(), ex);
	}

	public void handleOnOpen(WebSocket conn, ClientHandshake handshake) {
		Log.trace("WebSocketRequestService.handleOnOpen() " + conn.toString() + " " + handshake.toString());
	}

	public void handleOnMessage(WebSocket conn, String message) {
		//Log.trace("WebSocketRequestService.handleOnMessage() " + conn.toString() + " " + message);
		
		// 1 JSON to WebSocketRawRequest
		WebSocketRequest wsr = null;
/*		try {
			wsr = mapper.readValue(message, WebSocketRequest.class);
		} catch (JsonParseException e) {
			Log.fatal("WebSocketRequestService.handleOnMessage()", e);
			return;
		} catch (JsonMappingException e) {
			Log.fatal("WebSocketRequestService.handleOnMessage()", e);
			return;
		} catch (IOException e) {
			Log.fatal("WebSocketRequestService.handleOnMessage()", e);
			return;
		}*/
		
		// 3 give it to the RequestService superclass
		WebSocketRequestHandler rh = new WebSocketRequestHandler(wsr, this, directoryService, xmlService);
		
		threadService.execute(rh);
	}

}
