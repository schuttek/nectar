package org.nectarframework.base.service.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import org.nectarframework.base.action.Action;
import org.nectarframework.base.element.Element;
import org.nectarframework.base.form.Form;
import org.nectarframework.base.form.ValidationError;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.directory.DirAction;
import org.nectarframework.base.service.directory.DirPath;
import org.nectarframework.base.service.directory.DirRedirect;
import org.nectarframework.base.service.directory.DirectoryService;
import org.nectarframework.base.service.file.FileInfo;
import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.file.ReadFileAccessDeniedException;
import org.nectarframework.base.service.file.ReadFileNotAFileException;
import org.nectarframework.base.service.file.ReadFileNotFoundException;
import org.nectarframework.base.service.log.AccessLogService;
import org.nectarframework.base.service.template.TemplateService;
import org.nectarframework.base.service.template.thymeleaf.ThymeleafService;
import org.nectarframework.base.service.thread.ThreadServiceTask;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.ByteArrayOutputStream;
import org.nectarframework.base.tools.FastByteArrayOutputStream;
import org.nectarframework.base.tools.Stopwatch;
import org.nectarframework.base.tools.StringTools;
import org.simpleframework.http.Address;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.thymeleaf.exceptions.TemplateEngineException;

/**
 * This Service process HTTP requests by the Simple Framework
 * (http://www.simpleframework.org/), resolves request paths to static files or
 * executes Actions and transforms the resulting Element object.
 * 
 * 
 * @author skander
 *
 */
public class SimpleHttpRequestHandler extends ThreadServiceTask {

	protected static final String serverName = "Nectar/" + org.nectarframework.base.Main.VERSION;;
	protected SimpleHttpRequestService simpleHttpRequestService;
	protected DirectoryService directoryService;
	protected ThymeleafService thymeleafService;
	protected TemplateService templateService;
	protected FileService fileService;
	protected Request request;
	protected Response response;
	protected XmlService xmlService;
	protected AccessLogService accessLogService;

	protected String contentType;
	protected long contentLength;
	// protected ByteArrayOutputStream outputBuffer;

	public SimpleHttpRequestHandler(Request httpRequest, Response httpResponse,
			SimpleHttpRequestService simpleHttpRequestService, DirectoryService directoryService, XmlService xmlService,
			FileService fileService, ThymeleafService thymeleafService, TemplateService templateService, AccessLogService accessLogService) {
		this.simpleHttpRequestService = simpleHttpRequestService;
		this.directoryService = directoryService;
		this.request = httpRequest;
		this.response = httpResponse;
		this.xmlService = xmlService;
		this.fileService = fileService;
		this.thymeleafService = thymeleafService;
		this.templateService = templateService;
		this.accessLogService = accessLogService;

		// outputBuffer = new ByteArrayOutputStream();
	}

	@Override
	public void execute() {

		Address address = request.getAddress();

		Log.trace(
				"request for " + address.getPath().getPath() + " on " + address.getDomain() + ":" + address.getPort());

		// resolve the action with the directory service
		String path = request.getAddress().getPath().getPath();

		
		DirPath dirPath = directoryService.lookupPath(path);
		if (dirPath == null) {
			handleNotFound(null);
		}
		while (dirPath != null && dirPath instanceof DirRedirect) {
			
		}
			
		
		// request level errors don't go past this point!
		try {
			if (path.startsWith("/s/")) {
				try {
					processStatic();
				} catch (NotFoundException e) {
					handleNotFound(e);
				} catch (AccessDeniedException e) {
					handleAccessDenied(e);
				}
			} else {
				try {
					processDynamic();
				} catch (NotFoundException e) {
					handleNotFound(e);
				} catch (InternalErrorException e) {
					handleInternalError(e);
				} catch (FormValidationException e) {
					handleFormValidationError(e);
				} catch (ClientWriteException e) {
					handleClientWriteException(e);
				}
			}
		} catch (Throwable t) {
			handleInternalError(t);
		}

		try {
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException e) {
			handleClientWriteException(e);
		}
	}

