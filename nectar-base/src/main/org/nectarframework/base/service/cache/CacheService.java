package org.nectarframework.base.service.cache;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.internode.InternodeService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.thread.ThreadServiceTask;

//TODO: implement a cluster level cache that backs up the local cache.
//FIXME: [23:05:50]566 <TRACE> CacheService update: 0 items using estimated 12609 bytes or a recalculated 0 bytes.

public class CacheService extends Service {

	protected long maxMemory = 4294967296L; // 4 Gigs
	protected long flushDelay = 60000; // one second
	protected long defaultExpiry = 3600000; // one hour
	protected float expiryFactor = 1.0f;

	/**
	 * knowing actual memory usage of an object is complicated and slow, so
	 * we're just going to estimate it.
	 * 
	 * This number uses approximations, even in some cases just wild ass guesses
	 * to begin with, and is updated without proper thread safety, but it's
	 * better than nothing.
	 * 
	 * 
	 */
	protected long memoryUsage = 0;
	protected long flushTimer = 0;

	protected ConcurrentHashMap<String, CacheWrapper> cache;
	private ThreadService threadService;

	@Override
	public void checkParameters() throws ConfigurationException {
		maxMemory = this.serviceParameters.getLong("maxMemory", 0, Integer.MAX_VALUE, maxMemory);
		flushDelay = this.serviceParameters.getLong("flushDelay", 0, Integer.MAX_VALUE, flushDelay);
		defaultExpiry = this.serviceParameters.getLong("defaultExpiry", 0, Integer.MAX_VALUE, defaultExpiry);
		expiryFactor = this.serviceParameters.getFloat("expiryFactor", 0.0f, 10000.0f, 1.0f);
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		threadService = (ThreadService) this.dependancy(ThreadService.class);
		return true;
	}

	@Override
	protected boolean init() {
		cache = new ConcurrentHashMap<String, CacheWrapper>();
		return true;
	}

	@Override
	protected boolean run() {
		createCheckFlushTimerTask();
		return true;
	}

	@Override
	protected boolean shutdown() {
		removeAll();
		return true;
	}

	/**
	 * Short-hand for getByteArray(key, true);
	 * 
	 * @param key
	 * @return
	 */

	public byte[] getByteArray(String key) {
		return getByteArray(key, true);
	}

	/**
	 * returns a cached byte[] for key if it exists.
	 * 
	 * @param key
	 * @param refreshCache
	 *            if true, update the last used time for this cache entry.
	 * @return
	 */
	public byte[] getByteArray(String key, boolean refreshCache) {
		CacheWrapper cw = getWrapper(key, refreshCache);
		if (cw != null) {
			try {
				byte[] ret = cw.getData();
				return ret;
			} catch (ClassCastException e) {
				Log.fatal("CacheService -> ClassCastException. Did we hit a hash key duplicate??", e);
			}
		}
		return null;
	}

	public CacheableObject getObject(String key)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return getObject(key, true);
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
		checkFlushTimer();
		CacheWrapper cw = getWrapper(key, refreshCache);
		if (cw != null) {
			return cw.getObject();
		}
		return null;
	}

	/**
	 * Inserts a new key or overwrites an existing key with the given byte
	 * array. The entry will expire after defaultExpiry milliseconds. See
	 * configuration for this Service.
	 * 
	 * @param key
	 * @param ba
	 */

	public void set(String key, byte[] ba) {
		set(key, ba, defaultExpiry);
	}

	/**
	 * Inserts a new key or overwrites an existing key with the given byte
	 * array. The entry will expire after the given expiry milliseconds.
	 * 
	 * @param key
	 * @param ba
	 * @param expiry
	 */

	public void set(String key, byte[] ba, long expiry) {
		CacheWrapper cw = new CacheWrapper(InternodeService.getTime() + expiry);
		cw.setData(ba);
		replaceCache(key, cw);
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
		set(key, co, defaultExpiry);
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
		CacheWrapper cw = new CacheWrapper(InternodeService.getTime() + expiry);
		cw.setObject(co);
		replaceCache(key, cw);
	}

	/**
	 * Inserts a new key for the byte array only if it doesn't already exist.
	 * The entry will expire after defaultExpiry milliseconds, See configuration
	 * for this Service.
	 * 
	 * @param key
	 * @param ba
	 */
	public void add(String key, byte[] ba) {
		add(key, ba, defaultExpiry);
	}

	/**
	 * Inserts a new key for the byte array only if it doesn't already exist.
	 * The entry will expire after the given expiry milliseconds.
	 * 
	 * @param key
	 * @param ba
	 * @param expiry
	 */
	public void add(String key, byte[] ba, long expiry) {
		if (getWrapper(key) == null) {
			set(key, ba, expiry);
		}
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
		add(key, o, defaultExpiry);
	}

	/**
	 * Inserts a new key for the CacheableObject only if it doesn't already
	 * exist. The entry will expire after the given expiry milliseconds, See
	 * configuration for this Service.
	 * 
	 * @param key
	 * @param ba
	 */
	public void add(String key, CacheableObject o, long expiry) {
		if (getWrapper(key) == null) {
			set(key, o, expiry);
		}
	}

	protected CacheWrapper getWrapper(String key) {
		return getWrapper(key, false);
	}

	protected CacheWrapper getWrapper(String key, boolean refreshCache) {
		CacheWrapper cw = cache.get(key);
		long now = InternodeService.getTime();
		if (cw != null) {
			if (cw.getExpiry() < now && !refreshCache) {
				cache.remove(key);
				return null;
			}
			if (refreshCache) {
				cw.setExpiry(now + this.defaultExpiry);
			}
		}
		return cw;
	}

	/**
	 * Removes a cache entry by the given key.
	 * 
	 * @param key
	 */
	public void remove(String key) {
		cache.remove(key);
	}

	/**
	 * Removes ALL cache entries.
	 */
	public void removeAll() {
		cache.clear();
		flushTimer = InternodeService.getTime();
	}

	protected void checkFlushTimer() {
		long now = InternodeService.getTime();
		if (flushTimer + flushDelay < now) {
			flushTimer = now + flushDelay;
			try {
				threadService.execute(new ThreadServiceTask() {
					@Override
					public void execute() throws Exception {
						((CacheService) CacheService.instance).flushCache();
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

	protected void flushCache() {
		Set<String> keys = cache.keySet();
		long now = InternodeService.getTime();
		ArrayList<String> toRemove = new ArrayList<String>();
		for (String key : keys) {
			CacheWrapper cw = cache.get(key);
			if (cw != null && cw.getExpiry() < now) {
				toRemove.add(key);
			}
		}

		for (String key : toRemove) {
			cache.remove(key);
		}
	}

	protected void createCheckFlushTimerTask() {
		threadService.executeLater(new ThreadServiceTask() {
			@Override
			public void execute() throws Exception {
				checkFlushTimer();
				((CacheService) CacheService.instance).createCheckFlushTimerTask();
			}

		}, this.flushDelay - 1);
	}

	protected void replaceCache(String key, CacheWrapper cw) {
		CacheWrapper oldWrapper = cache.get(key);
		if (oldWrapper != null) {
			this.memoryUsage -= oldWrapper.estimateMemorySize();
		}
		this.memoryUsage += cw.estimateMemorySize();
		cache.put(key, cw);
		checkFlushTimer();
	}

}
