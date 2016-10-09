package org.nectarframework.base.tools;

public class BitMap implements ByteArrayBuildable<BitMap> {

	private byte[] map;
	private int size;

	public BitMap() {
	}
	
	public BitMap(int size) {
		this.size = size;
		map = new byte[size / 8 + (size % 8 == 0 ? 0 : 1)];
		for (int n = 0; n < map.length; n++) {
			map[n] = 0;
		}
	}

	public BitMap(byte[] map, int size) {
		this.map = map;
		this.size = size;
	}

	public boolean is(int index) {
		return (map[index / 8] & (1 << (index % 8))) != 0;
	}

	public void set(int index, boolean value) {
		byte b = map[index / 8];
		byte posBit = (byte) (1 << (index % 8));
		if (value) {
			b |= posBit;
		} else {
			b &= (255 - posBit);
		}
		map[index / 8] = b;
	}

	public void set(int index) {
		set(index, true);
	}

	public void clear(int index) {
		set(index, false);
	}

	public int size() {
		return size;
	}
	
	@Override
	public BitMap fromBytes(ByteArray ba) {
		size = ba.getInt();
		map = ba.getByteArray();
		return this;
	}

	@Override
	public ByteArray toBytes(ByteArray ba) {
		ba.add(size);
		ba.addByteArray(map);
		return ba;
	}

}
