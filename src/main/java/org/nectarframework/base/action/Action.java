package org.nectarframework.base.action;

import org.nectarframework.base.element.Element;
import org.nectarframework.base.form.Form;

/**
 * The absolute base class of any class in Nectar that actually does any real
 * work.
 * 
 * Everything that could possibly be requested directly from the Nectar
 * webservice should be implemented as an Action, and nothing else.
 * 
 */
public abstract class Action {
	protected Form form;
	
	public final void init(Form form) {
		this.form = form;
	}
	
	public abstract Element execute();
	
}
