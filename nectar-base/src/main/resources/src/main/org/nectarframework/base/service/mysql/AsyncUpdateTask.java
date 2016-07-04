package org.nectarframework.base.service.mysql;

import org.nectarframework.base.service.thread.ThreadServiceTask;

public class AsyncUpdateTask extends ThreadServiceTask {

	private MysqlPreparedStatement ps;
	private AsyncTicket at;
	private DatabaseService mysqlService;

	public AsyncUpdateTask(DatabaseService mysqlService, MysqlPreparedStatement ps, AsyncTicket at) {
		this.mysqlService = mysqlService;
		this.ps = ps;
		this.at = at;
	}

	@Override
	public void execute() throws Exception {
		int rows = mysqlService.update(ps);
		at.setRowCount(rows);
		at.setReady(true);
		synchronized(at) {
			at.notifyAll();
		}
	}
}
