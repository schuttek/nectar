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

	protected ConcurrentHashMap<String, Tuple<CacheableObject, Long>> cache;
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
		cache = new ConcurrentHashMap<>();
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

	public CacheableObject getObject(String key) {
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
		Tuple<CacheableObject, Long> tup = cache.get(key);
		Log.trace("[CacheService:getWrapper " + key + ((tup == null) ? "null" : "found"));
		if (tup == null)
			return null;
		long now = InternodeService.getTime();
		if (tup.getRight() < now && !refreshCache) {
			cache.remove(key);
			return null;
		}
		if (refreshCache) {
			Tuple<CacheableObject, Long> newTup = new Tuple<>(tup.getLeft(), now);
			cache.put(key, newTup);
		}
		return tup.getLeft();
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
		cache.put(key, new Tuple<CacheableObject, Long>(co, expiry));
		checkFlushTimer();
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
		if (!cache.containsKey(key)) {
			set(key, o, expiry);
		}
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
		Set<String> keys = cache.keySet();
		long now = InternodeService.getTime();
		ArrayList<String> toRemove = new ArrayList<String>();
		for (String key : keys) {
			Tuple<CacheableObject, Long> tup = cache.get(key);
			if (tup != null && tup.getRight() < now) {
				toRemove.add(key);
			}
		}

		for (String key : toRemove) {
			cache.remove(key);
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
