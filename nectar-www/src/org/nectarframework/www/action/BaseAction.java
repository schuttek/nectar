package org.nectarframework.www.action;

import org.nectarframework.base.action.Action;
import org.nectarframework.base.service.xml.Element;

public abstract class BaseAction extends Action {

	public Element execute() {
		return _execute();
	}

	public abstract Element _execute();

}
