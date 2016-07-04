package org.nectarframework.base.service.listener;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;


public class ListenerService extends Service {

	
	
	@Override
	public void checkParameters() throws ConfigurationException {
		
	}

	@Override
	public boolean establishDependancies() {
		return true;
	}

	@Override
	protected boolean init() {
		return true;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	public void removeListener(Listener wsc) {
		
	}

}
