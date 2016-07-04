package org.nectarframework.base.service.mysql;

import org.nectarframework.base.service.thread.ThreadServiceTask;

public class OpenRemainingConnectionsTask extends ThreadServiceTask {
	private DatabaseService mysqlService;
	private int amount;
	
	
	public OpenRemainingConnectionsTask(DatabaseService mysqlService, int amount) {
		this.mysqlService = mysqlService;
		this.amount = amount;
	}

	@Override
	public void execute() throws Exception {
		mysqlService.openConnections(amount);
	}

}
