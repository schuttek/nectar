package org.nectarframework.junit.tools;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BitmapTest.class, ByteArrayTest.class, RandUtilsTest.class, TripleTest.class, TupleTest.class })

public class AAllToolsTests {

}