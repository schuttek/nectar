package org.nectarframework.base.service.nanohttp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadServiceTask;

/**
 * The runnable that will be used for every new client connection.
 */
public class ClientHandler extends ThreadServiceTask {

	private final InputStream inputStream;

	private final Socket acceptSocket;
	
	private NanoHttpService nanoService;

	public ClientHandler(InputStream inputStream, Socket acceptSocket, NanoHttpService nanoService) {
		this.inputStream = inputStream;
		this.acceptSocket = acceptSocket;
		this.nanoService = nanoService;
	}

	public void close() {
		Utils.safeClose(this.inputStream);
		Utils.safeClose(this.acceptSocket);
	}

	@Override
	public void execute() throws Exception {
		OutputStream outputStream = null;
		try {
			outputStream = this.acceptSocket.getOutputStream();
			TempFileManager tempFileManager = new TempFileManager();
			HTTPSession session = new HTTPSession(tempFileManager, this.inputStream, outputStream,
					this.acceptSocket.getInetAddress(), nanoService);
			while (!this.acceptSocket.isClosed()) {
				session.execute();
			}
		} catch (Exception e) {
			// When the socket is closed by the client,
			// we throw our own SocketException
			// to break the "keep alive" loop above. If
			// the exception was anything other
			// than the expected SocketException OR a
			// SocketTimeoutException, print the
			// stacktrace
			if (!(e instanceof SocketException && "NanoHttpd Shutdown".equals(e.getMessage()))
					&& !(e instanceof SocketTimeoutException)) {
				Log.fatal("Communication with the client broken, or an bug in the handler code", e);
			}
		} finally {
			Utils.safeClose(outputStream);
			Utils.safeClose(this.inputStream);
			Utils.safeClose(this.acceptSocket);
		}
	}
}