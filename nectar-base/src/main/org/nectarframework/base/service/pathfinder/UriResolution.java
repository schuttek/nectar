package org.nectarframework.base.service.pathfinder;


public abstract class UriResolution extends PathFinderResolution {

	private Type type = null;
	
	protected UriResolution(Type type) {
		this.type = type;
	}
	
	public final Type getType() {
		return type;
	}
	
	public enum Type {
	    Action,
	    Alias,
	    Proxy,
		Redirect,
	    Static;
	}

	public abstract String dumpConfig();
}
