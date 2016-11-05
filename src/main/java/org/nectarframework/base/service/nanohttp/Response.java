package org.nectarframework.base.service.nanohttp;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.tools.IoTools;

/**
 * HTTP response. Return one of these from serve().
 */
public class Response implements Closeable {

	/**
	 * HTTP status code after processing, e.g. "200 OK", Status.OK
	 */
	private Status status;

	/**
	 * MIME type of content, e.g. "text/html"
	 */
	private String mimeType;

	/**
	 * Data of the response, may be null.
	 */
	private InputStream data;

	private long contentLength;

	/**
	 * Headers for the HTTP response. Use addHeader() to add lines. the
	 * lowercase map is automatically kept up to date.
	 */
	@SuppressWarnings("serial")
	private final Map<String, String> header = new HashMap<String, String>() {

		public String put(String key, String value) {
			lowerCaseHeader.put(key == null ? key : key.toLowerCase(), value);
			return super.put(key, value);
		};
	};

	/**
	 * copy of the header map with all the keys lowercase for faster searching.
	 */
	private final Map<String, String> lowerCaseHeader = new HashMap<String, String>();

	/**
	 * The request method that spawned this response.
	 */
	private Method requestMethod;

	/**
	 * Use chunkedTransfer
	 */
	private boolean chunkedTransfer;

	private boolean encodeAsGzip;

	private boolean keepAlive;

	private CloseableHttpResponse proxyResponse = null;

	/**
	 * Creates a fixed length response if totalBytes>=0, otherwise chunked.
	 */
	protected Response(Status status, String mimeType, InputStream data, long totalBytes) {
		this.status = status;
		this.mimeType = mimeType;
		if (data == null) {
			this.data = new ByteArrayInputStream(new byte[0]);
			this.contentLength = 0L;
		} else {
			this.data = data;
			this.contentLength = totalBytes;
		}
		this.chunkedTransfer = this.contentLength < 0;
		keepAlive = true;
	}

	@Override
	public void close() throws IOException {
		if (this.data != null) {
			this.data.close();
		}
	}

	/**
	 * Adds given line to the header.
	 */
	public void addHeader(String name, String value) {
		this.header.put(name, value);
	}

	/**
	 * Indicate to close the connection after the Response has been sent.
	 * 
	 * @param close
	 *            {@code true} to hint connection closing, {@code false} to let
	 *            connection be closed by client.
	 */
	public void closeConnection(boolean close) {
		if (close)
			this.header.put("connection", "close");
		else
			this.header.remove("connection");
	}

	/**
	 * @return {@code true} if connection is to be closed after this Response
	 *         has been sent.
	 */
	public boolean isCloseConnection() {
		return "close".equals(getHeader("connection"));
	}

	public InputStream getData() {
		return this.data;
	}

