package org.nectarframework.base.service.cache;

import org.nectarframework.base.tools.ByteArray;

public class CacheableString implements CacheableObject {
	private String str;

	public CacheableString(String s) {
		if (s == null) {
			throw new IllegalArgumentException();
		}
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
}
