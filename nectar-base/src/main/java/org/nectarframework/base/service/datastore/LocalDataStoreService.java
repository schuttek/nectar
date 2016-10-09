package org.nectarframework.base.service.datastore;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.datastore.DataStoreObjectDescriptorColumn.Type;
import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.file.ReadFileNotFoundException;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.thread.ThreadServiceTask;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.ByteArray;

/**
 * A DataStoreService that saves DataStoreObjects to file.
 * 
 * @author skander
 *
 */
public class LocalDataStoreService extends DataStoreService {

	protected FileService fileService;
	protected String storageRootDir = "dataStore";

	protected String tableIndex = "table_index.xml";
	protected String dataTableSuffix = "dat";
	protected int cacheExpiry = 3600000; // one hour
	private int compactionDelay;
	private float compactionThresholdFactor = 0.05f; 

	private ConcurrentHashMap<DataStoreObjectDescriptor, ConcurrentHashMap<Object, DataStoreObject>> tableMap;

	private HashMap<DataStoreObjectDescriptor, Integer> internalTableIds;

	private AtomicIntegerArray tableReplaces;

	private ThreadService threadService;
	private static int headerLength = 5000;

	
	@Override
	protected boolean secondStageinit() {
		Collection<DataStoreObjectDescriptor> dsodList = getAllDataStoreObjectDescriptors();
		tableMap = new ConcurrentHashMap<>();
		int t = 0;
		tableReplaces = new AtomicIntegerArray(dsodList.size());
		
		for (DataStoreObjectDescriptor dsod : dsodList) {
			internalTableIds.put(dsod, t++);
			tableMap.put(dsod, buildTable(dsod));
		}
		
		return true;
	}

	@Override
	public void checkParameters() throws ConfigurationException {
		storageRootDir = serviceParameters.getString("storageRootDir", "dataStore");
		tableIndex = serviceParameters.getString("tableIndex", "table_index.xml");
		dataTableSuffix = serviceParameters.getString("dataTableSuffix", "dat");
		cacheExpiry = serviceParameters.getInt("cacheExpiry", 0, Integer.MAX_VALUE, 3600000);
		compactionDelay = serviceParameters.getInt("compactionDelay", 1, Integer.MAX_VALUE, 30*60*1000); // 30 minutes
		compactionThresholdFactor = serviceParameters.getFloat("compactionThresholdFactor", Float.MIN_VALUE, Float.MAX_VALUE, 0.05f); // 5 percent
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		this.fileService = (FileService) dependancy(FileService.class);
		threadService = (ThreadService) dependancy(ThreadService.class);
		dependancy(XmlService.class);
		return true;
	}

	@Override
	protected boolean run() {
		createCompactDatabasesTask();
		return true;
	}


	@Override
	protected boolean shutdown() {
		return true;
	}

	@Override
	public List<? extends DataStoreObject> loadAll(DataStoreObjectDescriptor dsod) throws Exception {
		ConcurrentHashMap<Object, DataStoreObject> table = tableMap.get(dsod);
		LinkedList<DataStoreObject> list = new LinkedList<>();

		for (DataStoreObject dso : table.values()) {
			list.add(dso.copy());
		}

		return list;
	}

	@Override
	public List<? extends DataStoreObject> loadRange(DataStoreObjectDescriptor dsod, Object startKey, Object endKey)
			throws Exception {
		ConcurrentHashMap<Object, DataStoreObject> table = tableMap.get(dsod);
		LinkedList<DataStoreObject> list = new LinkedList<>();

		for (Object key : table.keySet()) {
			if (startKey.equals(key) || endKey.equals(key) || (dsod.getPrimaryKey().compareTo(key, startKey) < 0
					&& dsod.getPrimaryKey().compareTo(key, endKey) > 0)) {
				DataStoreObject dso = table.get(key);
				if (dso != null) {
					list.add(dso.copy());
				}
			}
		}

		return list;
	}

