package org.nectarframework.base.service.log;

import java.sql.SQLException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.sql.SqlPreparedStatement;
import org.nectarframework.base.service.sql.mysql.MysqlService;
import org.nectarframework.base.tools.StringTools;

public class MysqlLogService extends LogService {
	private MysqlService my;

	
	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		my = (MysqlService) dependency(MysqlService.class);
		return super.establishDependencies();
	}

	@Override
	public void log(LogEvent le) {
		if (!isLevelEnabled(le.getLevel())) {
			return;
		}
		SqlPreparedStatement ps = new SqlPreparedStatement();

		ps.setSql("INSERT INTO nectar_log_logger(date_ms, level, thread_hash, thread_name, message, throwable)"
				+ " VALUES (?, ?, ?, ?, ?, ?)");

		ps.setLong(1, System.currentTimeMillis());
		ps.setInt(2, le.getLevel().ordinal());
		ps.setInt(3, Thread.currentThread().hashCode());
		ps.setString(4, Thread.currentThread().getName());
		ps.setString(5, le.getMessage());
		ps.setString(6, StringTools.throwableStackTracetoString(le.getThrowable()));

		try {
			my.update(ps);
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}
	}

}
