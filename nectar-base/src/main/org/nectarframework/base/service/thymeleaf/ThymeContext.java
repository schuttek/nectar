package org.nectarframework.base.service.thymeleaf;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import org.nectarframework.base.service.xml.Element;
import org.thymeleaf.context.IContext;

public class ThymeContext implements IContext {
	private Locale locale;
	private HashMap<String, Object> varMap;
	

	public ThymeContext(Locale locale, Element elm) {
		this.locale = locale;
		this.varMap = buildVarMapRec(elm);
	}
	
	
	
    public Set<String> getVariableNames() {
    	return varMap.keySet();
    }
    
    public Object getVariable(final String name) {
    	return varMap.get(name);
    }

	public Locale getLocale() {
		return locale;
	}

	private HashMap<String, Object> buildVarMapRec(Element e) {
		HashMap<String, Object> m = new HashMap<String, Object>();
		for (String k : e.getAttributes().keySet()) {
			m.put(k, e.get(k));
		}
		for (Element kid : e.getChildren()) {
			m.put(kid.getName(), buildVarMapRec(kid));
		}
		return m;
	}



	public boolean containsVariable(String key) {
		return varMap.containsKey(key);
	}
}
