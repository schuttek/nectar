package org.nectarframework.base.service.log;

import org.nectarframework.base.service.event.EventChannel;
import org.nectarframework.base.service.event.NectarEvent;

public class LogEvent implements NectarEvent {
	private long timestamp;
	private LogLevel level;
	private String message;
	private Throwable throwable;
	private String className;
	private String methodName;
	private int lineNumber;

	
	public LogEvent(long timestamp, LogLevel level, String msg, Throwable t, String className, String methodName,
			int lineNumber) {
		this.timestamp = timestamp;
		this.level = level;
		this.message = msg;
		this.throwable = t;
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
	}

	public LogEvent(long timestamp, LogLevel level, String msg, Throwable t, StackTraceElement[] stackTrace) {
		this.timestamp = timestamp;
		this.level = level;
		this.message = msg;
		this.throwable = t;
		if (stackTrace == null) {
			this.className = null;
			this.methodName = null;
			this.lineNumber = 0;
		} else if (stackTrace.length < 3) {
			throw new IllegalArgumentException();
		} else {
			this.className = stackTrace[2].getClassName();
			this.methodName = stackTrace[2].getMethodName();
			this.lineNumber = stackTrace[2].getLineNumber();
		}
	}

	public long getTimestamp() {
		return timestamp;
	}

	public LogLevel getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public EventChannel getChannel() {
		return LogService.eventChannel;
	}

}
