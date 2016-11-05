package org.nectarframework.base.service.pathfinder;

public class FormResolution extends PathFinderResolution {
	protected String name;
	protected String description;
	protected FormvarResolution[] formvars;

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public FormvarResolution[] getFormvars() {
		return formvars;
	}
}
