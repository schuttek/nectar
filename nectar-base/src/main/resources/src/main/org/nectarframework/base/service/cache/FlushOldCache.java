package org.nectarframework.base.service.cache;

import org.nectarframework.base.service.thread.ThreadServiceTask;

public class FlushOldCache extends ThreadServiceTask {
private CacheService cacheService;
	public FlushOldCache(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@Override
	public void execute() throws Exception {
		cacheService.flushOldCache();
	}

}
