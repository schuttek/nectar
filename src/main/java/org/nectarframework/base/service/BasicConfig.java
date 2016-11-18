package org.nectarframework.base.service;

import java.io.File;

public class BasicConfig {

	public BasicConfig(File configFile, String nodeName, String nodeGroup) {
		super();
		this.configFile = configFile;
		this.nodeName = nodeName;
		this.nodeGroup = nodeGroup;
	}

	private final File configFile;
	private final String nodeName;
	private final String nodeGroup;

	public File getConfigFile() {
		return configFile;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getNodeGroup() {
		return nodeGroup;
	}

}
