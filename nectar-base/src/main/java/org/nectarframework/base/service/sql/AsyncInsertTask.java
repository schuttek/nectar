package org.nectarframework.base.service.sql;

import java.util.Vector;

import org.nectarframework.base.service.thread.ThreadServiceTask;

public class AsyncInsertTask extends ThreadServiceTask {

	private SqlPreparedStatement ps;
	private AsyncTicket at;
	private SqlService mysqlService;

	public AsyncInsertTask(SqlService mysqlService, SqlPreparedStatement ps, AsyncTicket at) {
		this.mysqlService = mysqlService;
		this.ps = ps;
		this.at = at;
	}

	@Override
	public void execute() throws Exception {
		Vector<Long> ids = mysqlService.insert(ps);
		at.setInsertIds(ids);
		at.setReady(true);
		synchronized(at) {
			at.notifyAll();
		}
	}
}
