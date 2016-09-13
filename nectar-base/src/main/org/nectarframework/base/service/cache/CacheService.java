package org.nectarframework.base.service.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.internode.InternodeService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadService;

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

	protected HashMap<String, CacheWrapper> cache;
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
		cache = new HashMap<String, CacheWrapper>();
		return true;
	}

	@Override
	protected boolean run() {
		this.checkFlushTimerTaskCallback();
		return true;
	}

	@Override
	protected boolean shutdown() {
		removeAll();
		return true;
	}

	public byte[] getByteArray(String key) {
		return getByteArray(key, true);
	}
	
	public byte[] getByteArray(String key, boolean refreshCache) {
		checkFlushTimer();
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

	public CacheableObject getObject(String key) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return getObject(key, true);
	}
	
	public CacheableObject getObject(String key, boolean refreshCache) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		checkFlushTimer();
		CacheWrapper cw = getWrapper(key, refreshCache);
		if (cw != null) {
			try {
				return cw.getObject();
			} catch (ClassCastException e) {
				Log.fatal("CacheService -> ClassCastException. Did we hit a hash key duplicate??", e);
			}
		}
		return null;
	}

	private void replaceCache(String key, CacheWrapper cw) {
		CacheWrapper oldWrapper = cache.get(key);
		if (oldWrapper != null) {
			this.memoryUsage -= oldWrapper.estimateMemorySize();
		}
		this.memoryUsage += cw.estimateMemorySize();
		cache.put(key, cw);
		checkFlushTimer();
	}

	public void set(String key, byte[] ba) {
		set(key, ba, defaultExpiry);
	}

	public void set(String key, byte[] ba, long expiry) {
		CacheWrapper cw = new CacheWrapper(InternodeService.getTime() + expiry);
		cw.setData(ba);
		replaceCache(key, cw);
	}

	public void set(String key, CacheableObject co) {
		set(key, co, defaultExpiry);
	}

	public void set(String key, CacheableObject co, long expiry) {
		CacheWrapper cw = new CacheWrapper(InternodeService.getTime() + expiry);
		cw.setObject(co);
		replaceCache(key, cw);
	}

	public void add(String key, byte[] ba) {
		add(key, ba, defaultExpiry);
	}

	public void add(String key, byte[] ba, long expiry) {
		if (getWrapper(key) == null) {
			set(key, ba, expiry);
		}
	}

	public void add(String key, CacheableObject o) {
		add(key, o, defaultExpiry);
	}

	public void add(String key, CacheableObject o, long expiry) {
		if (getWrapper(key) == null) {
			set(key, o, expiry);
		}
	}

	public CacheWrapper getWrapper(String key) {
		return getWrapper(key, false);
	}
	
	public CacheWrapper getWrapper(String key, boolean refreshCache) {
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

	public void remove(String key) {
		cache.remove(key);
	}

	public void removeAll() {
		cache.clear();
		flushTimer = InternodeService.getTime();
	}

	private void checkFlushTimer() {
		long now = InternodeService.getTime();
		if (flushTimer + flushDelay < now) {
			flushTimer = now + flushDelay;
			FlushOldCache foc = new FlushOldCache(this);
			try {
				threadService.execute(foc);
			} catch (Exception e) {
				Log.fatal(e);
			}

		}
	}

	public void flushOldCache() {
		Set<String> keys = cache.keySet();
		long now = InternodeService.getTime();
		long recalcMemoryUsage = 0;
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
		
//		Log.trace("CacheService update: "+cache.size()+" items using estimated "+this.memoryUsage+" bytes or a recalculated "+recalcMemoryUsage+" bytes.");
	}

	public void checkFlushTimerTaskCallback() {
		checkFlushTimer();
		threadService.executeLater(new CheckFlushTimerTask(this), this.flushDelay -1);
	}

}
