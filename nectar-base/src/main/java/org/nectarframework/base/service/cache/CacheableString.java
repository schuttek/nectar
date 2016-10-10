package org.nectarframework.base.service.cache;

import org.nectarframework.base.tools.ByteArray;

public class CacheableString implements CacheableObject {
	private String str;

	public CacheableString(String s) {
		this.str = s;
	}

	@Override
	public CacheableString fromBytes(ByteArray ba) {
		str = ba.getString();
		return this;
	}

	@Override
	public ByteArray toBytes(ByteArray ba) {
		ba.add(str);
		return ba;
	}

	public String getString() {
		return str;
	}
	
	public String setString() {
		return str;
	}
}
