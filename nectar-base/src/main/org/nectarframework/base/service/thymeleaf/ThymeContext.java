package org.nectarframework.base.service.thymeleaf;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.nectarframework.base.service.xml.Element;
import org.thymeleaf.context.IContext;

public class ThymeContext implements IContext {
	private Locale locale;
	
	private Element actionElement;
	private Element sessionElement;
	private Element globalElement;
	static final HashSet<String> varNames = new HashSet<String>();

	static {
		varNames.add("action");
		varNames.add("session");
		varNames.add("global");
	}
	
	public ThymeContext(Locale locale, Element actionElement, Element sessionElement, Element globalElement) {
		this.locale = locale;
		this.actionElement = actionElement;
		this.sessionElement = sessionElement;
		this.globalElement = globalElement;
	}
	
	
	
    public Set<String> getVariableNames() {
    	return varNames;
    }
    
    public Object getVariable(final String name) {
    	if (name.equals("action")) {
    		return this.actionElement;
    	} else if (name.equals("session")) {
    		return this.sessionElement;
    	} else if (name.equals("global")) {
    		return this.globalElement;
    	}
    	
    	return null;
    }

	public Locale getLocale() {
		return locale;
	}

	public boolean containsVariable(String key) {
		return varNames.contains(key);
	}
}
