package org.nectarframework.base.service.websocket;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.nectarframework.base.action.Action;
import org.nectarframework.base.form.Form;
import org.nectarframework.base.service.directory.DirAction;
import org.nectarframework.base.service.directory.DirectoryService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadServiceTask;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;

public class WebSocketRequestHandler extends ThreadServiceTask {

	protected WebSocketRequestService rs;
	protected DirectoryService ds;
	protected XmlService xs;
	protected WebSocketRequest request = null;
	private ByteArrayOutputStream outputBuffer = null;

	public WebSocketRequestHandler(WebSocketRequest r, WebSocketRequestService rs, DirectoryService ds, XmlService xs) {
		this.rs = rs;
		this.ds = ds;
		this.xs = xs;
		this.request = r;
	}

	@Override
	public void execute() throws Exception {
		WebSocket webSocket = request.getWebSocket();
		InetSocketAddress webSocketLocalAddress = webSocket.getLocalSocketAddress();

//		webSocketLocalAddress.getHostName();
//		webSocketLocalAddress.getPort();

		// Log.trace("WebServiceRequestHandler.execute() for " + request.getPath() + " on " + request.getHost() + ":" + request.getPort() + " reqID:" + request.getWsrr().getRequestId());

		// TODO: session handling.

		long execStart = System.nanoTime();
		ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

		String path = request.getPath();

		int prefixIdx = path.lastIndexOf('/');
		String prefix = "";
		String actionPath = "";
		if (prefixIdx > 0) {
			prefix = path.substring(0, prefixIdx);
			actionPath = path.substring(prefixIdx + 1, path.length());
		} else {
			actionPath = path;
		}
		
		DirAction dirAction = ds.lookupAction(prefix, actionPath);

		if (dirAction == null) { // not found
			Log.warn("no dirAction for dirPath " + prefix + "/"+ request.getPath());
			handleNotFound();
			return;
		}
		// instantiate the action
		Action action = (Action) ClassLoader.getSystemClassLoader().loadClass(dirAction.className).newInstance();
		
		// set up the form.
		Form form = new Form(dirAction.form, request.getParameters());
		
		action._init(form);

		// execute the action
		Element elm = action.execute();
		outputBuffer.write(xs.xmlHeader().getBytes());
//		xs.transformToOS(elm, form.getXslPath(), outputBuffer);
		outputBuffer.write(XmlService.toXmlBytes(elm));

		finishOutputStream();
		long execEnd = System.nanoTime();

//		Log.accessLog(request, directoryPath, form, this.outputBuffer.toByteArray(), (execEnd - execStart) / 1000);

		
		Log.accessLog(request.getPath(), form.getElement(), form.getElement(), elm,  (execEnd - execStart) / 1000, webSocketLocalAddress.getHostName(), form.getSession());

		Log.trace("Request processed: input "+ form.toString() + " --- output " + this.outputBuffer.toString());

	}

	private void finishOutputStream() throws IOException {

		/*
		 * Ideally, we'd like to send binary headers, and even send the data in
		 * compressed form However, this makes interpreting the response on the
		 * client an absolute nightmare, since reading bytes and gzip are very
		 * difficult to access in XULRunner.
		 * 
		 * byte[] oldBuffer = this.outputBuffer.toByteArray(); this.outputBuffer
		 * = new ByteArrayOutputStream();
		 * 
		 * int requestId = request.getWsrr().getRequestId();
		 * 
		 * // header // 4 bytes for request id // 1 byte for flags.
		 * ObjectOutputStream oob = new ObjectOutputStream(this.outputBuffer);
		 * oob.writeInt(requestId); byte flags = 0x00; if
		 * (this.outputBuffer.size() > rs.getCompressionMinSize()) { flags |=
		 * 0x01; } oob.writeByte(flags); oob.flush(); oob.close();
		 * 
		 * if (oldBuffer.length > rs.getCompressionMinSize()) { GZIPOutputStream
		 * gzipOS = new GZIPOutputStream(this.outputBuffer);
		 * gzipOS.write(oldBuffer); gzipOS.flush(); gzipOS.close(); } else {
		 * this.outputBuffer.write(oldBuffer); }
		 */

		this.request.getWebSocket().send(this.outputBuffer.toByteArray());
	}

	private void handleNotFound() throws IOException {
		Log.warn("404... :( ");

		Element error = new Element("ncc_ws_error");
		error.add("code", "404");
		error.add("message", "Request not Found");
		
		error.add("ncc_ws_request_id", Integer.toString(request.getRequestId()));

		xs.outputNDOJson(error, this.outputBuffer);

		finishOutputStream();
	}
}
