package org.nectarframework.base.service.pathfinder;

import org.nectarframework.base.form.Form;

public class FormvarResolution extends PathFinderResolution {

	protected String name;
	protected Form.VarType type;
	protected boolean nullAllowed;

	public String getName() {
		return name;
	}

	public Form.VarType getType() {
		return type;
	}

	public boolean isNullAllowed() {
		return nullAllowed;
	}
}
