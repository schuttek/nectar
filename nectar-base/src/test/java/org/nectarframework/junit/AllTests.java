package org.nectarframework.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TemplateServicePatternTest.class, ToolsTest.class, XmlServiceTest.class })
public class AllTests {
 
}
