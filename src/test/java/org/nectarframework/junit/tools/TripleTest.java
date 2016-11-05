package org.nectarframework.junit.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nectarframework.base.tools.Triple;

public class TripleTest {

	@Test
	public void tripleTest() {
		Triple<Long, Long, Long> t = new Triple<>(1l, 2l, 3l);
		assertEquals(t.getLeft().longValue(), 1l);
		assertEquals(t.getMiddle().longValue(), 2l);
		assertEquals(t.getRight().longValue(), 3l);
	}
}
