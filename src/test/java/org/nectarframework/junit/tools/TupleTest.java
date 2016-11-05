package org.nectarframework.junit.tools;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nectarframework.base.tools.Tuple;

public class TupleTest {

	@Test
	public void tupleTest() {
		Tuple<Long, Long> tupLL = new Tuple<>(1l, 2l);
		assertEquals(tupLL.getLeft().longValue(), 1l);
		assertEquals(tupLL.getRight().longValue(), 2l);
	}
	
}
