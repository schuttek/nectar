package org.slf4j.impl;

import org.nectarframework.base.service.log.Log;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

public class Slf4jAdapter extends MarkerIgnoringBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1907982825945770773L;

	public Slf4jAdapter(String name) {
	}

	private void formatAndLog(Log.Level level, String format, Object arg1, Object arg2) {
		if (!Log.isLevelEnabled(level)) {
			return;
		}
		FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
		Log.log(level, tp.getMessage(), tp.getThrowable());
	}

	private void formatAndLog(Log.Level level, String format, Object... arguments) {
		if (!Log.isLevelEnabled(level)) {
			return;
		}
		FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
		Log.log(level, tp.getMessage(), tp.getThrowable());
	}

	
	public void debug(String arg0) {
		Log.log(Log.Level.DEBUG, arg0, null);
	}

	
	public void debug(String arg0, Object arg1) {
		formatAndLog(Log.Level.DEBUG, arg0, arg1, null);
	}

	
	public void debug(String arg0, Object[] arg1) {
		formatAndLog(Log.Level.DEBUG, arg0, arg1);
	}

	
	public void debug(String arg0, Throwable arg1) {
		Log.log(Log.Level.DEBUG, arg0, arg1);
	}

	
	public void debug(String arg0, Object arg1, Object arg2) {
		formatAndLog(Log.Level.DEBUG, arg0, arg1, arg2);
	}

	
	public void error(String arg0) {
		Log.log(Log.Level.ERROR, arg0, null);
	}

	
	public void error(String arg0, Object arg1) {
		formatAndLog(Log.Level.ERROR, arg0, arg1);
	}

	
	public void error(String arg0, Object[] arg1) {
		formatAndLog(Log.Level.ERROR, arg0, arg1);

	}

	
	public void error(String arg0, Throwable arg1) {
		Log.log(Log.Level.ERROR, arg0, arg1);
	}

	
	public void error(String arg0, Object arg1, Object arg2) {
		formatAndLog(Log.Level.ERROR, arg0, arg1, arg2);
	}

	
	public void info(String arg0) {
		Log.log(Log.Level.INFO, arg0, null);
	}

	
	public void info(String arg0, Object arg1) {
		formatAndLog(Log.Level.INFO, arg0, arg1);

	}

	
	public void info(String arg0, Object[] arg1) {
		formatAndLog(Log.Level.INFO, arg0, arg1);

	}

	
	public void info(String arg0, Throwable arg1) {
		Log.log(Log.Level.INFO, arg0, arg1);

	}

	
	public void info(String arg0, Object arg1, Object arg2) {
		formatAndLog(Log.Level.INFO, arg0, arg1, arg2);

	}

	
	public boolean isDebugEnabled() {
		return Log.isLevelEnabled(Log.Level.DEBUG);
	}

	
	public boolean isErrorEnabled() {
		return Log.isLevelEnabled(Log.Level.ERROR);
	}

	
	public boolean isInfoEnabled() {
		return Log.isLevelEnabled(Log.Level.INFO);
	}

	
	public boolean isTraceEnabled() {
		return Log.isLevelEnabled(Log.Level.TRACE);
	}

	
	public boolean isWarnEnabled() {
		return Log.isLevelEnabled(Log.Level.WARN);
	}

	
	public void trace(String arg0) {
		Log.log(Log.Level.TRACE, arg0, null);
	}

	
	public void trace(String arg0, Object arg1) {
		formatAndLog(Log.Level.TRACE, arg0, arg1);
	}

	
	public void trace(String arg0, Object[] arg1) {
		formatAndLog(Log.Level.TRACE, arg0, arg1);
	}

	
	public void trace(String arg0, Throwable arg1) {
		Log.log(Log.Level.TRACE, arg0, arg1);
	}

	
	public void trace(String arg0, Object arg1, Object arg2) {
		formatAndLog(Log.Level.TRACE, arg0, arg1, arg2);
	}

	
	public void warn(String arg0) {
		Log.log(Log.Level.WARN, arg0, null);
	}

	
	public void warn(String arg0, Object arg1) {
		formatAndLog(Log.Level.WARN, arg0, arg1);
	}

	
	public void warn(String arg0, Object[] arg1) {
		formatAndLog(Log.Level.WARN, arg0, arg1);
	}

	
	public void warn(String arg0, Throwable arg1) {
		Log.log(Log.Level.WARN, arg0, arg1);
	}

	
	public void warn(String arg0, Object arg1, Object arg2) {
		formatAndLog(Log.Level.WARN, arg0, arg1, arg2);
	}

}
