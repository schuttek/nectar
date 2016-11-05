package org.nectarframework.base.service.sql;

import org.nectarframework.base.service.thread.ThreadServiceTask;

public class OpenRemainingConnectionsTask extends ThreadServiceTask {
	private SqlService mysqlService;
	private int amount;
	
	
	public OpenRemainingConnectionsTask(SqlService mysqlService, int amount) {
		this.mysqlService = mysqlService;
		this.amount = amount;
	}

	@Override
	public void execute() throws Exception {
		mysqlService.openConnections(amount);
	}

}