	@Override
	public List<? extends DataStoreObject> loadBulkDSO(DataStoreObjectDescriptor dsod, LinkedList<Object> keys)
			throws Exception {
		ConcurrentHashMap<Object, DataStoreObject> table = tableMap.get(dsod);
		LinkedList<DataStoreObject> list = new LinkedList<>();
		for (Object key : keys) {
			DataStoreObject dso = table.get(key);
			if (dso != null) {
				list.add(dso.copy());
			}
		}
		return list;
	}

	@Override
	public DataStoreObject loadDSO(DataStoreObjectDescriptor dsod, Object key) throws Exception {
		DataStoreObject dso = tableMap.get(dsod).get(key);
		if (dso == null)
			return null;
		return dso.copy();
	}

	@Override
	public void save(Collection<DataStoreObject> dsoList) throws Exception {
		for (DataStoreObject dso : dsoList) {
			save(dso);
		}
	}

	@Override
	public void save(DataStoreObject dso) throws Exception {
		DataStoreObjectDescriptor dsod = dso.getDataStoreObjectDescriptor();
		if (tableMap.get(dsod).containsKey(dso.getPrimaryKey())) {
			tableReplaces.incrementAndGet(internalTableIds.get(dsod));
		}
		synchronized (dsod) {
			ByteArray ba = new ByteArray();
			dso.toBytes(ba);
			fileService.append(getTableFilePath(dsod.getTableName()), ba.getAllBytes());
		}
	}

	private ConcurrentHashMap<Object, DataStoreObject> buildTable(DataStoreObjectDescriptor dsod) {

		String tableName = dsod.getTableName();
		byte[] tableBA;
		ConcurrentHashMap<Object, DataStoreObject> table = new ConcurrentHashMap<Object, DataStoreObject>();

		try {
			tableBA = fileService.readAllBytes(getTableFilePath(tableName), -1);
		} catch (ReadFileNotFoundException e) {
			byte[] header = createHeader(dsod);
			try {
				fileService.write(getTableFilePath(tableName), header);
			} catch (IOException e1) {
				Log.fatal(e1);
				return table;
			}
			tableBA = header;
		} catch (IOException e1) {
			Log.fatal(e1);
			return table;
		}

		ByteArray ba = new ByteArray(tableBA);
		ba.remove(headerLength);
		while (ba.length() > 0) {
			DataStoreObject dso;
			try {
				dso = dsod.newDsoInstance();
			} catch (Exception e) {
				Log.fatal(e);
				return table;
			}
			for (int col = 0; col < dsod.getColumnCount(); col++) {
				Type type = dsod.getColumnTypes().get(col);
				dso.set(col, type.fromBytes(ba));
			}
			if (table.containsKey(dso.getPrimaryKey())) {
				tableReplaces.incrementAndGet(internalTableIds.get(dsod));
			}
			table.put(dso.getPrimaryKey(), dso);
		}
		return table;
	}

	private byte[] createHeader(DataStoreObjectDescriptor dsod) {
		return new byte[headerLength];
	}

	protected String getTableFilePath(String tableName) {
		return this.storageRootDir + "/" + tableName + "." + this.dataTableSuffix;
	}

	private void createCompactDatabasesTask() {
		LocalDataStoreService thisService = this;
		threadService.executeLater(new ThreadServiceTask() {
			@Override
			public void execute() throws Exception {
				thisService.compactDatabases();
			}
		}, compactionDelay);
	}

	protected void compactDatabases() throws IOException {
		for (Entry<DataStoreObjectDescriptor, Integer> iti : internalTableIds.entrySet()) {
			double tr = tableReplaces.get(iti.getValue());
			double ts = tableMap.get(iti.getKey()).size();
			if (tr / ts > compactionThresholdFactor) {
				compactDatabase(iti.getKey());
			}
		}
		createCompactDatabasesTask();
	}

	private void compactDatabase(DataStoreObjectDescriptor dsod) throws IOException {
		synchronized (dsod) {
			ByteArray ba = new ByteArray();
			for (DataStoreObject dso : tableMap.get(dsod).values())
			dso.toBytes(ba);
			
			fileService.replace(getTableFilePath(dsod.getTableName()), ba.getAllBytes()); 
			
		}
		
	}
}
