package org.nectarframework.base.tools;



public class BitMap {

	public static byte[] init(int size) {
		byte[] map = new byte[size / 8 + (size % 8 == 0 ? 0 : 1)];
		for (int n = 0; n < map.length; n++) {
			map[n] = 0;
		}
		return map;
	}

	public static boolean is(byte[] map, int index) {
		return (map[index / 8] & (1 << (index % 8))) != 0;
	}

	public static void set(byte[] map, int index, boolean value) {
		byte b = map[index / 8];
		byte posBit = (byte) (1 << (index % 8));
		if (value) {
			b |= posBit;
		} else {
			b &= (255 - posBit);
		}
		map[index / 8] = b;
	}

	public static void set(byte[] map, int index) {
		set(map, index, true);
	}

	public static void clear(byte[] map, int index) {
		set(map, index, false);
	}

}
