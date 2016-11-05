package org.nectarframework.junit.service;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.*;
import org.nectarframework.base.Main;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.cache.HashMapCacheService;
import org.nectarframework.base.service.log.LogLevel;

public class HashMapCacheServiceTest {
	
	@BeforeClass
	public static void setup() throws FileNotFoundException, ConfigurationException {
		Main.runNectar("config/junitTest.xml", "unitTest", "hashMapCacheService", LogLevel.TRACE);
	}
	@AfterClass
	public static void breakDown() {
		Main.endNectar();
	}
	
	@Test
	public void basicTest() throws FileNotFoundException {
		Log.info("test is running");
		
		HashMapCacheService cs = ServiceRegister.getService(HashMapCacheService.class);
		// should be empty
		assertNull(cs.getObject("void"));
	}
	
}
