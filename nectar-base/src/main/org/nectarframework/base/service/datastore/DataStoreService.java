package org.nectarframework.base.service.datastore;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;

/**
 * This service allows quick and easy access to common data objects.
 * 
 * @author skander
 *
 */
public abstract class DataStoreService extends Service {

	private String dataStoreObjectsConfigFile = "config/dataStoreObjects.xml";

	public boolean init() {
		Element dsoConfig;
		try {
			dsoConfig = XmlService.fromXml(Files.readAllBytes(new File(dataStoreObjectsConfigFile).toPath()));
		List<Element> dsoElmList = dsoConfig.getChildren("dataStoreObject");
		for (Element dsoElm : dsoElmList) {
			String packageName = dsoElm.get("package");
			String className = dsoElm.get("className");

			DataStoreObject dso = (DataStoreObject) ClassLoader.getSystemClassLoader().loadClass(packageName + "." + className).newInstance();
			dso.initDataStoreObjectDescriptor(this);
		}
		} catch (Exception e) {
			Log.fatal(e);
			return false;
		}
		return secondStageinit();
	}

	protected abstract boolean secondStageinit();

	public abstract List<? extends DataStoreObject> loadAll(DataStoreObjectDescriptor dsod) throws Exception;

	public abstract List<? extends DataStoreObject> loadRange(DataStoreObjectDescriptor dsod, Object startKey, Object endKey) throws Exception;

	public abstract List<? extends DataStoreObject> loadBulkDSO(DataStoreObjectDescriptor dsod, LinkedList<Object> keys) throws Exception;

	public abstract DataStoreObject loadDSO(DataStoreObjectDescriptor dsod, Object key) throws Exception;

	/**
	 * Updates the existing record, or inserts a new DSO if the given key is not
	 * found, or if the DSOD's primary key is an auto increment key, sets the
	 * newly generated primary key.
	 * 
	 * @param dso
	 * @throws Exception
	 */

	public void save(DataStoreObject dso) throws Exception {
		LinkedList<DataStoreObject> dsoList = new LinkedList<DataStoreObject>();
		dsoList.add(dso);
		save(dsoList);
	}

	public abstract void save(Collection<DataStoreObject> dsoList) throws Exception;

	private HashMap<Class<? extends DataStoreObject>, DataStoreObjectDescriptor> dsodMap = new HashMap<Class<? extends DataStoreObject>, DataStoreObjectDescriptor>();

	public final void initDataStoreObjectDescriptor(DataStoreObjectDescriptor dsod) {
		dsodMap.put(dsod.getDsoClass(), dsod);
	}

	public final List<? extends DataStoreObject> loadAll(Class<? extends DataStoreObject> dsoClass) throws Exception {
		return loadAll(getDataStoreObjectDescriptor(dsoClass));
	}
	
	public final DataStoreObjectDescriptor getDataStoreObjectDescriptor(Class<? extends DataStoreObject> dsoClass) {
		DataStoreObjectDescriptor dsod = dsodMap.get(dsoClass);
		if (dsod == null) {
			Log.fatal("DataStoreObject: " + dsoClass.getName() + " needs to be initialized by the DataStoreService before it can be instatiated. See the init() for " + this.getClass().getName());
		}
		return dsod;
	}
}
