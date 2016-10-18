package org.nectarframework.base.service.nanohttp;

/*
 * #%L
 * NanoHttpd-Core
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.nectarframework.base.action.Action;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.form.Form;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.file.FileInfo;
import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.file.ReadFileAccessDeniedException;
import org.nectarframework.base.service.file.ReadFileNotAFileException;
import org.nectarframework.base.service.file.ReadFileNotFoundException;
import org.nectarframework.base.service.http.RawAction;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.pathfinder.ActionResolution;
import org.nectarframework.base.service.pathfinder.ActionResolution.OutputType;
import org.nectarframework.base.service.template.TemplateService;
import org.nectarframework.base.service.template.thymeleaf.ThymeleafService;
import org.nectarframework.base.service.pathfinder.AliasResolution;
import org.nectarframework.base.service.pathfinder.PathFinderService;
import org.nectarframework.base.service.pathfinder.ProxyResolution;
import org.nectarframework.base.service.pathfinder.RedirectResolution;
import org.nectarframework.base.service.pathfinder.StaticResolution;
import org.nectarframework.base.service.pathfinder.UriResolution;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.FastByteArrayOutputStream;
import org.nectarframework.base.tools.IoTools;
import org.nectarframework.base.tools.StringTools;

/**
 * A simple, tiny, nicely embeddable HTTP server in Java
 * <p/>
 * <p/>
 * NanoHTTPD
 * <p>
 * Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen,
 * 2010 by Konstantinos Togias
 * </p>
 * <p/>
 * <p/>
 * <b>Features + limitations: </b>
 * <ul>
 * <p/>
 * <li>Only one Java file</li>
 * <li>Java 5 compatible</li>
 * <li>Released as open source, Modified BSD licence</li>
 * <li>No fixed config files, logging, authorization etc. (Implement yourself if
 * you need them.)</li>
 * <li>Supports parameter parsing of GET and POST methods (+ rudimentary PUT
 * support in 1.25)</li>
 * <li>Supports both dynamic content and file serving</li>
 * <li>Supports file upload (since version 1.2, 2010)</li>
 * <li>Supports partial content (streaming)</li>
 * <li>Supports ETags</li>
 * <li>Never caches anything</li>
 * <li>Doesn't limit bandwidth, request time or simultaneous connections</li>
 * <li>Default code serves files and shows all HTTP parameters and headers</li>
 * <li>File server supports directory listing, index.html and index.htm</li>
 * <li>File server supports partial content (streaming)</li>
 * <li>File server supports ETags</li>
 * <li>File server does the 301 redirection trick for directories without '/'
 * </li>
 * <li>File server supports simple skipping for files (continue download)</li>
 * <li>File server serves also very long files without memory overhead</li>
 * <li>Contains a built-in list of most common MIME types</li>
 * <li>All header names are converted to lower case so they don't vary between
 * browsers/clients</li>
 * <p/>
 * </ul>
 * <p/>
 * <p/>
 * <b>How to use: </b>
 * <ul>
 * <p/>
 * <li>Subclass and implement serve() and embed to your own program</li>
 * <p/>
 * </ul>
 * <p/>
 * See the separate "LICENSE.md" file for the distribution license (Modified BSD
 * licence)
 */
public class NanoHttpService extends Service {

	/**
	 * Maximum time to wait on Socket.getInputStream().read() (in milliseconds)
	 * This is required as the Keep-Alive HTTP connections would otherwise block
	 * the socket reading thread forever (or as long the browser is open).
	 */
	private int socketReadTimeout = 15000;

	private String listeningHost = "127.0.0.1";

	private int listeningSSLPort = 443;

	private int sslSocketBacklog = 20;

	private int listeningPort = 80;

	private int socketBacklog = 20;

	private PathFinderService pathFinderService;

	private Thread myThread;

	private String keyStoreFilePath;

	private ThreadService threadService;

	private ServerSocket sslServerSocket;

	private ServerSocket simpleServerSocket;

	private OutputType defaultOutput = OutputType.template;

	private XmlService xmlService;

	private FileService fileService;

	private TemplateService templateService;

	private String staticFileLocalDirectory;

	private ThymeleafService thymeleafService;

	private long staticFileCacheExpiry;

	/**
	 * Common MIME type for dynamic content: plain text
	 */
	public static final String MIME_PLAINTEXT = "text/plain";

	/**
	 * Common MIME type for dynamic content: html
	 */
	public static final String MIME_HTML = "text/html";