	public String getHeader(String name) {
		return this.lowerCaseHeader.get(name.toLowerCase());
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public Method getRequestMethod() {
		return this.requestMethod;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setGzipEncoding(boolean encodeAsGzip) {
		this.encodeAsGzip = encodeAsGzip;
	}

	public void setKeepAlive(boolean useKeepAlive) {
		this.keepAlive = useKeepAlive;
	}

	/**
	 * Sends given response to the socket.
	 */
	protected void send(OutputStream outputStream) {
		SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		try {
			if (this.status == null) {
				throw new Error("sendResponse(): Status can't be null.");
			}
			PrintWriter pw = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(outputStream, new ContentType(this.mimeType).getEncoding())), false);
			pw.append("HTTP/1.1 ").append(this.status.getDescription()).append(" \r\n");
			if (this.mimeType != null) {
				printHeader(pw, "Content-Type", this.mimeType);
			}
			if (getHeader("date") == null) {
				printHeader(pw, "Date", gmtFrmt.format(new Date()));
			}
			for (Entry<String, String> entry : this.header.entrySet()) {
				printHeader(pw, entry.getKey(), entry.getValue());
			}
			if (getHeader("connection") == null) {
				printHeader(pw, "Connection", (this.keepAlive ? "keep-alive" : "close"));
			}
			if (getHeader("content-length") != null) {
				encodeAsGzip = false;
			}
			if (encodeAsGzip) {
				printHeader(pw, "Content-Encoding", "gzip");
				setChunkedTransfer(true);
			}
			long pending = this.data != null ? this.contentLength : 0;
			if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
				printHeader(pw, "Transfer-Encoding", "chunked");
			} else if (!encodeAsGzip) {
				pending = sendContentLengthHeaderIfNotAlreadyPresent(pw, pending);
			}
			pw.append("\r\n");
			pw.flush();
			sendBodyWithCorrectTransferAndEncoding(outputStream, pending);
			outputStream.flush();
			IoTools.safeClose(this.data);
			if (this.proxyResponse != null) {
				this.proxyResponse.close();
				this.proxyResponse = null;
			}
		} catch (IOException ioe) {
			Log.fatal("Could not send response to the client", ioe);
		}
	}

	@SuppressWarnings("static-method")
	protected void printHeader(PrintWriter pw, String key, String value) {
		pw.append(key).append(": ").append(value).append("\r\n");
	}

	protected long sendContentLengthHeaderIfNotAlreadyPresent(PrintWriter pw, long defaultSize) {
		String contentLengthString = getHeader("content-length");
		long size = defaultSize;
		if (contentLengthString != null) {
			try {
				size = Long.parseLong(contentLengthString);
			} catch (NumberFormatException ex) {
				Log.fatal("content-length was no number " + contentLengthString);
			}
		}
		pw.print("Content-Length: " + size + "\r\n");
		return size;
	}

	private void sendBodyWithCorrectTransferAndEncoding(OutputStream outputStream, long pending) throws IOException {
		if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
			ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(outputStream);
			sendBodyWithCorrectEncoding(chunkedOutputStream, -1);
			chunkedOutputStream.finish();
		} else {
			sendBodyWithCorrectEncoding(outputStream, pending);
		}
	}

	private void sendBodyWithCorrectEncoding(OutputStream outputStream, long pending) throws IOException {
		if (encodeAsGzip) {
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
			sendBody(gzipOutputStream, -1);
			gzipOutputStream.finish();
		} else {
			sendBody(outputStream, pending);
		}
	}

	/**
	 * Sends the body to the specified OutputStream. The pending parameter
	 * limits the maximum amounts of bytes sent unless it is -1, in which case
	 * everything is sent.
	 * 
	 * @param outputStream
	 *            the OutputStream to send data to
	 * @param pending
	 *            -1 to send everything, otherwise sets a max limit to the
	 *            number of bytes sent
	 * @throws IOException
	 *             if something goes wrong while sending the data.
	 */
	private void sendBody(OutputStream outputStream, long pending) throws IOException {
		long BUFFER_SIZE = 16 * 1024;
		byte[] buff = new byte[(int) BUFFER_SIZE];
		boolean sendEverything = pending == -1;
		while (pending > 0 || sendEverything) {
			long bytesToRead = sendEverything ? BUFFER_SIZE : Math.min(pending, BUFFER_SIZE);
			int read = this.data.read(buff, 0, (int) bytesToRead);
			if (read <= 0) {
				break;
			}
			outputStream.write(buff, 0, read);
			if (!sendEverything) {
				pending -= read;
			}
		}
	}

	public void setChunkedTransfer(boolean chunkedTransfer) {
		this.chunkedTransfer = chunkedTransfer;
	}

	public void setData(InputStream data) {
		this.data = data;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setRequestMethod(Method requestMethod) {
		this.requestMethod = requestMethod;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setProxyResponse(CloseableHttpResponse proxyResponse) {
		this.proxyResponse = proxyResponse;
	}
}