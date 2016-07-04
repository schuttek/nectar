package org.nectarframework.base.service.websocket;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;

public class WebSocketServer extends org.java_websocket.server.WebSocketServer {
	private WebSocketRequestService wsrs = null;

	public WebSocketServer(WebSocketRequestService webSocketRequestService) throws UnknownHostException {
		super(webSocketRequestService.getListeningAddress(), Runtime.getRuntime().availableProcessors(), null);
		wsrs = webSocketRequestService;
	}

	/**
	 * Called after an opening handshake has been performed and the given
	 * websocket is ready to be written on.
	 */
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		wsrs.handleOnOpen(conn, handshake);
	}

	/**
	 * Called after the websocket connection has been closed.
	 * 
	 * @param code
	 *            The codes can be looked up here: {@link CloseFrame}
	 * @param reason
	 *            Additional information string
	 * @param remote
	 *            Returns whether or not the closing of the connection was
	 *            initiated by the remote host.
	 **/
	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		wsrs.handleOnClose(conn, code, reason, remote);
	}

	/**
	 * Called when errors occurs. If an error causes the websocket connection to
	 * fail {@link #onClose(WebSocket, int, String, boolean)} will be called
	 * additionally.<br>
	 * This method will be called primarily because of IO or protocol errors.<br>
	 * If the given exception is an RuntimeException that probably means that
	 * you encountered a bug.<br>
	 * 
	 * @param conn
	 *            Can be null if there error does not belong to one specific
	 *            websocket. For example if the servers port could not be bound.
	 **/
	@Override
	public void onError(WebSocket conn, Exception ex) {
		wsrs.handleOnError(conn, ex);
	}

	/**
	 * Callback for string messages received from the remote host
	 * 
	 * @see #onMessage(WebSocket, ByteBuffer)
	 **/
	@Override
	public void onMessage(WebSocket conn, String message) {
		wsrs.handleOnMessage(conn, message);
	}
}
