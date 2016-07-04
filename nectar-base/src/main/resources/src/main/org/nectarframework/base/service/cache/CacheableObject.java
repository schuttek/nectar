package org.nectarframework.base.service.cache;

import org.nectarframework.base.tools.ByteArray;

public interface CacheableObject {

	public void fromBytes(ByteArray bq);
	
	public ByteArray toBytes();
	
}
