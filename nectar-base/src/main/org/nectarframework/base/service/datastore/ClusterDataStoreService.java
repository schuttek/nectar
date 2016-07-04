package org.nectarframework.base.service.datastore;

import java.util.Collection;
import java.util.LinkedList;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.cluster.ClusterDatabaseService;


/**
 * A DataStoreService adaptor for nectar.base.service.cluster.ClusterDataBaseService
 * @author skander
 *
 */
public class ClusterDataStoreService extends DataStoreService {

	private ClusterDatabaseService cdbs;

	@Override
	protected boolean _init() {
		
		return true;
	}

	@Override
	public void checkParameters() throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		this.cdbs = (ClusterDatabaseService)dependancy(ClusterDatabaseService.class);
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

	

	@Override
	public Collection<DataStoreObject> loadAll(DataStoreObjectDescriptor dsod) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DataStoreObject> loadRange(DataStoreObjectDescriptor dsod, DataStoreKey startKey, DataStoreKey endKey) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DataStoreObject> loadBulkDSO(DataStoreObjectDescriptor dsod, LinkedList<Object> keys) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataStoreObject loadDSO(DataStoreObjectDescriptor dsod, Object key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(Collection<DataStoreObject> dsoList) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
