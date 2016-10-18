package org.nectarframework.base.service.cache;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.internode.InternodeService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.thread.ThreadServiceTask;
import org.nectarframework.base.tools.Tuple;

//TODO: implement a cluster level cache that backs up the local cache.

public abstract class CacheService extends Service {

	protected long flushDelay = 60000; // one second
	protected long defaultExpiry = 3600000; // one hour
	protected float expiryFactor = 1.0f;

	protected long flushTimer = 0;

	private ThreadService threadService;

	/*** Service Methods ***/

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		flushDelay = sp.getLong("flushDelay", 0, Integer.MAX_VALUE, flushDelay);
		defaultExpiry = sp.getLong("defaultExpiry", 0, Integer.MAX_VALUE, defaultExpiry);
		expiryFactor = sp.getFloat("expiryFactor", -1.0f, 10000.0f, 1.0f);
		_checkParameters(sp);
	}
	

	protected abstract void _checkParameters(ServiceParameters sp) throws ConfigurationException;

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		threadService = (ThreadService) this.dependency(ThreadService.class);
		return _establishDependencies();
	}

	protected abstract boolean _establishDependencies() throws ServiceUnavailableException;

	@Override
	protected boolean init() {
		flushTimer = 0;
		return _init();
	}

	protected abstract boolean _init();

	@Override
	protected boolean run() {
		createCheckFlushTimerTask();
		return _run();
	}
	
	protected abstract boolean _run();
	
	@Override
	protected boolean shutdown() {
		removeAll();
		return _shutdown();
	}

	protected abstract boolean _shutdown();

	/*** Convenience Methods ***/
	/**
	 * returns a cached CacheableObject for string key if it exists.
	 * 
	 * @param key
	 * @return
	 */
	public CacheableObject getObject(String key) {
		return getObject(null, key, true);
	}

	/**
	 * returns a cached CacheableObject for string key if it exists.
	 * 
	 * @param key
	 * @param refreshCache
	 *            if true, update the last used time for this cache entry.
	 * @return
	 */
	public CacheableObject getObject(String key, boolean refreshCache) {
		return getObject(null, key, refreshCache);
	}

	public CacheableObject getObject(Service realm, String key) {
		return getObject(null, key, true);
	}
	
	/**
	 * Inserts a new key or overwrites an existing key with the given
	 * CacheableObject. The entry will expire after defaultExpiry milliseconds.
	 * See configuration for this Service.
	 * 
	 * @param key
	 * @param ba
	 */

	public void set(String key, CacheableObject co) {
		set(null, key, co, defaultExpiry);
	}

	/**
	 * Inserts a new key or overwrites an existing key with the given
	 * CaheableObject. The entry will expire after the given expiry
	 * milliseconds.
	 * 
	 * @param key
	 * @param ba
	 * @param expiry
	 */

	public void set(String key, CacheableObject co, long expiry) {
		set(null, key, co, expiry);
	}

	public void set(Service realm, String key, CacheableObject co) {
		set(realm, key, co, defaultExpiry);
	}

	/**
	 * Inserts a new key for the CacheableObject only if it doesn't already
	 * exist. The entry will expire after defaultExpiry milliseconds, See
	 * configuration for this Service.
	 * 
	 * @param key
	 * @param ba
	 */
	public void add(String key, CacheableObject o) {
		add(null, key, o, defaultExpiry);
	}

	public void add(Service realm, String key, CacheableObject o) {
		add(realm, key, o, defaultExpiry);
	}

	public void add(String key, CacheableObject o, long expiry) {
		add(null, key, o, expiry);
	}

	/**
	 * Removes a cache entry by the given key.
	 * 
	 * @param key
	 */
	public void remove(String key) {
		remove(null, key);
	}
	
	/*** Operational Methods ***/

	public abstract CacheableObject getObject(Service realm, String key, boolean refreshCache);
	/**
	 * Inserts a new key or overwrites an existing key with the given
	 * CaheableObject. The entry will expire after the given expiry
	 * milliseconds.
	 * 
	 * @param key
	 * @param ba
	 * @param expiry
	 */
	public abstract void set(Service realm, String key, CacheableObject co, long expiry);
	/**
	 * Inserts a new key for the CacheableObject only if it doesn't already
	 * exist. The entry will expire after the given expiry milliseconds, See
	 * configuration for this Service.
	 * 
	 * @param key
	 * @param ba
	 */
	public abstract void add(Service realm, String key, CacheableObject co, long expiry);
	

	/**
	 * Removes a cache entry by the given key.
	 * 
	 * @param key
	 */
	public abstract void remove(Service realm, String key);

	/**
	 * Removes ALL cache entries.
	 */
	public abstract void removeAll();
	
	/*** Self Maintenance Methods ***/
	
	protected void checkFlushTimer() {
		long now = InternodeService.getTime();
		CacheService thisCS = this;
		if (flushTimer + flushDelay < now) {
			flushTimer = now + flushDelay;
			try {
				threadService.execute(new ThreadServiceTask() {
					@Override
					public void execute() throws Exception {
						thisCS.flushCache();
					}
				});
			} catch (Exception e) {
				Log.fatal(e);
			}
		}
	}

	/**
	 * Removes out dated or inefficient cache entires.
	 * 
	 */
	protected abstract void flushCache();
	
	protected void createCheckFlushTimerTask() {
		CacheService thisCS = this;
		threadService.executeLater(new ThreadServiceTask() {
			@Override
			public void execute() throws Exception {
				checkFlushTimer();
				thisCS.createCheckFlushTimerTask();
			}

		}, this.flushDelay - 1);
	}

}
