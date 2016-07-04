package org.nectarframework.base.service.mysql;

import java.sql.SQLException;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadServiceTask;

public class DelayedInsertTask extends ThreadServiceTask {

	private MysqlPreparedStatement ps;
	private DatabaseService mysqlService;

	public DelayedInsertTask(DatabaseService mysqlService, MysqlPreparedStatement ps) {
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
