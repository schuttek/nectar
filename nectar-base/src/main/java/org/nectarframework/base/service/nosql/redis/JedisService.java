package org.nectarframework.base.service.nosql.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.log.Log;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisService extends Service {

	private String hostname;
	private int port;
	private String password;
	private int databaseId;
	private int connectionsIdleMin;
	private int connectionsIdleMax;
	private int connectionsTotalMax;
	private long poolMaxWaitTime;
	private int timeout;
	
	
	private JedisPool jedisPool;

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		hostname = sp.getString("hostname", "localhost");
		port = sp.getInt("port", 1, Short.MAX_VALUE, 6379);
		timeout = sp.getInt("timeout", 0, Integer.MAX_VALUE, 3000);
		password = sp.getString("password", null);
		databaseId = sp.getInt("databaseId", 0, Integer.MAX_VALUE, 0);
		connectionsIdleMin = sp.getInt("connectionsIdleMin", 0, 10000, 3);
		connectionsIdleMax = sp.getInt("connectionsIdleMin", 0, 10000, 30);
		connectionsTotalMax = sp.getInt("connectionsIdleMin", 0, 10000, 30);
		// -1 for pool default value.
		poolMaxWaitTime = sp.getLong("poolMaxWaitTime", -1, Long.MAX_VALUE, -1);
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		return true;
	}

	@Override
	protected boolean init() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMinIdle(connectionsIdleMin);
		config.setMaxIdle(connectionsIdleMax);
		config.setMaxTotal(connectionsTotalMax);
		if(poolMaxWaitTime >= 0) {
			config.setMaxWaitMillis(poolMaxWaitTime);
		}
		try {
			jedisPool = new JedisPool(config, hostname, port, timeout, password, databaseId);
		} catch (RuntimeException e) {
			Log.fatal(e);
			return false;
		}
		return true;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		jedisPool.close();
		return true;
	}

	public JedisPool getPool() {
		return jedisPool;
	}
}
