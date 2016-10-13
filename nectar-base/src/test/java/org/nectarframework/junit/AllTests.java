package org.nectarframework.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nectarframework.junit.service.AAllServiceTests;
import org.nectarframework.junit.tools.AAllToolsTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AAllServiceTests.class,
		AAllToolsTests.class })

public class AllTests {

}