	private void handleNotFound(NotFoundException e) {
		Log.info("SimpleHttpRequestService: Not Found: " + request.getAddress().getPath().getPath(), e);

		StringBuffer sb = new StringBuffer();
		String path = request.getAddress().getPath().getPath();
		sb.append("<!DOCTYPE html><html><head><title>404 Not Found</title></head><body><h1>Not Found</h1>");
		sb.append("<p>The requested URL " + path + " was not found on this server.</p>");
		sb.append("<hr><address>" + serverName + " Server at " + request.getAddress().getDomain() + " Port "
				+ request.getAddress().getPort() + "</address>");
		sb.append("</body></html>\n");

		byte[] byteArray = sb.toString().getBytes();

		response.setCode(404);
		response.setContentLength(byteArray.length);
		response.setContentType("text/html");

		try {
			response.getOutputStream().write(byteArray);
		} catch (IOException e1) {
			Log.warn(e1);
		}
	}

	private void handleAccessDenied(AccessDeniedException e) {
		Log.info("SimpleHttpRequestService: Access Denied: " + request.getAddress().getPath().getPath(), e);

		StringBuffer sb = new StringBuffer();
		String path = request.getAddress().getPath().getPath();
		sb.append("<!DOCTYPE html><html><head><title>403 Forbidden</title></head><body><h1>Forbidden</h1>");
		sb.append("<p>You don't have permission to access " + path + " on this server.</p>");
		sb.append("<hr><address>" + serverName + " Server at " + request.getAddress().getDomain() + " Port "
				+ request.getAddress().getPort() + "</address>");
		sb.append("</body></html>\n");

		byte[] byteArray = sb.toString().getBytes();

		response.setCode(403);
		response.setContentLength(byteArray.length);
		response.setContentType("text/html");

		try {
			response.getOutputStream().write(byteArray);
		} catch (IOException e1) {
			Log.warn(e1);
		}
	}

	private void handleInternalError(Throwable e) {
		Log.info("SimpleHttpRequestService: InternalError: " + request.getAddress().getPath().getPath(), e);

		StringBuffer sb = new StringBuffer();
		String path = request.getAddress().getPath().getPath();
		sb.append(
				"<!DOCTYPE html><html><head><title>500 Internal Server Error</title></head><body><h1>Internal Server Error</h1>");
		sb.append("<p>The requested URL " + path + " was generated the following error:</p>");
		sb.append("<pre>" + StringTools.throwableStackTracetoString(e) + "</pre>");
		sb.append("<hr><address>" + serverName + " Server at " + request.getAddress().getDomain() + " Port "
				+ request.getAddress().getPort() + "</address>");
		sb.append("</body></html>\n");

		byte[] byteArray = sb.toString().getBytes();

		response.setCode(500);
		response.setContentLength(byteArray.length);
		response.setContentType("text/html");

		try {
			response.getOutputStream().write(byteArray);
		} catch (IOException e1) {
			Log.warn(e1);
		}

	}

	private void handleFormValidationError(FormValidationException e) {
		Log.info("SimpleHttpRequestService: FormValidationError: " + request.getAddress().getPath().getPath(), e);
		Form form = e.getForm();

		StringBuffer sb = new StringBuffer();
		String path = request.getAddress().getPath().getPath();
		sb.append(
				"<!DOCTYPE html><html><head><title>400 Input Validation Error</title></head><body><h1>Input Validation Error</h1>");
		sb.append("<p>The requested URL " + path + "?" + request.getQuery().toString() + " requires the input form "
				+ form.getName() + " generated the following validation errors:</p>");

		List<ValidationError> vel = form.getValidationErrors();
		for (ValidationError ve : vel) {
			String description = "";
			if (ve.getErrorType() == ValidationError.ErrorType.NULL_NOT_ALLOWED) {
				description = "Required variable is undefined.";
			} else if (ve.getErrorType() == ValidationError.ErrorType.NUMBER_PARSING_ERROR) {
				description = "Number parsing error.";
			}
			sb.append("<p>" + ve.getKey() + ": " + description + "</p>");
		}

		sb.append("<hr><address>" + serverName + " Server at " + request.getAddress().getDomain() + " Port "
				+ request.getAddress().getPort() + "</address>");
		sb.append("</body></html>\n");

		byte[] byteArray = sb.toString().getBytes();

		response.setCode(400);
		response.setContentLength(byteArray.length);
		response.setContentType("text/html");

		try {
			response.getOutputStream().write(byteArray);
		} catch (IOException e1) {
			Log.warn(e1);
		}
	}

