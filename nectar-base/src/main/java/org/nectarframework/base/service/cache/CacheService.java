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
import org.nectarframework.base.tools.Tuple;

//TODO: implement a cluster level cache that backs up the local cache.

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

	protected ConcurrentHashMap<String, Tuple<CacheableObject, Long>> generalCache;
	protected ConcurrentHashMap<Service, ConcurrentHashMap<String, Tuple<CacheableObject, Long>>> realmCache;
	private ThreadService threadService;

	/*** Service Methods ***/

	@Override
	public void checkParameters() throws ConfigurationException {
		maxMemory = this.serviceParameters.getLong("maxMemory", 0, Integer.MAX_VALUE, maxMemory);
		flushDelay = this.serviceParameters.getLong("flushDelay", 0, Integer.MAX_VALUE, flushDelay);
		defaultExpiry = this.serviceParameters.getLong("defaultExpiry", 0, Integer.MAX_VALUE, defaultExpiry);
		expiryFactor = this.serviceParameters.getFloat("expiryFactor", -1.0f, 10000.0f, 1.0f);
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		threadService = (ThreadService) this.dependancy(ThreadService.class);
		return true;
	}

	@Override
	protected boolean init() {
		generalCache = new ConcurrentHashMap<>();
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

	/*** Operational Methods ***/

	public CacheableObject getObject(Service realm, String key, boolean refreshCache) {
		checkFlushTimer();
		Tuple<CacheableObject, Long> tup = null;
		if (realm == null) {
			tup = generalCache.get(key);
		} else {
			ConcurrentHashMap<String, Tuple<CacheableObject, Long>> realmMap = realmCache.get(realm);
			if (realmMap != null) {
				tup = realmMap.get(key);
			}
		}
		// Log.trace("[CacheService:getWrapper " + key + ((tup == null) ? "null"
		// : "found"));
		if (tup == null)
			return null;
		long now = InternodeService.getTime();
		if (tup.getRight() < now && !refreshCache) {
			generalCache.remove(key);
			return null;
		}
		if (refreshCache) {
			Tuple<CacheableObject, Long> newTup = new Tuple<>(tup.getLeft(), now);
			generalCache.put(key, newTup);
		}
		return tup.getLeft();
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
	public void set(Service realm, String key, CacheableObject co, long expiry) {
		checkFlushTimer();
		if (realm == null) {
			generalCache.put(key, new Tuple<CacheableObject, Long>(co, expiry));
		} else {
			ConcurrentHashMap<String, Tuple<CacheableObject, Long>> realmMap = realmCache.get(realm);
			if (realmMap == null) {
				realmMap = new ConcurrentHashMap<>();
				realmCache.put(realm, realmMap);
			}
			realmMap.put(key, new Tuple<CacheableObject, Long>(co, expiry));
		}
	}

	/**
	 * Inserts a new key for the CacheableObject only if it doesn't already
	 * exist. The entry will expire after the given expiry milliseconds, See
	 * configuration for this Service.
	 * 
	 * @param key
	 * @param ba
	 */
	public void add(Service realm, String key, CacheableObject co, long expiry) {
		if (realm == null) {
			generalCache.putIfAbsent(key, new Tuple<CacheableObject, Long>(co, expiry));
		} else {
			ConcurrentHashMap<String, Tuple<CacheableObject, Long>> realmMap = realmCache.get(realm);
			if (realmMap == null) {
				realmMap = new ConcurrentHashMap<>();
				realmCache.put(realm, realmMap);
			}
			realmMap.putIfAbsent(key, new Tuple<CacheableObject, Long>(co, expiry));
		}
	}

	/**
	 * Removes a cache entry by the given key.
	 * 
	 * @param key
	 */
	public void remove(String key) {
		remove(null, key);
	}

	/**
	 * Removes a cache entry by the given key.
	 * 
	 * @param key
	 */
	public void remove(Service realm, String key) {
		if (realm == null) {
			generalCache.remove(key);
		}
		ConcurrentHashMap<String, Tuple<CacheableObject, Long>> realmMap = realmCache.get(realm);
		if (realmMap != null) {
			realmMap.remove(key);
		}
	}

	/**
	 * Removes ALL cache entries.
	 */
	public void removeAll() {
		generalCache.clear();
		realmCache.clear();
		flushTimer = InternodeService.getTime();
	}
	
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
	protected void flushCache() {
		flushCacheMap(generalCache);
		for (ConcurrentHashMap<String, Tuple<CacheableObject, Long>> m : realmCache.values()) {
			flushCacheMap(m);
		}
	}
	
	
	protected void flushCacheMap(ConcurrentHashMap<String, Tuple<CacheableObject, Long>> map) {
		Set<String> keys = map.keySet();
		long now = InternodeService.getTime();
		ArrayList<String> toRemove = new ArrayList<String>();
		for (String key : keys) {
			Tuple<CacheableObject, Long> tup = map.get(key);
			if (tup != null && tup.getRight() < now) {
				toRemove.add(key);
			}
		}

		for (String key : toRemove) {
			map.remove(key);
		}
	}

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
