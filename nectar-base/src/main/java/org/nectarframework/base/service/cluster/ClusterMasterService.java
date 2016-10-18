package org.nectarframework.base.service.cluster;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceUnavailableException;

public class ClusterMasterService extends Service {

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean init() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean run() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean shutdown() {
		// TODO Auto-generated method stub
		return false;
	}

}
