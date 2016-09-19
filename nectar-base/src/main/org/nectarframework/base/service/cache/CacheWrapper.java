package org.nectarframework.base.service.cache;

import java.io.Serializable;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.tools.ByteArray;

public class CacheWrapper implements Serializable {
	private static final long serialVersionUID = 3412734420305501244L;
	protected long expiry;
	private byte[] data;

	protected CacheWrapper(long exp) {
		expiry = exp;
	}

	protected final long getExpiry() {
		return expiry;
	}
	
	protected long estimateMemorySize() {
		return data.length+16;
	}

	protected void setExpiry(long l) {
		expiry = l;
	}
	
	protected void setData(byte[] data) {
		this.data = data;
	}
	
	protected void setObject(CacheableObject co) {
		ByteArray ba = new ByteArray();
		co.toBytes(ba);
		ba.add(co.getClass().getName());
		this.data = ba.getBytes();
	}

	protected byte[] getData() {
		return this.data;
	}
	
	protected CacheableObject getObject() {
		ByteArray ba = new ByteArray(this.data);
		String className = ba.getString();
		CacheableObject co;
		try {
			co = (CacheableObject)ClassLoader.getSystemClassLoader().loadClass(className).newInstance();
		} catch (InstantiationException e) {
			Log.warn(e);
			return null;
		} catch (IllegalAccessException e) {
			Log.warn(e);
			return null;
		} catch (ClassNotFoundException e) {
			Log.warn(e);
			return null;
		}
		co.fromBytes(ba);
		return co;
	}
}
