package org.nectarframework.base.service.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.internode.InternodeService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.mysql.MysqlPreparedStatement;
import org.nectarframework.base.service.mysql.ResultTable;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.xml.sax.SAXException;

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

	public Element getElement(String key) {
		return 	getElement(key, true);
	}
	
	public Element getElement(String key, boolean refreshCache) {
		checkFlushTimer();
		CacheWrapper cw = getWrapper(key, refreshCache);
		if (cw == null) {
			return null;
		}
		try {
			Element ret = XmlService.fromXml(((ByteArrayCacheWrapper)cw).getByteArray());
			return ret;
		} catch (ClassCastException e) {
			Log.fatal("CacheService -> ClassCastException. Did we hit a hash key duplicate??", e);
		} catch (SAXException e) {
			Log.warn("CacheService couldn't parse a cached Element from it's XML form.", e);
		}
		return null;

	}

	public ResultTable getResultTable(String key) {
		return getResultTable(key, true);
	}
	
	public ResultTable getResultTable(String key, boolean refreshCache) {
		checkFlushTimer();
		CacheWrapper cw = getWrapper(key, refreshCache);
		if (cw == null) {
			return null;
		}
		try {
			ResultTable ret = ((ResultTableCacheWrapper) cw).getResultTable();
			return ret;
		} catch (ClassCastException e) {
			Log.fatal("CacheService -> ClassCastException. Did we hit a hash key duplicate??", e);
		}
		return null;
	}

	public ResultTable getResultTable(MysqlPreparedStatement key) {
		return getResultTable(key, true);
	}
	
	public ResultTable getResultTable(MysqlPreparedStatement key, boolean refreshCache) {
		checkFlushTimer();
		CacheWrapper cw = getWrapper(Integer.toHexString(key.hashCode()), refreshCache);
		if (cw == null) {
			return null;
		}
		try {
			ResultTable ret = ((ResultTableCacheWrapper) cw).getResultTable();
			return ret;
		} catch (ClassCastException e) {
			Log.fatal("CacheService -> ClassCastException. Did we hit a hash key duplicate??", e);
		}
		return null;
	}

	public byte[] getByteArray(String key) {
		return getByteArray(key, true);
	}
	
	public byte[] getByteArray(String key, boolean refreshCache) {
		checkFlushTimer();
		CacheWrapper cw = getWrapper(key, refreshCache);
		if (cw != null) {
			try {
				byte[] ret = ((ByteArrayCacheWrapper) cw).getByteArray();
				return ret;
			} catch (ClassCastException e) {
				Log.fatal("CacheService -> ClassCastException. Did we hit a hash key duplicate??", e);
			}
		}
		return null;
	}

	public CacheableObject getGeneric(String key) {
		return getGeneric(key, true);
	}
	
	public CacheableObject getGeneric(String key, boolean refreshCache) {
		checkFlushTimer();
		CacheWrapper cw = getWrapper(key, refreshCache);
		if (cw != null) {
			try {
				CacheableObject o = ((ObjectCacheWrapper) cw).getObject();
				return o;
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

	/**
	 * Overwrites the current key in the cache.
	 * 
	 * @param key
	 * @param e
	 * @param expiry
	 */

	public void set(String key, Element e) {
		set(key, e, defaultExpiry);
	}

	public void set(String key, Element e, long expiry) {
		ByteArrayCacheWrapper cw = new ByteArrayCacheWrapper(InternodeService.getTime() + expiry);
		cw.setByteArray(XmlService.toXmlBytes(e));
		replaceCache(key, cw);
	}

	public void set(String key, ResultTable rt) {
		set(key, rt, defaultExpiry);
	}

	public void set(String key, ResultTable rt, long expiry) {
		ResultTableCacheWrapper cw = new ResultTableCacheWrapper(InternodeService.getTime() + expiry);
		cw.setResultTable(rt);
		replaceCache(key, cw);
	}

	public void set(String key, byte[] ba) {
		set(key, ba, defaultExpiry);
	}

	public void set(String key, byte[] ba, long expiry) {
		ByteArrayCacheWrapper cw = new ByteArrayCacheWrapper(InternodeService.getTime() + expiry);
		cw.setByteArray(ba);
		replaceCache(key, cw);
	}

	public void setGeneric(String key, CacheableObject o) {
		setGeneric(key, o, defaultExpiry);
	}

	public void setGeneric(String key, CacheableObject o, long expiry) {
		ObjectCacheWrapper cw = new ObjectCacheWrapper(InternodeService.getTime() + expiry);
		cw.setObject(o);
		replaceCache(key, cw);
	}

	/**
	 * Adds a key if it doesn't already exist.
	 * 
	 * @param key
	 * @param e
	 */
	public void add(String key, Element e) {
		add(key, e, defaultExpiry);
	}

	public void add(String key, Element e, long expiry) {
		if (getWrapper(key) == null) {
			set(key, e, expiry);
		}
	}

	public void add(String key, byte[] ba) {
		add(key, ba, defaultExpiry);
	}

	public void add(String key, byte[] ba, long expiry) {
		if (getWrapper(key) == null) {
			set(key, ba, expiry);
		}
	}

	public void add(String key, ResultTable rt) {
		add(key, rt, defaultExpiry);
	}

	public void add(String key, ResultTable rt, long expiry) {
		if (getWrapper(key) == null) {
			set(key, rt, expiry);
		}
	}

	public void add(MysqlPreparedStatement mps, ResultTable rt) {
		add(mps, rt, defaultExpiry);
	}

	public void add(MysqlPreparedStatement mps, ResultTable rt, long expiry) {
		if (getWrapper(Integer.toHexString(mps.hashCode())) == null) {
			set(Integer.toHexString(mps.hashCode()), rt, expiry);
		}
	}

	public void addGeneric(String key, CacheableObject o) {
		addGeneric(key, o, defaultExpiry);
	}

	public void addGeneric(String key, CacheableObject o, long expiry) {
		if (getWrapper(key) == null) {
			setGeneric(key, o, expiry);
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
			recalcMemoryUsage += cw.estimateMemorySize();
			if (cw != null && cw.getExpiry() < now) {
				toRemove.add(key);
			}
		}
		
		for (String key : toRemove) {
			cache.remove(key);
		}
		
		Log.trace("CacheService update: "+cache.size()+" items using estimated "+this.memoryUsage+" bytes or a recalculated "+recalcMemoryUsage+" bytes.");
	}

	public void checkFlushTimerTaskCallback() {
		checkFlushTimer();
		threadService.executeLater(new CheckFlushTimerTask(this), this.flushDelay -1);
	}

	public boolean fillCacheableObject(CacheableObject dso, String cacheKey) {
		// TODO fill from byte array
		
		return false;
	}
}
