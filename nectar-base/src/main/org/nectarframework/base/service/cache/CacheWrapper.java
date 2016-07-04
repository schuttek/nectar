package org.nectarframework.base.service.cache;

import java.io.Serializable;

public abstract class CacheWrapper implements Serializable {
	private static final long serialVersionUID = 3412734420305501244L;
	protected long expiry;

	public CacheWrapper(long l) {
		expiry = l;
	}

	public final long getExpiry() {
		return expiry;
	}
	
	public abstract long estimateMemorySize();

	public void setExpiry(long l) {
		expiry = l;
	}
}
