package org.nectarframework.base.service.log;

import org.nectarframework.base.service.event.EventChannel;
import org.nectarframework.base.service.event.NectarEvent;

public class LogEvent implements NectarEvent {
	private final long timestamp;
	private final LogLevel level;
	private final String message;
	private final Throwable throwable;
	private final String className;
	private final String methodName;
	private final int lineNumber;
	private final String threadName;

	protected LogEvent(long timestamp, LogLevel level, String msg, Throwable t, String className, String methodName,
			int lineNumber, String threadName) {
		this.timestamp = timestamp;
		this.level = level;
		this.message = msg;
		this.throwable = t;
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.threadName = threadName;
	}

	public static LogEvent buildLogEvent(long timestamp, LogLevel level, String msg, Throwable t,
			StackTraceElement[] stackTrace) {
		String className = null;
		String methodName = null;
		int lineNumber = 0;

		if (stackTrace == null) {
			throw new IllegalArgumentException("stackTrace cannot be null");
		} else if (stackTrace.length < 3) {
			throw new IllegalArgumentException("stackTrace must be at least 3 in length.");
		} else {
			className = stackTrace[2].getClassName();
			methodName = stackTrace[2].getMethodName();
			lineNumber = stackTrace[2].getLineNumber();
		}
		String threadName = Thread.currentThread().getName();
		return new LogEvent(timestamp, level, msg, t, className, methodName, lineNumber, threadName);
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

	public String getThreadName() {
		return threadName;
	}

	@Override
	public EventChannel getChannel() {
		return LogService.eventChannel;
	}
}
