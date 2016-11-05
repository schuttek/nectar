package org.nectarframework.base.service.pathfinder;

import java.util.HashMap;
import java.util.List;

import org.nectarframework.base.tools.StringTools;

public class AliasResolution extends UriResolution {

	protected AliasResolution() {
		super(Type.Alias);
	}

	protected String path;
	protected String toPath;
	protected boolean relative;
	protected HashMap<String, List<String>> variables;

	public String getPath() {
		return path;
	}

	public String getToPath() {
		return toPath;
	}
	
	public boolean isRelative() {
		return relative;
	}

	public HashMap<String, List<String>> getVariables() {
		return variables;
	}
	
	public String dumpConfig() {
		return path +" -> "+toPath+" "+(relative?"relative":"direct"+" ")+StringTools.mapToString(variables);
	}
}
