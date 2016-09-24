package org.nectarframework.base.service.nanohttp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadService;

/**
 * The runnable that will be used for the main listening thread.
 */
public class ServerRunnable implements Runnable {

    private final int timeout;

    private IOException bindException = null;

    private boolean binded = false;

	private ServerSocket serverSocket;

	private String hostname;

	private int port;

	private ThreadService threadService;
	private NanoHttpService nanoService;

	private FileService fileService;

    public ServerRunnable(int timeout, ServerSocket serverSocket, String hostname, int port, ThreadService threadService, NanoHttpService nanoService, FileService fileService) {
        this.timeout = timeout;
        this.serverSocket = serverSocket;
        this.hostname = hostname;
        this.port = port;
        this.threadService = threadService;
        this.nanoService = nanoService;
        this.fileService = fileService;
    }
    
    public boolean isBinded() {
    	return binded;
    }
    
    public IOException getBindException() {
    	return this.bindException;
    }

    @Override
    public void run() {
        try {
        	serverSocket.bind(hostname != null ? new InetSocketAddress(hostname, port) : new InetSocketAddress(port));
        	binded = true;
        } catch (IOException e) {
            this.bindException = e;
            return;
        }
        do {
            try {
                final Socket finalAccept = serverSocket.accept();
                if (this.timeout > 0) {
                    finalAccept.setSoTimeout(this.timeout);
                }
                final InputStream inputStream = finalAccept.getInputStream();
                Log.trace("[NanoHttpService/ServerRunnable] new connection: "+finalAccept.toString());
                threadService.execute(new ClientHandler(inputStream, finalAccept, nanoService, fileService));
            } catch (IOException e) {
                Log.info("Communication with the client broken", e);
            }
        } while (!serverSocket.isClosed());
    }
}