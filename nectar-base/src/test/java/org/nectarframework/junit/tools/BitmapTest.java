package org.nectarframework.junit.tools;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.nectarframework.base.tools.BitMap;
import org.nectarframework.base.tools.ByteArray;
import org.nectarframework.base.tools.RandUtils;
import org.nectarframework.base.tools.Tuple;

public class BitmapTest {

	@Test
	public void constTest() {
		BitMap m = new BitMap();
		assertTrue(m.size() == 0);
		m = new BitMap(20);
		assertTrue(m.size() == 20);
		byte[] b = RandUtils.nextByteArray(50);
		m = new BitMap(b, 50 * 8);
		assertTrue(m.size() == 50 * 8);
		assertTrue(Arrays.equals(m.map(), b));
		assertFalse(m.map() == b);
		assertNotEquals(m.map(), b);
	}

	@Test
	public void readWriteTest() {
		for (int t = 0; t < 10; t++)
			privBitMapTest(RandomUtils.nextInt(0, 50000));
		for (int t = 0; t < 64; t++)
			privBitMapTest(t);
	}

	@Test
	public void invertTest() {
		byte[] b = RandUtils.nextByteArray(50);
		BitMap pos = new BitMap(b, 50 * 8);
		BitMap neg = new BitMap(b, 50 * 8);
		assertTrue(pos.equals(neg));
		for (int t = 0; t < 50 * 8; t++) {
			if (pos.is(t)) {
				neg.clear(t);
			} else {
				neg.set(t);
			}
		}
		assertFalse(pos.equals(neg));
		for (int t = 0; t < 50 * 8; t++) {
			assertNotEquals(pos.is(t), neg.is(t));
		}
	}

	private void privBitMapTest(int len) {
		boolean[] boolArray = new boolean[len];
		for (int i = 0; i < len; i++) {
			if (RandomUtils.nextFloat(0, 1) < 0.5) {
				boolArray[i] = false;
			} else {
				boolArray[i] = true;
			}
		}

		BitMap seqbitmap = new BitMap(len);
		assertEquals(seqbitmap.size(), len);
		for (int i = 0; i < len; i++) {
			if (boolArray[i]) {
				seqbitmap.set(i);
			}
		}

		LinkedList<Tuple<Integer, Boolean>> vb = new LinkedList<Tuple<Integer, Boolean>>();
		for (int i = 0; i < len; i++) {
			vb.add(new Tuple<Integer, Boolean>(i, boolArray[i]));
		}
		Collections.shuffle(vb);

		BitMap randbitmap = new BitMap(len);
		for (Tuple<Integer, Boolean> tp : vb) {
			if (tp.getRight()) {
				randbitmap.set(tp.getLeft());
			}
		}

		for (int i = 0; i < len; i++) {
			assertNotEquals(boolArray[i], !seqbitmap.is(i));
			assertNotEquals(!boolArray[i], seqbitmap.is(i));
			assertNotEquals(boolArray[i], !seqbitmap.is(i));
			assertEquals(boolArray[i], seqbitmap.is(i));
		}
	}

	@Test
	public void byteArrayTest() {
		byte[] b = RandUtils.nextByteArray(50);
		BitMap map1 = new BitMap(b, 50 * 8);
		BitMap map2 = new BitMap(b, 50 * 8);
		assertArrayEquals(map1.map(), map2.map());
		ByteArray ba1 = map1.toBytes(new ByteArray());
		ByteArray ba2 = map2.toBytes(new ByteArray());
		byte[] ba1b = ba1.getAllBytes();
		byte[] ba2b = ba2.getAllBytes();
		assertArrayEquals(ba1b, ba2b);
		ByteArray ba1copy = new ByteArray(ba1b);
		ByteArray ba2copy = new ByteArray(ba2b);
		map1 = new BitMap().fromBytes(ba1copy);
		map2 = new BitMap().fromBytes(ba2copy);
		assertArrayEquals(map1.map(), map2.map());
	}

}
