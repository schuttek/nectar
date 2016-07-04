package org.nectarframework.base.service.mysql;

import java.sql.SQLException;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadServiceTask;

public class AsyncSelectTask extends ThreadServiceTask {

	private MysqlPreparedStatement mps;
	private AsyncTicket at;
	private long expiry;
	private DatabaseService mysqlService;

	public AsyncSelectTask(DatabaseService mysqlService, String sql, AsyncTicket at) {
		this.mysqlService = mysqlService;
		this.mps = new MysqlPreparedStatement(sql);
		this.at = at;
		this.expiry = 0;
	}

	public AsyncSelectTask(DatabaseService mysqlService, MysqlPreparedStatement mps, AsyncTicket at) {
		this.mysqlService = mysqlService;
		this.mps = mps;
		this.at = at;
		this.expiry = 0;
	}

	public AsyncSelectTask(DatabaseService mysqlService, String sql, AsyncTicket at, long expiry) {
		this.mysqlService = mysqlService;
		this.mps = new MysqlPreparedStatement(sql);
		this.at = at;
		this.expiry = expiry;
	}

	public AsyncSelectTask(DatabaseService mysqlService, MysqlPreparedStatement mps, AsyncTicket at, long expiry) {
		this.mysqlService = mysqlService;
		this.mps = mps;
		this.at = at;
		this.expiry = expiry;
	}

	@Override
	public void execute() throws Exception {
		ResultTable rt;
		try {
			if (expiry > 0) {
				rt = mysqlService.select(mps, expiry);
			} else {
				rt = mysqlService.select(mps);
			}
			at.setResultTable(rt);
		} catch (SQLException e) {
			Log.warn(mps.getSql(), e);
		}
		at.setReady(true);
		synchronized (at) {
			at.notifyAll();
		}
	}
}