	/**
	 * Pseudo-Parameter to use to store the actual query string in the
	 * parameters map for later re-processing.
	 */
	static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		socketReadTimeout = sp.getInt("socketReadTimeout", 0, Integer.MAX_VALUE, 15000);
		listeningHost = sp.getString("listeningHost", null);
		listeningPort = sp.getInt("listeningPort", 1, Short.MAX_VALUE, 80);
		listeningSSLPort = sp.getInt("listeningSSLPort", 1, Short.MAX_VALUE, 443);
		keyStoreFilePath = sp.getString("keyStoreFilePath", "config/keystore.jks");

		staticFileLocalDirectory = sp.getString("staticFileLocalDirectory", "public_root");

		staticFileCacheExpiry = sp.getInt("staticFileCacheExpiry", -1, Integer.MAX_VALUE,
				24 * 60 * 60 * 1000); // 24 hours
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		threadService = (ThreadService) dependency(ThreadService.class);
		pathFinderService = (PathFinderService) dependency(PathFinderService.class);
		xmlService = (XmlService) this.dependency(XmlService.class);
		fileService = (FileService) this.dependency(FileService.class);
		templateService = (TemplateService) this.dependency(TemplateService.class);
		return true;
	}

	@Override
	protected boolean init() {
		thymeleafService = (ThymeleafService) ServiceRegister.getService(ThymeleafService.class);
		return true;
	}

	@Override
	protected boolean run() {
		Log.trace(pathFinderService.dumpConfig());
		try {

			try {
				this.sslServerSocket = makeSSLServerSocket(keyStoreFilePath, ("suckre").toCharArray());
			} catch (IOException e1) {
				Log.warn("NanoHTTP couldn't start SSL because of missing keyfile.", e1);
				this.sslServerSocket = null;
			}
			this.simpleServerSocket = new ServerSocket();

			this.simpleServerSocket.setReuseAddress(true);

			ServerRunnable serverRunnable = new ServerRunnable(socketReadTimeout, simpleServerSocket,
					this.listeningHost, this.listeningPort, threadService, this, fileService);
			this.myThread = new Thread(serverRunnable);
			this.myThread.setDaemon(true);
			this.myThread.setName("NanoHttpService Main Listener");
			this.myThread.start();
			while (!serverRunnable.isBinded() && serverRunnable.getBindException() == null) {
				try {
					Thread.sleep(10L);
				} catch (Throwable t) {
					// on android this may not be allowed, that's why we
					// catch throwable the wait should be very short because we
					// are
					// just waiting for the bind of the socket
				}
			}
			if (serverRunnable.getBindException() != null) {
				throw serverRunnable.getBindException();
			}
		} catch (IOException e) {
			Log.fatal("NanoHttpService failed to start:", e);
			return false;
		}

		return true;
	}

	@Override
	protected boolean shutdown() {
		try {
			IoTools.safeClose(this.simpleServerSocket);
			IoTools.safeClose(this.sslServerSocket);
			if (this.myThread != null) {
				this.myThread.join();
			}
		} catch (Exception e) {
			Log.warn("Could not stop all connections", e);
			return false;
		}
		return true;
	}

	/**
	 * Creates an SSLSocketFactory for HTTPS. Pass a KeyStore resource with your
	 * certificate and passphrase
	 */
	public ServerSocket makeSSLServerSocket(String keyAndTrustStoreClasspathPath, char[] passphrase)
			throws IOException {
		try {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			InputStream keystoreStream = new FileInputStream(new File(keyAndTrustStoreClasspathPath));

			keystore.load(keystoreStream, passphrase);
			KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keystore, passphrase);

			SSLServerSocketFactory res = null;
			try {
				TrustManagerFactory trustManagerFactory = TrustManagerFactory
						.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustManagerFactory.init(keystore);
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
				res = ctx.getServerSocketFactory();

			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}

			SSLServerSocket ss = null;
			ss = (SSLServerSocket) res.createServerSocket();
			ss.setEnabledProtocols(ss.getSupportedProtocols());
			ss.setUseClientMode(false);
			ss.setWantClientAuth(false);
			ss.setNeedClientAuth(false);

			return ss;

		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * create a instance of the client handler, subclasses can return a subclass
	 * of the ClientHandler.
	 * 
	 * @param finalAccept
	 *            the socket the cleint is connected to
	 * @param inputStream
	 *            the input stream
	 * @return the client handler
	 */
	protected ClientHandler createClientHandler(final Socket finalAccept, final InputStream inputStream) {
		return new ClientHandler(inputStream, finalAccept, this, fileService);
	}

	/**
	 * Create a response with unknown length (using HTTP 1.1 chunking).
	 */
	public static Response newChunkedResponse(Status status, String mimeType, InputStream data) {
		return new Response(status, mimeType, data, -1);
	}

	/**
	 * Create a response with known length.
	 */
	public static Response newFixedLengthResponse(Status status, String mimeType, InputStream data, long totalBytes) {
		return new Response(status, mimeType, data, totalBytes);
	}

	/**
	 * Create a text response with known length.
	 */
	public static Response newFixedLengthResponse(Status status, String mimeType, String txt) {
		ContentType contentType = new ContentType(mimeType);
		if (txt == null) {
			return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(new byte[0]), 0);
		} else {
			byte[] bytes;
			try {
				CharsetEncoder newEncoder = Charset.forName(contentType.getEncoding()).newEncoder();
				if (!newEncoder.canEncode(txt)) {
					contentType = contentType.tryUTF8();
				}
				bytes = txt.getBytes(contentType.getEncoding());
			} catch (UnsupportedEncodingException e) {
				Log.warn("encoding problem, responding nothing", e);
				bytes = new byte[0];
			}
			return newFixedLengthResponse(status, contentType.getContentTypeHeader(), new ByteArrayInputStream(bytes),
					bytes.length);
		}
	}

	/**
	 * Create a text response with known length.
	 */
	public static Response newFixedLengthResponse(String msg) {
		return newFixedLengthResponse(Status.OK, NanoHttpService.MIME_HTML, msg);
	}

	/**
	 * Override this to customize the server.
	 * <p/>
	 * <p/>
	 * (By default, this returns a 404 "Not Found" plain text error response.)
	 * 
	 * @param session
	 *            The HTTP session
	 * @return HTTP response, see class Response for details
	 */
	public Response serve(HTTPSession session) {
		Map<String, String> files = new HashMap<String, String>();
		Method method = session.getMethod();
		if (Method.PUT.equals(method) || Method.POST.equals(method)) {
			try {
				session.parseBody(files);
			} catch (IOException ioe) {
				return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
						"SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			} catch (ResponseException re) {
				return newFixedLengthResponse(re.getStatus(), NanoHttpService.MIME_PLAINTEXT, re.getMessage());
			}
		}

		Map<String, List<String>> parms = session.getParameters();
		return serve(session.getUri(), method, session.getHeaders(), parms, session.getQueryParameterString(), files);
	}

	/**
	 * Override this to customize the server.
	 * <p/>
	 * <p/>
	 * (By default, this returns a 404 "Not Found" plain text error response.)
	 * 
	 * @param uri
	 *            Percent-decoded URI without parameters, for example
	 *            "/index.cgi"
	 * @param method
	 *            "GET", "POST" etc.
	 * @param parms
	 *            Parsed, percent decoded parameters from URI and, in case of
	 *            POST, data.
	 * @param headers
	 *            Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	public Response serve(String uri, Method method, Map<String, String> headers, Map<String, List<String>> parms,
			String queryParameterString, Map<String, String> files) {

		Log.trace("[NanoHttpService:serve] uri=" + uri + " method=" + method.toString() + " headers="
				+ StringTools.mapToString(headers) + " parms=" + StringTools.mapToString(parms) + " query="
				+ queryParameterString + " files=" + StringTools.mapToString(files));

		String hostHeader = headers.get("host");
		if (hostHeader == null) {
			// https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.23
			// Host is an obligatory header
			return newFixedLengthResponse(Status.BAD_REQUEST, NanoHttpService.MIME_PLAINTEXT, "400 Bad Request");
		}

		if (!hostHeader.contains(":")) {
			hostHeader += ":80";
		}

		UriResolution uriResolution = pathFinderService.resolveUri(hostHeader, uri);

		if (uriResolution == null) {
			Log.warn("[NanoHttpService]serve: " + hostHeader + " " + uri + " NOT FOUND.");
			return newFixedLengthResponse(Status.NOT_FOUND, NanoHttpService.MIME_PLAINTEXT, "Not Found");
		}

		switch (uriResolution.getType()) {
		case Action:
			return serveAction((ActionResolution) uriResolution, uri, method, headers, parms, queryParameterString,
					files);
		case Alias:
			return serveAlias((AliasResolution) uriResolution, uri, method, headers, parms, queryParameterString,
					files);
		case Proxy:
			return serveProxy((ProxyResolution) uriResolution, uri, method, headers, parms, queryParameterString,
					files);
		case Redirect:
			return serveRedirect((RedirectResolution) uriResolution, uri, method, headers, parms, queryParameterString,
					files);
		case Static:
			return serveStatic((StaticResolution) uriResolution, uri, method, headers, parms, queryParameterString,
					files);
		}

		return newFixedLengthResponse(Status.NOT_FOUND, NanoHttpService.MIME_PLAINTEXT, "Not Found");
	}

	private Response serveAction(ActionResolution actionResolution, String uri, Method method,
			Map<String, String> headers, Map<String, List<String>> parms, String queryParameterString,
			Map<String, String> files) {

		Action action = null;
		try {
			action = actionResolution.getActionClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			Log.fatal(e);
			return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
					"SERVER INTERNAL ERROR: Exception: " + e.getMessage());
		}

		Form form = new Form(actionResolution.getForm(), parms);

		if (!form.isValid()) {
			Log.fatal("FormValidationException: ");// TODO add details
			return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
					"SERVER INTERNAL ERROR: FormValidationException");
		}

		action._init(form);
		// execute the action

		OutputType output = this.defaultOutput;
		if (actionResolution.getDefaultOutput() != null) {
			output = actionResolution.getDefaultOutput();
		}

		// Raw output

		Element elm = null;

		// the action's implementation runs now.
		elm = action.execute();

		FastByteArrayOutputStream outputByteArrayOutputStream = null;

		Locale locale = Locale.getDefault();
		if (form.getSession() != null) {
			locale = form.getSession().getLocale();
		}

		String mimeType = null;
		byte[] byteBuff = null;
		// processing the resulting element:
		switch (output) {
		case raw:
			mimeType = elm.get("httpContentType");
			byteBuff = ((RawAction) action).getRawByteArray();
			return newFixedLengthResponse(Status.OK, mimeType, new ByteArrayInputStream(byteBuff), byteBuff.length);
		case json:
			mimeType = "application/json";
			outputByteArrayOutputStream = new FastByteArrayOutputStream();
			try {
				XmlService.toNDOJson(elm, outputByteArrayOutputStream);
			} catch (IOException e) {
				return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
						"SERVER INTERNAL ERROR: IOException" + e.getMessage());
			}
			byteBuff = outputByteArrayOutputStream.toByteArray();
			return newFixedLengthResponse(Status.OK, mimeType, new ByteArrayInputStream(byteBuff), byteBuff.length);
		case template:
			mimeType = "text/html";
			outputByteArrayOutputStream = new FastByteArrayOutputStream();
			try {
				templateService.outputTemplate(outputByteArrayOutputStream, actionResolution.getTemplateName(), locale,
						elm, null);
			} catch (IOException e) {
				return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
						"SERVER INTERNAL ERROR: IOException" + e.getMessage());
			}
			byteBuff = outputByteArrayOutputStream.toByteArray();
			return newFixedLengthResponse(Status.OK, mimeType, new ByteArrayInputStream(byteBuff), byteBuff.length);
		case thymeleaf:
			if (thymeleafService == null) {
				return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
						"SERVER INTERNAL ERROR: Thymeleaf Service not available.");
			} else {
				mimeType = "text/html";
				outputByteArrayOutputStream = new FastByteArrayOutputStream();
				thymeleafService.output(locale, "", actionResolution.getTemplateName(), elm,
						outputByteArrayOutputStream);
				byteBuff = outputByteArrayOutputStream.toByteArray();
				return newFixedLengthResponse(Status.OK, mimeType, new ByteArrayInputStream(byteBuff), byteBuff.length);
			}
		case xml:
			mimeType = "text/xml";
			outputByteArrayOutputStream = new FastByteArrayOutputStream();
			try {
				outputByteArrayOutputStream
						.write(("<?xml version=\"1.0\" encoding=\"" + Charset.defaultCharset().toString() + "\"?>\n")
								.getBytes());
				XmlService.toXml(elm, outputByteArrayOutputStream);
			} catch (IOException e) {
				return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
						"SERVER INTERNAL ERROR: IOException" + e.getMessage());
			}
			byteBuff = outputByteArrayOutputStream.toByteArray();
			return newFixedLengthResponse(Status.OK, mimeType, new ByteArrayInputStream(byteBuff), byteBuff.length);
		case xsl:
			mimeType = "text/xsl";
			return newFixedLengthResponse(Status.SERVICE_UNAVAILABLE, NanoHttpService.MIME_PLAINTEXT,
					"Not Implemented: ");
		default:
			break;
		}
		return newFixedLengthResponse(Status.SERVICE_UNAVAILABLE, NanoHttpService.MIME_PLAINTEXT, "Not Implemented: ");
	}

	private Response serveAlias(AliasResolution aliasResolution, String uri, Method method, Map<String, String> headers,
			Map<String, List<String>> parms, String queryParameterString, Map<String, String> files) {

		for (String key : aliasResolution.getVariables().keySet()) {
			if (!parms.containsKey(key)) {
				parms.put(key, new LinkedList<String>());
			}
			parms.get(key).addAll(aliasResolution.getVariables().get(key));
		}

		if (aliasResolution.isRelative()) {
			uri = uri.substring(0, uri.indexOf('/')) + aliasResolution.getToPath();
		} else {
			uri = aliasResolution.getToPath();
		}
		Log.trace("[NanoHttpService]serveAlias: " + aliasResolution.getPath() + " to " + aliasResolution.getToPath());
		return serve(uri, method, headers, parms, queryParameterString, files);
	}

	private String getStaticLocalDirectory() {
		return staticFileLocalDirectory;
	}

	private Response serveStatic(StaticResolution uriResolution, String uri, Method method, Map<String, String> headers,
			Map<String, List<String>> parms, String queryParameterString, Map<String, String> files) {

		// TODO: hand off to FileService
		// remember to filter ../
		try {

			InputStream is = null;
			FileInfo fileInfo = fileService.getFileInfo("/" + getStaticLocalDirectory() + uri);
			is = fileService.getFileAsInputStream("/" + getStaticLocalDirectory() + uri, this.staticFileCacheExpiry);

			String contentType = IoTools.getMimeTypeForFile(uri);
			if (contentType == null) {
				contentType = "application/octet-stream";
			}

			// Log.accessLog(this.request.getPath().getPath(), null, null, null,
			// stopwatch.sinceStart(),
			// request.getClientAddress().toString(), null);

			Log.trace(" Static Request processed");

			return newFixedLengthResponse(Status.OK, contentType, is, fileInfo.getLength());
		} catch (ReadFileNotFoundException e) {
			Log.warn(e);
			return newFixedLengthResponse(Status.NOT_FOUND, NanoHttpService.MIME_PLAINTEXT, "File Not Found.");
		} catch (ReadFileAccessDeniedException e) {
			Log.warn(e);
			return newFixedLengthResponse(Status.FORBIDDEN, NanoHttpService.MIME_PLAINTEXT, "Access Denied.");
		} catch (ReadFileNotAFileException e) {
			Log.warn(e);
			return newFixedLengthResponse(Status.NOT_FOUND, NanoHttpService.MIME_PLAINTEXT, "Not a File.");
		} catch (IOException e) {
			Log.warn(e);
			return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
					"IOException." + e.getMessage());
		}

	}

	private Response serveProxy(ProxyResolution proxyResolution, String uri, Method method, Map<String, String> headers,
			Map<String, List<String>> parms, String queryParameterString, Map<String, String> files) {

		String remoteTarget = uri.substring(proxyResolution.getPath().length() + 1);
		if (!remoteTarget.startsWith("/")) {
			remoteTarget = "/" + remoteTarget;
		}

		CloseableHttpClient httpclient = HttpClients.createDefault();
		URIBuilder remoteUri = new URIBuilder();
		remoteUri.setScheme("http");
		remoteUri.setHost(proxyResolution.getHost());
		remoteUri.setPort(proxyResolution.getPort());
		remoteUri.setPath(proxyResolution.getRequestPath() + remoteTarget);
		remoteUri.setCharset(Charset.defaultCharset());
		for (String k : parms.keySet()) {
			remoteUri.addParameter(k, parms.get(k).get(0));
		}

		HttpGet httpget;
		try {
			httpget = new HttpGet(remoteUri.build());
		} catch (URISyntaxException e) {
			Log.warn(e);
			return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
					"SERVER INTERNAL ERROR: URISyntaxException" + e.getMessage());
		}

		CloseableHttpResponse response = null;
		Response resp = null;
		try {
			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream is = entity.getContent();
				long isl = entity.getContentLength();
				resp = new Response(Status.lookup(response.getStatusLine().getStatusCode()), null, is, isl);
			} else {
				resp = new Response(Status.lookup(response.getStatusLine().getStatusCode()), null, null, 0);
			}

			Header[] remoteHeaders = response.getAllHeaders();
			for (Header h : remoteHeaders) {
				resp.addHeader(h.getName(), h.getValue());
			}

			resp.setProxyResponse(response);

		} catch (IOException e) {
			Log.warn(e);
			return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHttpService.MIME_PLAINTEXT,
					"SERVER INTERNAL ERROR: IOException" + e.getMessage());
		}

		return resp;
	}

	private Response serveRedirect(RedirectResolution redirectResolution, String uri, Method method,
			Map<String, String> headers, Map<String, List<String>> parms, String queryParameterString,
			Map<String, String> files) {

		Response response = new Response(Status.REDIRECT, null, null, 0);
		response.addHeader("Location", redirectResolution.getToUrl());
		return response;
	}

}