	private void handleClientWriteException(Exception e) {
		Log.warn("SimpleHttpRequestService: ClientWriteException: " + request.getAddress().getPath().getPath(), e);
	}

	public void processDynamic() throws NotFoundException, InternalErrorException,
			FormValidationException, ClientWriteException {
		Stopwatch stopwatch = new Stopwatch();

		stopwatch.start();

		HashMap<String, List<String>> parameters = new HashMap<String, List<String>>();

		Query query = request.getQuery();
		for (String s : query.keySet()) {
			parameters.put(s, query.getAll(s));
		}

		String path = request.getAddress().getPath().getPath();

		String actionNamespace = getActionNamespace(path);
		String actionPath = getActionPath(path);

		Log.trace("namespace='" + actionNamespace + "' , action='" + actionPath + "'");
		DirPath dirPath = null;

		stopwatch.mark("init");
		
		while (dirPath == null || !(dirPath instanceof DirAction)) {
			dirPath = directoryService.lookupPath(path);

			if (dirPath == null) {
				throw new NotFoundException();
			}

			if (dirPath instanceof DirRedirect) {
				parameters.putAll(((DirRedirect) dirPath).variables);
				dirPath = directoryService.lookupPath(((DirRedirect) dirPath).toPath);
				if (dirPath == null) {
					throw new NotFoundException();
				}
			}
		}

		DirAction dirAction = (DirAction) dirPath;

		// instantiate the action
		Action action;
		try {
			action = (Action) ClassLoader.getSystemClassLoader()
					.loadClass(dirAction.packageName + "." + dirAction.className).newInstance();
		} catch (InstantiationException e) {
			throw new InternalErrorException(e);
		} catch (IllegalAccessException e) {
			throw new InternalErrorException(e);
		} catch (ClassNotFoundException e) {
			throw new InternalErrorException(e);
		}

		stopwatch.mark("actionLoad");

		// set up the form.
		Form form = new SimpleHttpForm(dirAction.form, parameters, request);

		form.setHttpRequest(request);

		if (!form.isValid()) {
			throw new FormValidationException(form);
		}

		action.init(form);
		// execute the action

		String output = simpleHttpRequestService.getDefaultOutput();
		if (dirAction.defaultOutput != null) {
			output = dirAction.defaultOutput;
		}

		response.setValue("Server", "Nectar/" + org.nectarframework.base.Main.VERSION);

		// cache settings
		long time = System.currentTimeMillis();
		cacheHeaders(time, time, time);

		// Raw output

		Element elm = null;
		stopwatch.mark("actionInit");

		// a raw action is special...
		if (output.equals("raw")) {
			elm = handleRawAction(action);
			stopwatch.stop();
			accessLogService.accessLog(actionPath, form.getElement(), form.getElement(), elm, stopwatch.sinceStart(),
					request.getClientAddress().toString(), form.getSession());
			Log.trace("Request processed");
			return;
		}

		// the action's implementation runs now.
		elm = action.execute();
		stopwatch.mark("actionExec");

		FastByteArrayOutputStream outputByteArrayOutputStream = new FastByteArrayOutputStream();

		// processing the resulting element:
		if (output.equals("xml")) {
			response.setContentType("text/xml");
			try {
				outputByteArrayOutputStream
						.write(("<?xml version=\"1.0\" encoding=\"" + Charset.defaultCharset().toString() + "\"?>\n")
								.getBytes());
				XmlService.toXml(elm, outputByteArrayOutputStream);
			} catch (IOException e) {
				throw new InternalErrorException(e);
			}
		} else if (output.equals("json")) {
			response.setContentType("application/json");
			try {
				XmlService.toNDOJson(elm, outputByteArrayOutputStream);
			} catch (IOException e) {
				throw new InternalErrorException(e);
			}
		} else if (output.equals("template")) {
			response.setContentType("text/html");
			
			Log.trace("ActionElement: "+elm.toString());
			
			try {
				templateService.outputTemplate(outputByteArrayOutputStream, dirAction.templateName, Locale.getDefault(),
						elm, null);
			} catch (Exception e) {
				throw new InternalErrorException(e);
			}
		} else if (output.equals("thymeleaf")) {
			response.setContentType("text/html");
			Locale locale = Locale.getDefault();
			if (form.getSession() != null) {
				locale = form.getSession().getLocale();
			}
			try {
				thymeleafService.output(locale, actionNamespace, dirAction.templateName, elm,
						outputByteArrayOutputStream);
			} catch (TemplateEngineException e) {
				throw new InternalErrorException(e);
			}
		} // TODO add default output?
		stopwatch.mark("output");

		// byteArray = doCompression(byteArray);

		response.setCode(200);
		response.setContentLength(outputByteArrayOutputStream.size());

		try {
			response.getOutputStream().write(outputByteArrayOutputStream.toByteArray());
		} catch (IOException e) {
			throw new ClientWriteException(e);
		}

		stopwatch.stop();

		accessLogService.accessLog(actionPath, form.getElement(), form.getElement(), elm, stopwatch.sinceStart(),
				request.getClientAddress().toString(), form.getSession());

		Log.trace("Request processed: "+stopwatch.toString());
	}

