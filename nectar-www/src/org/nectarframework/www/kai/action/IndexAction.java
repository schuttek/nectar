package org.nectarframework.www.kai.action;

import org.nectarframework.base.service.xml.Element;
import org.nectarframework.www.action.BaseAction;

public class IndexAction extends BaseAction {

	@Override
	public Element _execute() {
		Element elm = new Element("index");

		return elm;
	}

}
