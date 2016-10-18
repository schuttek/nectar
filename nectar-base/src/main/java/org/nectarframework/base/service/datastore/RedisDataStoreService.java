package org.nectarframework.base.service.datastore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.datastore.DataStoreObjectDescriptorColumn.Type;
import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.file.ReadFileNotFoundException;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.nosql.redis.JedisService;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.thread.ThreadServiceTask;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.ByteArray;

import redis.clients.jedis.Jedis;

/**
 * A DataStoreService that saves DataStoreObjects in the redis NoSQL database.
 * 
 * @author skander
 *
 */
public class RedisDataStoreService extends DataStoreService {
	private static final String primaryTablePrefix = "pt";

	private JedisService jedisService;

	@Override
	protected boolean secondStageinit() {
		return true;
	}

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		jedisService = (JedisService) dependency(JedisService.class);
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
	public List<DataStoreObject> loadAll(DataStoreObjectDescriptor dsod) throws Exception {
		try (Jedis jedis = jedisService.getPool().getResource()) {
			ByteArray keyBa = new ByteArray();
			keyBa.add(makePrimaryTableName(dsod.getTableName()));
			byte key[] = keyBa.getAllBytes();
			List<byte[]> map = jedis.hvals(key);

			ArrayList<DataStoreObject> list = new ArrayList<>(map.size());

			for (byte[] dsoba : map) {
				DataStoreObject dso = dsod.newDsoInstance();
				dso.fromBytes(new ByteArray(dsoba));
				list.add(dso);
			}
			return list;
		}
	}

	/**
	 * Scanning keys
	 */
	@Override
	public List<DataStoreObject> loadRange(DataStoreObjectDescriptor dsod, Object startKey, Object endKey)
			throws Exception {
		List<DataStoreObject> list = loadAll(dsod);
		for (DataStoreObject dso : list) {
			if (dsod.getPrimaryKey().getType().compareTo(dso.getPrimaryKey(), startKey) < 0
					|| dsod.getPrimaryKey().getType().compareTo(dso.getPrimaryKey(), endKey) > 0) {
				list.remove(dso);
			}
		}
		return list;
	}

	@Override
	public List<DataStoreObject> loadBulkDSO(DataStoreObjectDescriptor dsod, Object... keys) throws Exception {
		LinkedList<DataStoreObject> list = new LinkedList<>();
		try (Jedis jedis = jedisService.getPool().getResource()) {
			ByteArray keyBa = new ByteArray();
			keyBa.add(makePrimaryTableName(dsod.getTableName()));
			byte key[] = keyBa.getAllBytes();

			byte[][] fields = new byte[keys.length][];
			int i = 0;
			for (Object k : keys) {
				fields[i++] = dsod.getPrimaryKey().getType().toBytes(k, new ByteArray()).getAllBytes();
			}

			List<byte[]> resultList = jedis.hmget(key, fields);
			for (byte[] rowba : resultList) {
				DataStoreObject dso = dsod.newDsoInstance();
				dso.fromBytes(new ByteArray(rowba));
				list.add(dso);
			}
		}

		return list;
	}

	@Override
	public DataStoreObject loadDSO(DataStoreObjectDescriptor dsod, Object key) throws Exception {
		List<? extends DataStoreObject> l = loadBulkDSO(dsod, new Object[] { key });
		if (l == null || l.isEmpty()) {
			return null;
		}
		return l.get(0);
	}

	@Override
	public void save(DataStoreObject... dsoList) throws Exception {

		try (Jedis jedis = jedisService.getPool().getResource()) {

			for (DataStoreObject dso : dsoList) {

				// primary key & data store.
				ByteArray keyBa = new ByteArray();

				DataStoreObjectDescriptor dsod = dso.getDataStoreObjectDescriptor();
				String tableName = dsod.getTableName();

				// key
				keyBa.add(makePrimaryTableName(tableName));
				byte[] key = keyBa.getAllBytes();

				// field
				DataStoreObjectDescriptorKey primaryKey = dsod.getPrimaryKey();
				Object fieldObject = dso.getObject(primaryKey.getColumnName());
				byte[] field = primaryKey.getType().toBytes(fieldObject, new ByteArray()).getAllBytes();

				// value
				byte[] value = dso.toBytes(new ByteArray()).getAllBytes();

				jedis.hset(key, field, value);
			}
		}
	}

	private String makePrimaryTableName(String tableName) {
		return primaryTablePrefix + tableName;
	}
}
