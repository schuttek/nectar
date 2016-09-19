package org.nectarframework.base.service.cache;

import org.nectarframework.base.tools.ByteArray;

public interface CacheableObject {

	public void fromBytes(ByteArray ba);
	
	public ByteArray toBytes(ByteArray ba);
	
}
