package org.nectarframework.base.service.cache;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.tools.ByteArray;

public class ObjectCacheWrapper extends CacheWrapper {

	private static final long serialVersionUID = 3641748163669592686L;

	public ObjectCacheWrapper(long l) {
		super(l);
	}

	private byte[] data;

	public void setObject(CacheableObject object) {
		ByteArray bq = object.toBytes();
		bq.addToFront(object.getClass().getName());
		data = bq.getBytes();
	}

	public CacheableObject getObject() {
		ByteArray bq = new ByteArray(data);
		String className = bq.getString();
		CacheableObject co;
		try {
			co = (CacheableObject) ClassLoader.getSystemClassLoader().loadClass(className).newInstance();
		} catch (InstantiationException e) {
			Log.fatal(e);
			return null;
		} catch (IllegalAccessException e) {
			Log.fatal(e);
			return null;
		} catch (ClassNotFoundException e) {
			Log.fatal(e);
			return null;
		}

		co.fromBytes(bq);

		return co;
	}

	@Override
	public long estimateMemorySize() {
		return data.length;
	}

}
