package org.nectarframework.base.service.sql;

import java.sql.SQLException;

import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.thread.ThreadServiceTask;

public class DelayedInsertTask extends ThreadServiceTask {

	private SqlPreparedStatement ps;
	private SqlService mysqlService;

	public DelayedInsertTask(SqlService mysqlService, SqlPreparedStatement ps) {
		this.mysqlService = mysqlService;
		this.ps = ps;
	}

	@Override
	public void execute() throws Exception {
		try {
			mysqlService.insert(ps);
		} catch (SQLException e) {
			Log.warn(ps.getSql(), e);
		}
	}
}
