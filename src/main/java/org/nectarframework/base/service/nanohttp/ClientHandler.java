package org.nectarframework.base.service.nanohttp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.thread.ThreadServiceTask;
import org.nectarframework.base.tools.IoTools;

/**
 * The runnable that will be used for every new client connection.
 */
public class ClientHandler extends ThreadServiceTask {

	private final InputStream inputStream;

	private final Socket acceptSocket;
	
	private NanoHttpService nanoService;

	private FileService fileService;

	public ClientHandler(InputStream inputStream, Socket acceptSocket, NanoHttpService nanoService, FileService fileService) {
		this.inputStream = inputStream;
		this.acceptSocket = acceptSocket;
		this.nanoService = nanoService;
		this.fileService = fileService;
	}

	public void close() {
		IoTools.safeClose(this.inputStream);
		IoTools.safeClose(this.acceptSocket);
	}

	@Override
	public void execute() throws Exception {
		OutputStream outputStream = null;
		try {
			outputStream = this.acceptSocket.getOutputStream();
			HTTPSession session = new HTTPSession(fileService, this.inputStream, outputStream,
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
			IoTools.safeClose(outputStream);
			IoTools.safeClose(this.inputStream);
			IoTools.safeClose(this.acceptSocket);
		}
	}
}