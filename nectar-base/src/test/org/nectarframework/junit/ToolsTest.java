package org.nectarframework.junit;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.nectarframework.base.tools.BitMap;
import org.nectarframework.base.tools.Tuple;

public class ToolsTest {

	@Test
	public void BitMapTest() {
		assertTrue(privBitMapTest(RandomUtils.nextInt(0, 50000)));
		assertTrue(privBitMapTest(RandomUtils.nextInt(0, 50000)));
		assertTrue(privBitMapTest(RandomUtils.nextInt(0, 50000)));
		assertTrue(privBitMapTest(RandomUtils.nextInt(0, 50000)));
		assertTrue(privBitMapTest(RandomUtils.nextInt(0, 50000)));
		assertTrue(privBitMapTest(0));
		assertTrue(privBitMapTest(1));
		assertTrue(privBitMapTest(2));
		assertTrue(privBitMapTest(3));
		assertTrue(privBitMapTest(4));
		assertTrue(privBitMapTest(6));
		assertTrue(privBitMapTest(7));
		assertTrue(privBitMapTest(9));
		assertTrue(privBitMapTest(11));
		assertTrue(privBitMapTest(13));

	}

	private boolean privBitMapTest(int len) {
		boolean[] boolArray = new boolean[len];
		for (int i = 0; i < len; i++) {
			if (RandomUtils.nextFloat(0, 1) < 0.5) {
				boolArray[i] = false;
			} else {
				boolArray[i] = true;
			}
		}

		byte[] seqbitmap = BitMap.init(len);
		for (int i = 0; i < len; i++) {
			if (boolArray[i]) {
				BitMap.set(seqbitmap, i);
			}
		}

		LinkedList<Tuple<Integer, Boolean>> vb = new LinkedList<Tuple<Integer, Boolean>>();
		for (int i = 0; i < len; i++) {
			vb.add(new Tuple<Integer, Boolean>(i, boolArray[i]));
		}
		Collections.shuffle(vb);

		byte[] randbitmap = BitMap.init(len);
		for (Tuple<Integer, Boolean> tp : vb) {
			if (tp.getRight()) {
				BitMap.set(randbitmap, tp.getLeft());
			}
		}

		for (int i = 0; i < len; i++) {
			if (boolArray[i] && !BitMap.is(seqbitmap, i))
				return false;
			if (!boolArray[i] && BitMap.is(seqbitmap, i))
				return false;
			if (boolArray[i] && !BitMap.is(randbitmap, i))
				return false;
			if (!boolArray[i] && BitMap.is(randbitmap, i))
				return false;
		}

		return true;
	}

}
