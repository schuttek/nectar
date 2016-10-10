package org.nectarframework.base.service.cache;

import org.nectarframework.base.tools.ByteArrayBuildable;

/**
 * CacheableObject have to be able to write and read themselves from a
 * ByteArray. Essentially this is a Serialization mechanism.
 * 
 * @author skander
 *
 */

public interface CacheableObject extends ByteArrayBuildable<Object> {


}