	@SuppressWarnings("unused")
	private byte[] doCompression(byte[] byteArray) throws InternalErrorException {
		// content length
		if (byteArray.length < simpleHttpRequestService.getCompressionMinSize()) {
			if (byteArray.length == 0) {
				Log.trace("Request: " + request.toString() + " produced 0 bytes of output... weird...");
			}
			return byteArray;
		} else {
			int uncompressedSize = byteArray.length;
			long compressStart = System.nanoTime();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try {
				GZIPOutputStream gzipOS = new GZIPOutputStream(baos);
				gzipOS.write(byteArray);
				gzipOS.flush();
				gzipOS.close();
			} catch (IOException e) {
				throw new InternalErrorException(e);
			}

			response.setValue("Content-Encoding", "gzip");

			long compressEnd = System.nanoTime();
			Log.trace("compression: time " + ((compressEnd - compressStart) / 1000) + "us ratio: " + baos.size() + "/"
					+ uncompressedSize + ":" + ((baos.size() * 100) / uncompressedSize));

			return baos.toBytes();
		}
	}

	private String getActionPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1, path.length());
		}

		int prefixIdx = path.lastIndexOf('/');
		String actionPath = "";
		if (prefixIdx > 0) {
			actionPath = path.substring(prefixIdx + 1, path.length());
		} else {
			actionPath = path;
		}
		return actionPath;
	}

	private String getActionNamespace(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1, path.length());
		}
		int prefixIdx = path.lastIndexOf('/');
		String prefix = "";
		if (prefixIdx > 0) {
			prefix = path.substring(0, prefixIdx);
		}
		return prefix;
	}

	private void cacheHeaders(long lastModified, long cacheUntil, long now) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		// cache-control: private, max-age=0, no-cache

		long age = (cacheUntil - now) / 1000;
		if (age < 0) {
			age = 0;
			response.setValue("Pragma", "no-cache");
			response.setValue("Cache-Control", "private, max-age=0, no-cache, no-store");
		} else {
			response.setValue("Cache-Control", "private, max-age=" + age);
		}

		response.setValue("Expires", dateFormat.format(cacheUntil));
		response.setValue("Date", dateFormat.format(lastModified));
	}

	private Element handleRawAction(Action action) throws InternalErrorException {

		if (!(action instanceof RawAction)) {
			throw new InternalErrorException("Only actions that inherit "+RawAction.class.getName()+" may be configured for raw output");
		}
		RawAction rawAction = (RawAction) action;

		Element elm = rawAction.execute();
		contentType = elm.get("httpContentType");
		if (contentType != null) {
			contentType = "application/octet-stream";
		}

		byte[] byteArray = rawAction.getRawByteArray();

		boolean isCompressible = true;
		if (elm.get("httpCompressible").equals("false")) {
			isCompressible = false;
		}

		/*
		 * if (isCompressible && byteArray.length() >
		 * simpleHttpRequestService.getCompressionMinSize()) { byteArray = new
		 * ByteArray(doCompression(byteArray.getBytes())); }
		 */

		contentLength = byteArray.length;

		response.setCode(200);
		response.setContentLength(contentLength);
		response.setContentType(contentType);
		long now = System.currentTimeMillis();
		cacheHeaders(now, now, now);

		return elm;
	}

	private void processStatic() throws NotFoundException, AccessDeniedException {

		// TODO: add cache layer
		Stopwatch stopwatch = new Stopwatch();

		InputStream is = null;
		try {
			FileInfo fileInfo = fileService.getFileInfo(
					"/" + this.simpleHttpRequestService.getStaticLocalDirectory() + this.request.getPath().getPath());
			is = fileService.getFileAsInputStream(
					"/" + this.simpleHttpRequestService.getStaticLocalDirectory() + this.request.getPath().getPath(), this.simpleHttpRequestService.getStaticFileCacheExpiry());

			response.setValue("Server", serverName);
			response.setDate("Date", fileInfo.getLastModified());
			response.setDate("Last-Modified", fileInfo.getLastModified());

			String ext = fileInfo.getFileExtension();
			String contentType = this.simpleHttpRequestService.getMimeTypesByExtension().get(ext);
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			response.setContentType(contentType);
			response.setContentLength((int) fileInfo.getLength());

		} catch (ReadFileNotFoundException e) {
			throw new NotFoundException(e);
		} catch (ReadFileAccessDeniedException e) {
			throw new AccessDeniedException(e);
		} catch (ReadFileNotAFileException e) {
			throw new NotFoundException(e);
		} catch (IOException e) {
			throw new AccessDeniedException(e);
		}

		byte[] buffer = new byte[simpleHttpRequestService.getStaticFileBufferSize()];

		int len = -1;
		try {
			len = is.read(buffer);
		} catch (IOException e) {
			// TODO make more detailed.
			Log.warn("SimpleHttpRequestHandler.processStatic: IOException while reading file", e);

			// TODO access log
			// TODO is this the right way to close the connection?
			try {
				response.close();
			} catch (IOException e1) {
			}
			return;
		}

		while (len != -1) {
			try {
				response.getOutputStream().write(buffer, 0, len);
			} catch (IOException e) {
				// TODO make more detailed
				Log.warn("SimpleHttpRequestHandler.processStatic: IOException while writing to client", e);
			}
			try {
				len = is.read(buffer);
			} catch (IOException e) {
				// TODO make more detailed.
				Log.warn("SimpleHttpRequestHandler.processStatic: IOException while reading file", e);

				// TODO access log
				// TODO is this the right way to close the connection?
				try {
					response.close();
				} catch (IOException e1) {
				}
				return;
			}
		}

		stopwatch.stop();

		accessLogService.accessLog(this.request.getPath().getPath(), null, null, null, stopwatch.sinceStart(),
				request.getClientAddress().toString(), null);

		Log.trace("Request processed");
	}

}
