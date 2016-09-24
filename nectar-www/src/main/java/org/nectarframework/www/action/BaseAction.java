package org.nectarframework.www.action;

import org.nectarframework.base.action.Action;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.datastore.DataStoreService;
import org.nectarframework.base.service.xml.Element;

public abstract class BaseAction extends Action {
	private DataStoreService dss;

	public Element execute() {
		return _execute();
	}

	public abstract Element _execute();


	protected DataStoreService dss() {
		if (dss == null) {
			dss = (DataStoreService) ServiceRegister.getService(DataStoreService.class);
		}
		return dss;
	}

}
