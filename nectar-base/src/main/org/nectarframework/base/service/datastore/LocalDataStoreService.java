package org.nectarframework.base.service.datastore;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.xml.XmlService;


/**
 * A DataStoreService that saves DataStoreObjects to file.
 * @author skander
 *
 */
public class LocalDataStoreService extends DataStoreService {

	protected FileService fs;
	
	protected String storageRootDir = "dataStore";
	
	protected String tableIndex = "table_index.xml";
	protected String dataTableSuffix = "dat";

	@Override
	protected boolean secondStageinit() {
		
		
		
		return true;
	}

	@Override
	public void checkParameters() throws ConfigurationException {
		storageRootDir = serviceParameters.getString("storageRootDir", "dataStore");
		tableIndex = serviceParameters.getString("tableIndex", "table_index.xml");
		dataTableSuffix = serviceParameters.getString("dataTableSuffix",  "dat");
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		this.fs = (FileService)dependancy(FileService.class);
		dependancy(XmlService.class);
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
		
		synchronized(this) {
			String tableName = dsod.getTableName();
			
			byte[] tableBA = fs.readAllBytes(getTableFilePath(tableName));
			
			Table table = buildTable(tableBA);
			
			return table.get(key);
			
		}
		
		return null;
	}

	private Table buildTable(byte[] tableBA) {
		// TODO Auto-generated method stub
		return null;
	}

	protected String getTableFilePath(String tableName) {
		return this.storageRootDir + "/" + tableName + "." + this.dataTableSuffix;
	}

	@Override
	public void save(Collection<DataStoreObject> dsoList) throws Exception {
		for (DataStoreObject dso : dsoList) {
			save(dso);
		}
	}
	
	@Override 
	public void save(DataStoreObject dso) throws Exception {
		synchronized(this) {
			// if key exists, this is an update
			
		}
		dso.
	}

}
