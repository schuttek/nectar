package org.nectarframework.base.service.cluster;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceUnavailableException;

/**
 * This Service provides read access to files stored in the cluster. 
 * @author skander
 *
 */

public class ClusterStorageService extends Service {

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean init() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean run() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean shutdown() {
		// TODO Auto-generated method stub
		return true;
	}

}
