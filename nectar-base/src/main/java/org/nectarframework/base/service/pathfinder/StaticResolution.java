package org.nectarframework.base.service.pathfinder;

public class StaticResolution extends UriResolution {

	protected StaticResolution() {
		super(Type.Static);
		// TODO Auto-generated constructor stub
	}
	protected String path;
	protected String toPath;
	

	public String dumpConfig() {
		return path + " -> " + toPath;
	}
}
