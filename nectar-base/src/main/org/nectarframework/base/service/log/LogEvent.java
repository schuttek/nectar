package org.nectarframework.base.service.log;

public class LogEvent {
	private long timestamp;
	private Log.Level level;
	private String message;
	private Throwable throwable;
	
	public LogEvent(long timestamp, Log.Level level, String msg, Throwable t) {
		this.timestamp = timestamp;
		this.level = level;
		this.message = msg;
		this.throwable  = t;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Log.Level getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getThrowable() {
		return throwable;
	}
}
