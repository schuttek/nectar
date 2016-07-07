package org.nectarframework.www.action;

import org.nectarframework.base.service.xml.Element;

public class IndexAction extends BaseAction {

	@Override
	public Element _execute() {

		Element elm = new Element("index");
		return elm;
	}

}
