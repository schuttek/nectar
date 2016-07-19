package org.nectarframework.base.service.pathfinder;

public class ProxyResolution extends UriResolution {

	protected ProxyResolution() {
		super(Type.Proxy);
		// TODO Auto-generated constructor stub
	}


	protected String path;
	protected String protocol;
	protected int port;
	protected String host;
	protected String requestPath;
}
