package org.nectarframework.junit.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TemplateServicePatternTest.class,
	XmlServiceTest.class })

public class AAllServiceTests {

}