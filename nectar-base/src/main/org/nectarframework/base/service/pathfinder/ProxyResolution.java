package org.nectarframework.base.service.pathfinder;

public class ProxyResolution extends UriResolution {

	public String getPath() {
		return path;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public String getRequestPath() {
		return requestPath;
	}

	protected ProxyResolution() {
		super(Type.Proxy);
	}

	protected String path;
	protected String protocol;
	protected int port;
	protected String host;
	protected String requestPath;

	public String dumpConfig() {
		return path + " -> " + host + ":" + port + " " + requestPath;
	}

}
