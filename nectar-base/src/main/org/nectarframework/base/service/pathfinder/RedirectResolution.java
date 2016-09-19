package org.nectarframework.base.service.pathfinder;

public class RedirectResolution extends UriResolution {

	protected RedirectResolution() {
		super(Type.Redirect);
		// TODO Auto-generated constructor stub
	}

	protected String path;
	protected int code;
	protected String toUrl;

	public String getPath() {
		return path;
	}

	public int getCode() {
		return code;
	}

	public String getToUrl() {
		return toUrl;
	}

	public String dumpConfig() {
		return path + " -> " + code + ":" + toUrl;
	}
}
