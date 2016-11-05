package org.nectarframework.base.service.pathfinder;

import java.util.HashMap;

public class VirtualHostResolution extends PathFinderResolution {
	protected String hostname;
	protected int port;

	protected HashMap<String, ProjectResolution> namespaceToProjectMap = new HashMap<String, ProjectResolution>();

	public VirtualHostResolution() {
	}

	public void put(String namespace, ProjectResolution pr) {
		this.namespaceToProjectMap.put(namespace, pr);
	}

	public ProjectResolution resolveNamespace(String namespace) {
		return namespaceToProjectMap.get(namespace);
	}

	public String dumpConfig() {
		String s = "";
		for (String k : namespaceToProjectMap.keySet()) {
			s += k + " -> " + namespaceToProjectMap.get(k).dumpConfig();
		}
		return s;
	}

}
