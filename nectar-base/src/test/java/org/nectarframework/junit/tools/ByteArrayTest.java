package org.nectarframework.junit.tools;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Test;
import org.nectarframework.base.tools.ByteArray;
import org.nectarframework.base.tools.RandUtils;
import org.nectarframework.base.tools.StringTools;
import org.nectarframework.base.tools.Tuple;

public class ByteArrayTest {

	Random rand = new Random();

	private static final int randIterations = 20;

	private boolean loadUnloadDouble(double d) {
		ByteArray ba = new ByteArray();
		ba.add(d);
		return (d == ba.getDouble()) ? true : false;
	}

	@Test
	public void doubleTest() {
		for (int t = 0; t < randIterations; t++) {
			assertTrue(loadUnloadDouble(rand.nextDouble()));
		}
		assertTrue(loadUnloadDouble(1.0d));
		assertTrue(loadUnloadDouble(0.0d));
		assertTrue(loadUnloadDouble(-1.0d));
		assertTrue(loadUnloadDouble(Double.MAX_VALUE));
		assertTrue(loadUnloadDouble(Double.MIN_VALUE));
		assertTrue(loadUnloadDouble(-Double.MIN_VALUE));
		// comparing NaN to NaN SHOULD be false!
		assertFalse(loadUnloadDouble(Double.NaN));
		assertFalse(loadUnloadDouble(-Double.NaN));
		assertTrue(loadUnloadDouble(Double.NEGATIVE_INFINITY));
		assertTrue(loadUnloadDouble(Double.POSITIVE_INFINITY));
	}

	private boolean loadUnloadFloat(float d) {
		ByteArray ba = new ByteArray();
		ba.add(d);
		return (d == ba.getFloat()) ? true : false;
	}

	@Test
	public void floatTest() {
		for (int t = 0; t < randIterations; t++) {
			assertTrue(loadUnloadFloat(rand.nextFloat()));
		}
		assertTrue(loadUnloadFloat(1.0f));
		assertTrue(loadUnloadFloat(0.0f));
		assertTrue(loadUnloadFloat(-1.0f));
		assertTrue(loadUnloadFloat(Float.MAX_VALUE));
		assertTrue(loadUnloadFloat(Float.MIN_VALUE));
		assertTrue(loadUnloadFloat(-Float.MIN_VALUE));
		// comparing NaN to NaN SHOULD be false!
		assertFalse(loadUnloadFloat(Float.NaN));
		assertFalse(loadUnloadFloat(-Float.NaN));
		assertTrue(loadUnloadFloat(Float.NEGATIVE_INFINITY));
		assertTrue(loadUnloadFloat(Float.POSITIVE_INFINITY));
	}

	private boolean loadUnloadLong(long d) {
		ByteArray ba = new ByteArray();
		ba.add(d);
		return (d == ba.getLong()) ? true : false;
	}

	@Test
	public void longTest() {
		for (int t = 0; t < randIterations; t++) {
			assertTrue(loadUnloadLong(rand.nextLong()));
		}
		assertTrue(loadUnloadLong(1));
		assertTrue(loadUnloadLong(0));
		assertTrue(loadUnloadLong(-1));
		assertTrue(loadUnloadLong(Long.MAX_VALUE));
		assertTrue(loadUnloadLong(Long.MIN_VALUE));
	}

	private boolean loadUnloadInt(int d) {
		ByteArray ba = new ByteArray();
		ba.add(d);
		return (d == ba.getInt()) ? true : false;
	}

	@Test
	public void intTest() {
		for (int t = 0; t < randIterations; t++) {
			assertTrue(loadUnloadLong(rand.nextInt()));
		}
		assertTrue(loadUnloadInt(1));
		assertTrue(loadUnloadInt(0));
		assertTrue(loadUnloadInt(-1));
		assertTrue(loadUnloadInt(Integer.MAX_VALUE));
		assertTrue(loadUnloadInt(Integer.MIN_VALUE));
	}

	private boolean loadUnloadShort(short d) {
		ByteArray ba = new ByteArray();
		ba.add(d);
		return (d == ba.getShort()) ? true : false;
	}

	@Test
	public void shortTest() {
		for (int t = 0; t < randIterations; t++) {
			assertTrue(loadUnloadShort(new Integer(rand.nextInt()).shortValue()));
		}
		assertTrue(loadUnloadShort((short) 1));
		assertTrue(loadUnloadShort((short) 0));
		assertTrue(loadUnloadShort((short) -1));
		assertTrue(loadUnloadShort(Short.MAX_VALUE));
		assertTrue(loadUnloadShort(Short.MIN_VALUE));
	}

	private boolean loadUnloadByte(byte d) {
		ByteArray ba = new ByteArray();
		ba.add(d);
		return (d == ba.getByte()) ? true : false;
	}

	@Test
	public void byteTest() {
		for (int t = 0; t < randIterations; t++) {
			assertTrue(loadUnloadLong(rand.nextLong()));
		}
		assertTrue(loadUnloadByte((byte) 1));
		assertTrue(loadUnloadByte((byte) 0));
		assertTrue(loadUnloadByte((byte) -1));
		assertTrue(loadUnloadByte(Byte.MAX_VALUE));
		assertTrue(loadUnloadByte(Byte.MIN_VALUE));
	}

