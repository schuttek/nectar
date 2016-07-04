package org.nectarframework.base.service.cache;

import org.nectarframework.base.service.thread.ThreadServiceTask;

public class CheckFlushTimerTask extends ThreadServiceTask {

	private CacheService cacheService;

	public CheckFlushTimerTask(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@Override
	public void execute() throws Exception {
		cacheService.checkFlushTimerTaskCallback();
	}

}
