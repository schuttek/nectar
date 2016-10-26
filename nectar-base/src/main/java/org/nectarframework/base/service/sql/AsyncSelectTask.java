package org.nectarframework.base.service.sql;

import java.sql.SQLException;

import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.thread.ThreadServiceTask;

public class AsyncSelectTask extends ThreadServiceTask {

	private SqlPreparedStatement mps;
	private AsyncTicket at;
	private long expiry;
	private SqlService mysqlService;

	public AsyncSelectTask(SqlService mysqlService, String sql, AsyncTicket at) {
		this.mysqlService = mysqlService;
		this.mps = new SqlPreparedStatement(sql);
		this.at = at;
		this.expiry = 0;
	}

	public AsyncSelectTask(SqlService mysqlService, SqlPreparedStatement mps, AsyncTicket at) {
		this.mysqlService = mysqlService;
		this.mps = mps;
		this.at = at;
		this.expiry = 0;
	}

	public AsyncSelectTask(SqlService mysqlService, String sql, AsyncTicket at, long expiry) {
		this.mysqlService = mysqlService;
		this.mps = new SqlPreparedStatement(sql);
		this.at = at;
		this.expiry = expiry;
	}

	public AsyncSelectTask(SqlService mysqlService, SqlPreparedStatement mps, AsyncTicket at, long expiry) {
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
