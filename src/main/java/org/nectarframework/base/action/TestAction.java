package org.nectarframework.base.action;


import java.text.DateFormat;
import java.util.Date;

import org.nectarframework.base.element.Element;

public class TestAction extends Action {
	
	@Override
	public Element execute() {
		Element elm = new Element("index");
		
		elm.add("date", DateFormat.getDateTimeInstance().format(new Date()));
		
		elm.add("xmlentities", "&'<>\"");
		
		return elm;
	}
}
