package org.nectarframework.base.service.pathfinder;

import java.util.HashMap;

public class ProjectResolution extends PathFinderResolution {

	protected String name;
	protected String namespace;

	protected HashMap<String, UriResolution> urForPathMap = new HashMap<>();

	public ProjectResolution() {
		super();
	}

	public UriResolution getUriResolution(String path) {
		return urForPathMap.get(path);
	}

	public void addPath(String path, UriResolution ar) {
		urForPathMap.put(path, ar);
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public String dumpConfig() {
		String s = "";
		for (String k : urForPathMap.keySet()) {
			s += "\t" + k + " " + urForPathMap.get(k).getType().toString() + " " + urForPathMap.get(k).dumpConfig()
					+ "\n";
		}
		return s;
	}
}
