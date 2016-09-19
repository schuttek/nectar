package org.nectarframework.base.service.cache;

import org.nectarframework.base.tools.ByteArray;

/**
 * CacheableObject have to be able to write and read themselves from a
 * ByteArray. Essentially this is a Serialization mechanism.
 * 
 * @author skander
 *
 */

public interface CacheableObject {

	public void fromBytes(ByteArray ba);

	public ByteArray toBytes(ByteArray ba);

}
