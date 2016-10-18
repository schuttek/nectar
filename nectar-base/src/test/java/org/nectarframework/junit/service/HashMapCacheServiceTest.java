package org.nectarframework.junit.service;

import org.junit.*;
import org.nectarframework.base.Main;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.cache.HashMapCacheService;
import org.nectarframework.base.service.log.Log.Level;

public class HashMapCacheServiceTest {
	@BeforeClass
	public void serviceSetup() {
		Main.runNectar("config/junitTest.xml", "unitTest", "hashMapCacheService", Level.WARN);
	}
	
	@AfterClass
	public void serviceShutdown() {
		Main.exit();
	}
	
	@Test
	public void basicTest() {
		HashMapCacheService cs = ServiceRegister.getService(HashMapCacheService.class);
	}
	
}