	@Test
	public void charEncoderTest() throws CharacterCodingException {
		String s = RandUtils.nextUTF8String(500, 500);
		CharsetEncoder enc = StringTools.getCharset().newEncoder();
		CharsetDecoder dec = StringTools.getCharset().newDecoder();
		CharBuffer cb = CharBuffer.wrap(s);
		ByteBuffer bb = enc.encode(CharBuffer.wrap(s));
		bb.rewind();
		byte[] ba = new byte[bb.remaining()];
		bb.get(ba);
		CharBuffer dcb = dec.decode(ByteBuffer.wrap(ba));
		assertEquals(cb, dcb);
		assertEquals(cb.toString(), dcb.toString());
	}

	private void loadUnloadString(String s) {
		ByteArray ba = new ByteArray();
		ba.add(s);
		String testString = ba.getString();
		if (s == null) {
			assertTrue(testString == null);
		} else {
			// Log.trace("original: "+s.length());
			// Log.trace("test: "+testString.length());
			assertTrue(s.equals(testString));
		}
	}

	@Test
	public void stringTest() {
		loadUnloadString(new String());
		loadUnloadString(null);
		for (int t = 0; t < randIterations; t++) {
			loadUnloadString(RandUtils.nextUTF8String(50, 50));
		}
	}

	private void loadUnloadByteArray(byte[] bb) {
		ByteArray ba = new ByteArray();
		ba.addByteArray(bb);
		assertArrayEquals(bb, ba.getByteArray());
	}

	@Test
	public void byteArrayTest() {
		loadUnloadByteArray(new byte[0]);
		loadUnloadByteArray(null);
		for (int t = 0; t < randIterations; t++) {
			loadUnloadByteArray(RandUtils.nextByteArray(500));
		}
		for (byte t = Byte.MIN_VALUE; t < Byte.MAX_VALUE; t++) {
			byte[] bq = new byte[1];
			bq[0] = t;
			loadUnloadByteArray(bq);
		}
	}

	@Test
	public void complexTest() {
		final int runTimes = 50;
		final int elements = 50;

		for (int r = 0; r < runTimes; r++) {
			ByteArray ba = new ByteArray();
			LinkedList<Tuple<Integer, Object>> list = new LinkedList<>();
			for (int e = 0; e < elements; e++) {
				int type = RandUtils.nextInt(9);
				switch (type) {
				case 0:
					double dvalue = rand.nextDouble();
					ba.add(dvalue);
					list.add(new Tuple<Integer, Object>(type, dvalue));
					break;
				case 1:
					float fvalue = rand.nextFloat();
					ba.add(fvalue);
					list.add(new Tuple<Integer, Object>(type, fvalue));
					break;
				case 2:
					long lvalue = rand.nextLong();
					ba.add(lvalue);
					list.add(new Tuple<Integer, Object>(type, lvalue));
					break;
				case 3:
					int ivalue = rand.nextInt();
					ba.add(ivalue);
					list.add(new Tuple<Integer, Object>(type, ivalue));
					break;
				case 4:
					short svalue = (short) rand.nextInt();
					ba.add(svalue);
					list.add(new Tuple<Integer, Object>(type, svalue));
					break;
				case 5:
					byte bvalue = (byte) rand.nextInt();
					ba.add(bvalue);
					list.add(new Tuple<Integer, Object>(type, bvalue));
					break;
				case 6:
					boolean ovalue = rand.nextBoolean();
					ba.add(ovalue);
					list.add(new Tuple<Integer, Object>(type, ovalue));
					break;
				case 7:
					String tvalue = RandUtils.nextUTF8String(1000, 2000);
					ba.add(tvalue);
					list.add(new Tuple<Integer, Object>(type, tvalue));
					break;
				case 8:
					byte[] avalue = RandUtils.nextByteArray(RandUtils.nextInt(1000, 2000));
					ba.addByteArray(avalue);
					list.add(new Tuple<Integer, Object>(type, avalue));
					break;
				}
			}

			assertEquals(list.size(), elements);

			for (int e = 0; e < elements; e++) {
				Tuple<Integer, Object> t = list.get(e);
				int type = t.getLeft();
				switch (type) {
				case 0:
					double dvalue = (double) t.getRight();
					double dactual = ba.getDouble();
					assertEquals(dvalue, dactual, 0.0);
					break;
				case 1:
					float fvalue = (float) t.getRight();
					float factual = ba.getFloat();
					assertEquals(fvalue, factual, 0.0);
					break;
				case 2:
					long lvalue = (long) t.getRight();
					long lactual = ba.getLong();
					assertEquals(lvalue, lactual);
					break;
				case 3:
					int ivalue = (int) t.getRight();
					int iactual = ba.getInt();
					assertEquals(ivalue, iactual);
					break;
				case 4:
					short svalue = (short) t.getRight();
					short sactual = ba.getShort();
					assertEquals(svalue, sactual);
					break;
				case 5:
					byte bvalue = (byte) t.getRight();
					byte bactual = ba.getByte();
					assertEquals(bvalue, bactual);
					break;
				case 6:
					boolean ovalue = (Boolean) t.getRight();
					boolean oactual = ba.getBoolean();
					assertEquals(ovalue, oactual);
					break;
				case 7:
					String tvalue = (String) t.getRight();
					String tactual = ba.getString();
					assertEquals(tvalue, tactual);
					break;
				case 8:
					byte[] avalue = (byte[]) t.getRight();
					byte[] aactual = ba.getByteArray();
					assertArrayEquals(avalue, aactual);
					break;
				}
			}
		}
	}

}
