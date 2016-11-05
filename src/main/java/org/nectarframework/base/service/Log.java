package org.nectarframework.base.service;

import java.io.PrintStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.nectarframework.base.service.event.EventService;
import org.nectarframework.base.service.log.LogEvent;
import org.nectarframework.base.service.log.LogLevel;

/**
 * Useable from anywhere when using Nectar, there's 6 functions from this class
 * that you MUST use whenever the program encounters anything from a situation
 * that *might* encounter an exception, even if it's snowball's chance in hell,
 * to major changes (like starting up and shutting down), to critical error that
 * will cause it to lose data.
 * 
 * Those six are:
 * 
 * Log.trace() - for progress reports, and whatever you feel like saying, feel
 * free to spam these while writing new code.
 * 
 * Log.debug() - for situation reports, internal stats, performance analysis,
 * etc.
 * 
 * Log.info() - major status changes, like starting up services, restarting
 * services, shutting down...
 * 
 * Log.warn() - for localized errors, for logical and programmatical errors and
 * sanity checks, that may incur data loss, but only have a small area of
 * effect, for example causing a specific request to fail, or an non essential
 * part of a service to fail.
 * 
 * Log.fatal() - Something is critically wrong with Nectar, which causes a
 * Service to be unavailable. It is usually followed by Nectar shutting down
 * because of it.
 * 
 * This class will print out to System.out and System.err. Once LogService is
 * started, it'll send it's messages onto it, which may then pass it on to a
 * database or whatever...
 * 
 * 
 * @author skander
 *
 */

public final class Log {

	private static EventService ev = null;

	public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;

	public static LogLevel logLevelOverride = DEFAULT_LOG_LEVEL;
	private static SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss]SSS");

	private static void printLogEvent(PrintStream out, LogEvent le) {
		out.println(sdf.format(new Date(le.getTimestamp())) + " <" + le.getLevel().name() + "> " + le.getMessage());
		if (le.getThrowable() != null) {
			le.getThrowable().printStackTrace(out);
		}
	}

	public static void log(LogLevel level, String msg, Throwable t) {
		logImpl(level, msg, t);
	}

	private static void logImpl(LogLevel level, String msg, Throwable t) {
		if (isLevelEnabled(level)) {
			LogEvent le = LogEvent.buildLogEvent(System.currentTimeMillis(), level, msg, t,
					Thread.currentThread().getStackTrace());
			printLogEvent(System.out, le);
			if (ev != null) {
				ev.publishEvent(le);
			}
		}
	}

	public static boolean isLevelEnabled(LogLevel level) {
		return true; // FIXME: mantis issue 37.
	}

	/*** Convenience Methods ***/

	public static boolean isTrace() {
		return isLevelEnabled(LogLevel.TRACE);
	}

	public static boolean isDebug() {
		return isLevelEnabled(LogLevel.DEBUG);
	}

	public static boolean isInfo() {
		return isLevelEnabled(LogLevel.INFO);
	}

	public static boolean isWarn() {
		return isLevelEnabled(LogLevel.WARN);
	}

	public static boolean isFatal() {
		return isLevelEnabled(LogLevel.FATAL);
	}

	public static void trace(String msg) {
		log(LogLevel.TRACE, msg, null);
	}

	public static void trace(String msg, Throwable t) {
		log(LogLevel.TRACE, msg, t);
	}

	public static void trace(Throwable t) {
		log(LogLevel.TRACE, null, t);
	}

	public static void debug(String msg) {
		log(LogLevel.DEBUG, msg, null);
	}

	public static void debug(String msg, Throwable t) {
		log(LogLevel.DEBUG, msg, t);
	}

	public static void debug(Throwable t) {
		log(LogLevel.DEBUG, null, t);
	}

	public static void info(String msg) {
		log(LogLevel.INFO, msg, null);
	}

	public static void info(String msg, Throwable t) {
		log(LogLevel.INFO, msg, t);
	}

	public static void info(Throwable t) {
		log(LogLevel.INFO, null, t);
	}

	public static void warn(String msg) {
		log(LogLevel.WARN, msg, null);
	}

	public static void warn(String msg, Throwable t) {
		log(LogLevel.WARN, msg, t);
	}

	public static void warn(Throwable t) {
		log(LogLevel.WARN, null, t);
	}

	public static void fatal(String msg) {
		log(LogLevel.FATAL, msg, null);
	}

	public static void fatal(String msg, Throwable t) {
		log(LogLevel.FATAL, msg, t);
	}

	public static void fatal(Throwable t) {
		log(LogLevel.FATAL, null, t);
	}

}
