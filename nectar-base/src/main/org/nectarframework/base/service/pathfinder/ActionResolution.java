package org.nectarframework.base.service.pathfinder;

import org.nectarframework.base.action.Action;

public class ActionResolution extends UriResolution {

	protected ActionResolution() {
		super(Type.Action);
		// TODO Auto-generated constructor stub
	}

	public enum OutputType {
		raw, xml, json, template, xsl, thymeleaf;

		static OutputType lookup(String method) {
			if (method == null)
				return null;

			return valueOf(method);
		}
	}

	protected String name;
	protected String packageName;
	protected String className;
	protected String formName;
	protected String description;
	protected FormResolution form;

	protected OutputType defaultOutput;
	protected String templateName;

	protected Class<? extends Action> actionClass;

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	public String getFormName() {
		return formName;
	}

	public String getDescription() {
		return description;
	}

	public FormResolution getForm() {
		return form;
	}

	public OutputType getDefaultOutput() {
		return defaultOutput;
	}

	public String getTemplateName() {
		return templateName;
	}

	public Class<? extends Action> getActionClass() {
		return actionClass;
	}

	public String dumpConfig() {
		return className +" "+defaultOutput+" "+formName+" "+templateName;
	}
}
