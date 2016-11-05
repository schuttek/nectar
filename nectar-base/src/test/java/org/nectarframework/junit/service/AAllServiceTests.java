package org.nectarframework.junit.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TemplateServicePatternTest.class,
	XmlServiceTest.class, HashMapCacheServiceTest.class })

public class AAllServiceTests {

}