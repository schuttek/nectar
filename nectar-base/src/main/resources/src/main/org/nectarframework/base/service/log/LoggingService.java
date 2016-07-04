package org.nectarframework.base.service.log;

import java.io.ByteArrayOutputStream;

import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.log.Log.Level;
import org.nectarframework.base.service.mysql.MysqlPreparedStatement;
import org.nectarframework.base.service.mysql.MysqlService;
import org.nectarframework.base.service.session.Session;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;

public class LoggingService extends Service {

	private Log.Level level = Log.Level.TRACE;
	private Log.Level mysqlLogLevel = Log.Level.WARN;

	@Override
	protected boolean init() {
		Log.setLoggingService(this);
		return true;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		Log.setLoggingService(null);
		return true;
	}

	@Override
	public void checkParameters() throws ConfigurationException {
		String logLevelStr = this.serviceParameters.getValue("logLevel");
		if (logLevelStr == "trace") {
			level = Level.TRACE;
		} else if (logLevelStr == "debug") {
			level = Level.DEBUG;
		} else if (logLevelStr == "info") {
			level = Level.INFO;
		} else if (logLevelStr == "warn") {
			level = Level.WARN;
		} else if (logLevelStr == "fatal") {
			level = Level.FATAL;
		} else if (logLevelStr == null) {
			level = Level.TRACE;
		} else {
			Log.warn("LoggingService configuration had an unrecognized parameter value for 'logLevel'.");
		}
	}

	@Override
	public boolean establishDependancies() {
		return true;
	}

	public void log(LogEvent le) {
		printLogEvent(level, System.err, le);
		logToMysql(mysqlLogLevel, le);
	}

	public boolean isLevelEnabled(Level trace) {
		return level.compareTo(level) >= 0;
	}

	private static void printLogEvent(Level level, PrintStream out, LogEvent le) {
		if (le.getLevel().compareTo(level) >= 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss]SSS");
			out.println(sdf.format(new Date(le.getTimestamp())) + " <" + le.getLevel().name() + "> " + le.getMessage());
			if (le.getThrowable() != null) {
				le.getThrowable().printStackTrace(out);
			}
		}
	}

	public static String throwableStackTracetoString(Throwable t) {
		if (t == null) {
			return null;
		}
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(buff);
		t.printStackTrace(ps);
		ps.flush();
		ps.close();
		return new String(buff.toByteArray());
	}

	private static void logToMysql(Level mysqlLogLevel, LogEvent le) {
		if (le.getLevel().compareTo(mysqlLogLevel) < 0) {
			return;
		}
		// TODO: buffering, async...
		MysqlService my = (MysqlService) ServiceRegister.getServiceByClassName("nectar.base.service.mysql.MysqlService");
		if (my == null) {
			return;
		}

		MysqlPreparedStatement ps = new MysqlPreparedStatement();

		ps.setSql("INSERT INTO nectar_log_logger(date_ms, level, thread_hash, thread_name, message, throwable)" + " VALUES (?, ?, ?, ?, ?, ?)");

		ps.setLong(1, System.currentTimeMillis());
		ps.setInt(2, le.getLevel().ordinal());
		ps.setInt(3, Thread.currentThread().hashCode());
		ps.setString(4, Thread.currentThread().getName());
		ps.setString(5, le.getMessage());
		ps.setString(6, throwableStackTracetoString(le.getThrowable()));

		try {
			my.update(ps);
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}

	}

	
	public void accessLog(String path, Element rawForm, Element validated, Element output, long duration, String remoteIp, Session session) {
		// TODO: buffering, async...

		XmlService xs = (XmlService) ServiceRegister.getServiceByClassName(XmlService.class.getName());
		
		String sql = "INSERT INTO nectar_log_access SET dateMs = ?, path = ?, formRaw = ?, formValid = ?, outputElm = ?, duration = ?, remoteIp = ?, session = ?";
		
		
		MysqlPreparedStatement ps = new MysqlPreparedStatement(sql);

		ps.setLong(1, System.currentTimeMillis());
		ps.setString(2, path);
		if (rawForm != null) {
			ps.setString(3, xs.toXmlString(rawForm).toString());
		} else {
			ps.setNull(3);
		}
		if (validated != null) {
		ps.setString(4, xs.toXmlString(validated).toString());
		} else {
			ps.setNull(4);
		}
		if (output != null) {
			ps.setString(5, xs.toXmlString(output).toString());
		} else {
			ps.setNull(5);
		}
		ps.setLong(6, duration);
		ps.setString(7, remoteIp);
		if (session != null) {
			ps.setString(8, session.toString());
		} else {
			ps.setNull(8);
		}

		MysqlService my = (MysqlService) ServiceRegister.getServiceByClassName("nectar.base.service.mysql.MysqlService");

		my.asyncUpdate(ps);
	}
}
