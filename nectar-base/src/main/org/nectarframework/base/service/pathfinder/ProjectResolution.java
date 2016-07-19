package org.nectarframework.base.service.pathfinder;

import java.util.HashMap;

public class ProjectResolution extends PathFinderResolution {

	protected String name;
	protected String namespace;

	public HashMap<String, UriResolution> urForPathMap = new HashMap<>();

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
}
