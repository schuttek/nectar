package org.nectarframework.base.tools;

/**
 * This class essentially works like java.io.ByteBuffer. You can add data to the front and back of the ByteArray, and remove bytes from the front.
 * 
 *  A common use case is to pack a set of mixed raw values (say, an int, a String and a double) into a byte array, then unpack them later. 
 * 
 * This class never actually changes the contents of byte arrays passed to it, so there's no need to copy arrays before passing them to these methods.
 * 
 *  WARNING: DO NOT CONFUSE THIS WITH StringBuffer! Adding Strings to this object in sequence does NOT concatenate Strings, but stores them as separate strings! byteArray.add("hello, "); byteArray.add("World!") will NOT become "hello, World!".
 *  
 * @author skander
 *
 */

public class ByteArray {
	private class Chunk {
		public byte[] array = null;
		int startIdx;
		int length;
		public Chunk next = null;
	}

	private int length = 0;

	private Chunk front = null;
	private Chunk back = null;

	public ByteArray(byte[] b) {
		add(b);
	}

	public ByteArray() {
	}

	private Chunk makeChunk(byte[] b, int fromIdx, int length) {
		if (fromIdx + length > b.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		Chunk tree = new Chunk();
		tree.array = b;
		tree.startIdx = fromIdx;
		tree.length = length;
		return tree;
	}

	public void addToFront(byte[] b, int fromIdx, int length) {
		if (length == 0)
			return;
		Chunk chunk = makeChunk(b, fromIdx, length);
		if (front == null) {
			front = chunk;
			back = front;
		} else {
			chunk.next = front;
			front = chunk;
		}
		this.length += chunk.length;
	}

	public void add(byte[] b, int fromIdx, int length) {
		if (length == 0)
			return;
		Chunk chunk = makeChunk(b, fromIdx, length);
		if (front == null) {
			front = chunk;
			back = chunk;
		} else {
			back.next = chunk;
			back = chunk;
		}
		this.length += chunk.length;
	}

	/**
	 * Compacts the internal storage data structures to it's minimal format. 
	 * 
	 * Every add() operation can increase the numbers of small data buffers and internal pointers. 
	 * 
	 * This method realigns the internal data structure into a single byte array.
	 * 
	 * max runtime is O(this.length) 
	 * 
	 */
	public void coalesce() {
		if (front == null || front.next == null) {
			return;
		}
		byte[] bb = new byte[length];

		int i = 0;
		Chunk cursor = front;
		while (cursor != null) {
			System.arraycopy(cursor.array, cursor.startIdx, bb, i, cursor.length);
			i += cursor.length;
			cursor = cursor.next;
		}

		front = makeChunk(bb, 0, length);
		back = front;
	}

	public int length() {
		return length;
	}

	public byte[] remove(int numBytes) {
		if (numBytes == 0) {
			return new byte[0];
		}
		if (numBytes > this.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		coalesce();
		byte[] ret = new byte[numBytes];
		System.arraycopy(front.array, front.startIdx, ret, 0, numBytes);

		if (front.length - numBytes == 0) {
			front = null;
			length = 0;
		} else {
			front = makeChunk(front.array, front.startIdx + numBytes, front.length - numBytes);
			length = front.length;
		}
		back = front;
		return ret;
	}
	

	public byte getByte() {
		return remove(1)[0];
	}
	
	public short getShort() {
		return bytesToShort(remove(2), 0);
	}

	public int getInt() {
		return bytesToInt(remove(4), 0);
	}

	public long getLong() {
		return bytesToLong(remove(8), 0);
	}

	public double getDouble() {
		return Double.longBitsToDouble(getLong());
	}

	public float getFloat() {
		return Float.intBitsToFloat(getInt());
	}

	public String getString() {
		int len = getInt();
		byte[] sb = remove(len);
		return new String(sb);
	}

	public boolean getBoolean() {
		byte[] b = remove(1);
		if (b[0] == 0) {
			return false;
		}
		return true;
	}

	public void addToFront(byte[] b) {
		addToFront(b, 0, b.length);
	}

	public void addByteArray(byte[] b) {
		if (b == null || b.length == 0) {
			add(0);
		} else {
			add(b.length);
			add(b);
		}
	}
	
	public void add(byte[] b) {
		add(b, 0, b.length);
	}

	public void addToFront(int i) {
		byte[] b = new byte[4];
		intToBytes(i, b, 0);
		addToFront(b);
	}

	public void add(byte b) {
		byte[] ba = new byte[1];
		ba[0] = b;
		add(ba);
	}

	public void add(short s) {
		byte[] b = new byte[2];
		shortToBytes(s, b, 0);
		add(b);
	}
	
	public void add(int i) {
		byte[] b = new byte[4];
		intToBytes(i, b, 0);
		add(b);
	}

	public void addToFront(long l) {
		byte[] b = new byte[8];
		longToBytes(l, b, 0);
		addToFront(b);
	}

	public void add(long l) {
		byte[] b = new byte[8];
		longToBytes(l, b, 0);
		add(b);
	}

	public void addToFront(double d) {
		long l = Double.doubleToRawLongBits(d);
		byte[] b = new byte[8];
		longToBytes(l, b, 0);
		addToFront(b);
	}

	public void add(double d) {
		long l = Double.doubleToRawLongBits(d);
		byte[] b = new byte[8];
		longToBytes(l, b, 0);
		add(b);
	}

	public void addToFront(float f) {
		int l = Float.floatToRawIntBits(f);
		byte[] b = new byte[8];
		intToBytes(l, b, 0);
		addToFront(b);
	}

	public void add(float f) {
		int i = Float.floatToRawIntBits(f);
		byte[] b = new byte[4];
		intToBytes(i, b, 0);
		add(b);
	}

	public void addToFront(String s) {
		if (s == null) {
			addToFront(0);
		} else {
			byte[] b = s.getBytes();
			addToFront(b);
			addToFront(b.length);
		}
	}

	public void add(String s) {
		if (s == null) {
			add(0);
		} else {
			byte[] b = s.getBytes();
			add(b.length);
			add(b);
		}
	}

	public void addToFront(boolean bool) {
		byte[] b = new byte[1];
		b[0] = (byte) ((bool) ? 1 : 0);
		addToFront(b);
	}

	public void add(boolean bool) {
		byte[] b = new byte[1];
		b[0] = (byte) ((bool) ? 1 : 0);
		add(b);
	}
	
	public static short bytesToShort(byte[] array, int offset) {
		return (short) ( array[offset] & 0xFF << 8 | array[offset+1] & 0xFF );
	}
	
	public static long bytesToLong(byte[] array, int offset) {
		long value = 0;
		for (int i = offset; i < offset + 8; i++) {
			value += ((long) array[i] & 0xffL) << (8 * i);
		}
		return value;
	}

	public static int bytesToInt(byte[] array, int offset) {
		return array[offset] << 24 | (array[offset + 1] & 0xFF) << 16 | (array[offset + 2] & 0xFF) << 8 | (array[offset + 3] & 0xFF);
	}

	public static void shortToBytes(int i, byte[] byteBuff, int offset) {
		for (int t = 0; t < 2; t++) {
			byteBuff[offset + t] = (byte) (i >> (2 - (t + 1)) * 8);
		}
	}
	
	public static void intToBytes(int i, byte[] byteBuff, int offset) {
		for (int t = 0; t < 4; t++) {
			byteBuff[offset + t] = (byte) (i >> (4 - (t + 1)) * 8);
		}
	}

	public static void longToBytes(long i, byte[] bb, int offset) {
		for (int t = 0; t < 8; t++) {
			bb[offset + t] = (byte) (i >> (8 - (t + 1)) * 8);
		}
	}

	public static void doubleToBytes(double d, byte[] bb, int offset) {
		longToBytes(Double.doubleToRawLongBits(d), bb, offset);
	}

	public static double bytesToDouble(byte[] array, int offset) {
		long value = 0;
		for (int i = offset; i < offset + 8; i++) {
			value += ((long) array[i] & 0xffL) << (8 * i);
		}
		return Double.longBitsToDouble(value);
	}

	public byte[] getByteArray() {
		int len = getInt();
		byte[] sb = remove(len);
		return sb;
	}
	
	public byte[] getBytes() {
		return remove(length());
	}

	public void reset() {
		front = null;
		back = null;
		length = 0;
	}


}
