package org.nectarframework.base.service.sql;

import org.nectarframework.base.service.thread.ThreadServiceTask;

public class AsyncUpdateTask extends ThreadServiceTask {

	private SqlPreparedStatement ps;
	private AsyncTicket at;
	private SqlService mysqlService;

	public AsyncUpdateTask(SqlService mysqlService, SqlPreparedStatement ps, AsyncTicket at) {
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
