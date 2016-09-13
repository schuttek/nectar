package org.nectarframework.base.service.cache;

import java.io.Serializable;

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
	
	protected CacheableObject getObject() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		ByteArray ba = new ByteArray(this.data);
		String className = ba.getString();
		CacheableObject co = (CacheableObject)ClassLoader.getSystemClassLoader().loadClass(className).newInstance();
		co.fromBytes(ba);
		return co;
	}
}
