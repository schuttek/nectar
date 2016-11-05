package org.nectarframework.base.tools;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;


/**
 * This class essentially works like java.io.ByteBuffer. You can add data to the
 * front and back of the ByteArray, and remove bytes from the front. The main
 * difference is that it's a bit faster on many successive add() operations than
 * ByteBuffer's putType() methods, since new chunks are added as a list, and
 * therefore always O(1).
 * 
 * A common use case is to pack a set of mixed raw values (say, an int, a String
 * and a double) into a byte array, then unpack them later.
 * 
 * This class never actually changes the contents of byte arrays passed to it,
 * so there's no need to copy arrays before passing them to these methods.
 * 
 * You can only ever remove bytes from the front of a ByteArray, by using the
 * get{Type}() methods. ByteArray cannot be rewound. the coalesce method may
 * free memory at the beginning of the ByteArray as it's being read from, so
 * it's safe to use this as a backing to a messaging queue between two threads
 * for example.
 * 
 * This class is NOT threadsafe, so make sure you use external synchronization.
 * 
 * WARNING: DO NOT CONFUSE THIS WITH StringBuffer! Adding Strings to this object
 * in sequence does NOT concatenate Strings, but stores them as separate
 * strings! byteArray.add("hello, "); byteArray.add("World!") will NOT become
 * "hello, World!".
 * 
 * @author skander
 *
 */

public class ByteArray {

	private class Chunk {
		protected byte[] array = null;
		protected int startIdx;
		protected int length;
		protected Chunk next = null;
	}

	private int length = 0;

	private Chunk front = null;
	private Chunk back = null;

	public ByteArray(byte[] b) {
		addRawBytes(b);
	}

	public ByteArray() {
	}

