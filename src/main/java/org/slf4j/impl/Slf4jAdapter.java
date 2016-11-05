package org.slf4j.impl;

import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.log.LogLevel;
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

	private void formatAndLog(LogLevel level, String format, Object arg1, Object arg2) {
		if (!Log.isLevelEnabled(level)) {
			return;
		}
		FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
		Log.log(level, tp.getMessage(), tp.getThrowable());
	}

	private void formatAndLog(LogLevel level, String format, Object... arguments) {
		if (!Log.isLevelEnabled(level)) {
			return;
		}
		FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
		Log.log(level, tp.getMessage(), tp.getThrowable());
	}

	
	public void debug(String arg0) {
		Log.log(LogLevel.DEBUG, arg0, null);
	}

	
	public void debug(String arg0, Object arg1) {
		formatAndLog(LogLevel.DEBUG, arg0, arg1, null);
	}

	
	public void debug(String arg0, Object[] arg1) {
		formatAndLog(LogLevel.DEBUG, arg0, arg1);
	}

	
	public void debug(String arg0, Throwable arg1) {
		Log.log(LogLevel.DEBUG, arg0, arg1);
	}

	
	public void debug(String arg0, Object arg1, Object arg2) {
		formatAndLog(LogLevel.DEBUG, arg0, arg1, arg2);
	}

	
	public void error(String arg0) {
		Log.log(LogLevel.ERROR, arg0, null);
	}

	
	public void error(String arg0, Object arg1) {
		formatAndLog(LogLevel.ERROR, arg0, arg1);
	}

	
	public void error(String arg0, Object[] arg1) {
		formatAndLog(LogLevel.ERROR, arg0, arg1);

	}

	
	public void error(String arg0, Throwable arg1) {
		Log.log(LogLevel.ERROR, arg0, arg1);
	}

	
	public void error(String arg0, Object arg1, Object arg2) {
		formatAndLog(LogLevel.ERROR, arg0, arg1, arg2);
	}

	
	public void info(String arg0) {
		Log.log(LogLevel.INFO, arg0, null);
	}

	
	public void info(String arg0, Object arg1) {
		formatAndLog(LogLevel.INFO, arg0, arg1);

	}

	
	public void info(String arg0, Object[] arg1) {
		formatAndLog(LogLevel.INFO, arg0, arg1);

	}

	
	public void info(String arg0, Throwable arg1) {
		Log.log(LogLevel.INFO, arg0, arg1);

	}

	
	public void info(String arg0, Object arg1, Object arg2) {
		formatAndLog(LogLevel.INFO, arg0, arg1, arg2);

	}

	
	public boolean isDebugEnabled() {
		return Log.isLevelEnabled(LogLevel.DEBUG);
	}

	
	public boolean isErrorEnabled() {
		return Log.isLevelEnabled(LogLevel.ERROR);
	}

	
	public boolean isInfoEnabled() {
		return Log.isLevelEnabled(LogLevel.INFO);
	}

	
	public boolean isTraceEnabled() {
		return Log.isLevelEnabled(LogLevel.TRACE);
	}

	
	public boolean isWarnEnabled() {
		return Log.isLevelEnabled(LogLevel.WARN);
	}

	
	public void trace(String arg0) {
		Log.log(LogLevel.TRACE, arg0, null);
	}

	
	public void trace(String arg0, Object arg1) {
		formatAndLog(LogLevel.TRACE, arg0, arg1);
	}

	
	public void trace(String arg0, Object[] arg1) {
		formatAndLog(LogLevel.TRACE, arg0, arg1);
	}

	
	public void trace(String arg0, Throwable arg1) {
		Log.log(LogLevel.TRACE, arg0, arg1);
	}

	
	public void trace(String arg0, Object arg1, Object arg2) {
		formatAndLog(LogLevel.TRACE, arg0, arg1, arg2);
	}

	
	public void warn(String arg0) {
		Log.log(LogLevel.WARN, arg0, null);
	}

	
	public void warn(String arg0, Object arg1) {
		formatAndLog(LogLevel.WARN, arg0, arg1);
	}

	
	public void warn(String arg0, Object[] arg1) {
		formatAndLog(LogLevel.WARN, arg0, arg1);
	}

	
	public void warn(String arg0, Throwable arg1) {
		Log.log(LogLevel.WARN, arg0, arg1);
	}

	
	public void warn(String arg0, Object arg1, Object arg2) {
		formatAndLog(LogLevel.WARN, arg0, arg1, arg2);
	}

}
