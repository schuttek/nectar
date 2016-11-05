package org.nectarframework.junit.tools;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nectarframework.base.tools.RandUtils;

public class RandUtilsTest {

	@Test
	public void boundTest() {
		assertEquals(RandUtils.nextInt(0), 0);
		for (int t = 0; t < 200; t++) {
			int test1 = RandUtils.nextInt(1, 2);
			assertTrue(test1 == 1 || test1 == 2);
		}
	}
}