	public ByteArray(ByteArray ba) {

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

	public void addRawBytes(byte[] b, int fromIdx, int length) {
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
	 * Every add() operation can increase the numbers of small data buffers and
	 * internal pointers.
	 * 
	 * This method realigns the internal data structure into a single byte
	 * array.
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
			front.startIdx += numBytes;
			front.length -= numBytes;
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
		return bytesToDouble(remove(8), 0);
	}

	public float getFloat() {
		return bytesToFloat(remove(4), 0);
	}

	public String getString() {
		int len = getInt();
		if (len == -1)
			return null;
		byte[] sb = remove(len);

		CharsetDecoder dec = StringTools.getCharset().newDecoder();
		dec.onMalformedInput(CodingErrorAction.IGNORE);
		try {
			return dec.decode(ByteBuffer.wrap(sb)).toString();
		} catch (CharacterCodingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean getBoolean() {
		byte[] b = remove(1);
		if (b[0] == 0) {
			return false;
		}
		return true;
	}

	public void addByteArray(byte[] b) {
		if (b == null) {
			add(-1);
		} else if (b.length == 0) {
			add(0);
		} else {
			add(b.length);
			addRawBytes(b);
		}
	}

	public void addByteArrayToFront(byte[] b) {
		if (b == null) {
			addToFront(-1);
		} else if (b.length == 0) {
			addToFront(0);
		} else {
			addRawBytesToFront(b);
			addToFront(b.length);
		}
	}

	public void addRawBytesToFront(byte[] b) {
		addToFront(b, 0, b.length);
	}

	public void addRawBytes(byte[] b) {
		addRawBytes(b, 0, b.length);
	}

	public void addToFront(int i) {
		byte[] b = new byte[4];
		intToBytes(i, b, 0);
		addRawBytesToFront(b);
	}

	public void add(byte b) {
		byte[] ba = new byte[1];
		ba[0] = b;
		addRawBytes(ba);
	}

	public void add(short s) {
		byte[] b = new byte[2];
		shortToBytes(s, b, 0);
		addRawBytes(b);
	}

	public void add(int i) {
		byte[] b = new byte[4];
		intToBytes(i, b, 0);
		addRawBytes(b);
	}

	public void addToFront(long l) {
		byte[] b = new byte[8];
		longToBytes(l, b, 0);
		addRawBytesToFront(b);
	}

	public void add(long l) {
		byte[] b = new byte[8];
		longToBytes(l, b, 0);
		addRawBytes(b);
	}

	public void addToFront(double d) {
		long l = Double.doubleToRawLongBits(d);
		byte[] b = new byte[8];
		longToBytes(l, b, 0);
		addRawBytesToFront(b);
	}

	public void add(double d) {
		byte[] b = new byte[8];
		doubleToBytes(d, b, 0);
		addRawBytes(b);
	}

	public void addToFront(float f) {
		byte[] b = new byte[8];
		floatToBytes(f, b, 0);
		addRawBytesToFront(b);
	}

	public void add(float f) {
		byte[] b = new byte[4];
		floatToBytes(f, b, 0);
		addRawBytes(b);
	}

	public void addToFront(String s) {
		if (s == null) {
			addToFront(0);
		} else {
			byte[] b = s.getBytes();
			addRawBytesToFront(b);
			addToFront(b.length);
		}
	}

	public void add(String s) {
		if (s == null) {
			add(-1);
		} else {
			CharsetEncoder enc = StringTools.getCharset().newEncoder();
			enc.onMalformedInput(CodingErrorAction.IGNORE);
			byte[] ba;
			try {
				ByteBuffer bb = enc.encode(CharBuffer.wrap(s));
				bb.rewind();
				ba = new byte[bb.remaining()];
				bb.get(ba);
			} catch (CharacterCodingException e) {
				throw new RuntimeException(e);
			}
			add(ba.length);
			addRawBytes(ba);
		}
	}

	public void addToFront(boolean bool) {
		byte[] b = new byte[1];
		b[0] = (byte) ((bool) ? 1 : 0);
		addRawBytesToFront(b);
	}

	public void add(boolean bool) {
		byte[] b = new byte[1];
		b[0] = (byte) ((bool) ? 1 : 0);
		addRawBytes(b);
	}

	public static short bytesToShort(byte[] array, int offset) {
		short n = 0;
		n ^= array[offset] & 0xFF;
		n <<= 8;
		n ^= array[offset + 1] & 0xFF;
		return n;
	}

	public static void shortToBytes(short val, byte[] byteBuff, int offset) {
		byteBuff[offset + 1] = (byte) val;
		val >>= 8;
		byteBuff[offset] = (byte) val;
	}

	public static long bytesToLong(byte[] array, int offset) {
		long l = 0;
		for (int i = offset; i < offset + 8; i++) {
			l <<= 8;
			l ^= array[i] & 0xFF;
		}
		return l;
	}

	public static void longToBytes(long l, byte[] bb, int offset) {
		for (int i = 7; i > 0; i--) {
			bb[i] = (byte) l;
			l >>>= 8;
		}
		bb[0] = (byte) l;
	}

	public static int bytesToInt(byte[] array, int offset) {
		return array[offset] << 24 | (array[offset + 1] & 0xFF) << 16 | (array[offset + 2] & 0xFF) << 8
				| (array[offset + 3] & 0xFF);
	}

	public static void intToBytes(int i, byte[] byteBuff, int offset) {
		for (int t = 0; t < 4; t++) {
			byteBuff[offset + t] = (byte) (i >> (4 - (t + 1)) * 8);
		}
	}

	/**
	 * This implementation uses Double.doubleToLongBits instead of
	 * Double.doubleToRawLongBits, which translates the 0x7ff8000000000100L NaN
	 * value to the 0x7ff8000000000000L NaN Value.
	 * 
	 * Honestly, I have no idea what that might imply... but Hadoop uses this
	 * version, and that's good enough for me.
	 * 
	 * @param d
	 * @param bb
	 * @param offset
	 */
	public static void doubleToBytes(double d, byte[] bb, int offset) {
		longToBytes(Double.doubleToLongBits(d), bb, offset);
	}

	public static double bytesToDouble(byte[] array, int offset) {
		return Double.longBitsToDouble(bytesToLong(array, offset));
	}

	public static void floatToBytes(float f, byte[] bb, int offset) {
		intToBytes(Float.floatToIntBits(f), bb, offset);
	}

	public static float bytesToFloat(byte[] array, int offset) {
		return Float.intBitsToFloat(bytesToInt(array, offset));
	}

	public byte[] getByteArray() {
		int len = getInt();
		if (len == -1)
			return null;
		if (len == 0)
			return new byte[0];
		byte[] sb = remove(len);
		return sb;
	}

	public byte[] getAllBytes() {
		return remove(length());
	}

	public void reset() {
		front = null;
		back = null;
		length = 0;
	}

}
