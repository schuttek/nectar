package org.nectarframework.base.service.datastore;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.file.FileService;


/**
 * A DataStoreService that saves
 * @author skander
 *
 */
public class LocalDataStoreService extends DataStoreService {

	private FileService fs;

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
		this.fs = (FileService)dependancy(FileService.class);
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
	public List<? extends DataStoreObject> loadAll(DataStoreObjectDescriptor dsod) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends DataStoreObject> loadRange(DataStoreObjectDescriptor dsod, Object startKey, Object endKey) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends DataStoreObject> loadBulkDSO(DataStoreObjectDescriptor dsod, LinkedList<Object> keys) throws Exception {
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
