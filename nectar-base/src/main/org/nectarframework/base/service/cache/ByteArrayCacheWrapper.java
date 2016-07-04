package org.nectarframework.base.service.cache;

public class ByteArrayCacheWrapper extends CacheWrapper {
	private static final long serialVersionUID = -9004406524707138851L;

	private byte[] byteArray;

	public ByteArrayCacheWrapper(long l) {
		super(l);
	}

	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;
	}

	public byte[] getByteArray() {
		return byteArray;
	}

	@Override
	public long estimateMemorySize() {
		return byteArray.length;
	}

}
