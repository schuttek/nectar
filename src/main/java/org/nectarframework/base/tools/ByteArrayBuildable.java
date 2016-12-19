package org.nectarframework.base.tools;

public interface ByteArrayBuildable<T> {
	public T fromBytes(ByteArray ba);
	public ByteArray toBytes(ByteArray ba);
}
