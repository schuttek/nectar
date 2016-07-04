package org.nectarframework.base.service.log;

import java.io.PrintStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import org.nectarframework.base.service.session.Session;
import org.nectarframework.base.service.xml.Element;

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
 * effect.
 * 
 * Log.error() - for internal Nectar errors, like the MysqlService can't connect
 * to the mysql server for some reason.
 * 
 * Log.fatal() - These are meant to log ignored Exceptions that could never
 * happen unless the JDK changes, and thereby reveal programming flaws and
 * unhandled exceptions.
 * 
 * This class will print out to System.out and System.err. Once LogService is
 * started, it'll send it's messages onto it, which may then pass it on to
 * whatever
 * 
 * TODO: try to find /dev/nectar.base.log and print to it.
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author skander
 *
 */

public class Log {

	private static LinkedList<LogEvent> eventStore = new LinkedList<LogEvent>();

	private static LoggingService ls = null;

	public enum Level {
		TRACE, DEBUG, INFO, WARN, ERROR, FATAL
	}

	private static final Level DEFAULT_LOG_LEVEL = Level.TRACE;

	public static void setLoggingService(LoggingService loggingService) {
		ls = loggingService;
		if (ls != null) {
			sendStoredEvents();
		}
	}

	private static void printLogEvent(PrintStream out, LogEvent le) {
		if (le.getLevel().compareTo(DEFAULT_LOG_LEVEL) >= 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss]SSS");
			out.println(sdf.format(new Date(le.getTimestamp())) + " <" + le.getLevel().name() + "> " + le.getMessage());
			if (le.getThrowable() != null) {
				le.getThrowable().printStackTrace(out);
			}
			out.flush();
		}
	}

	private static void sendStoredEvents() {
		for (LogEvent le : eventStore) {
			ls.log(le);
		}
		eventStore.clear();
	}

	public static void log(Level level, String msg, Throwable t) {
		LogEvent le = new LogEvent(System.currentTimeMillis(), level, msg, t);
		if (ls == null) {
			eventStore.add(le);
			printLogEvent(System.out, le);
		} else {
			ls.log(le);
		}
	}

	public static boolean isLevelEnabled(Level level) {
		if (ls == null)
			return level.compareTo(DEFAULT_LOG_LEVEL) >= 0;
		return ls.isLevelEnabled(level);
	}

	public static boolean isTrace() {
		return isLevelEnabled(Level.TRACE);
	}

	public static boolean isDebug() {
		return isLevelEnabled(Level.DEBUG);
	}

	public static boolean isInfo() {
		return isLevelEnabled(Level.INFO);
	}

	public static boolean isWarn() {
		return isLevelEnabled(Level.WARN);
	}

	public static boolean isFatal() {
		return isLevelEnabled(Level.FATAL);
	}

	public static void trace(String msg) {
		log(Level.TRACE, msg, null);
	}

	public static void trace(String msg, Throwable t) {
		log(Level.TRACE, msg, t);
	}

	public static void trace(Throwable t) {
		log(Level.TRACE, null, t);
	}

	public static void debug(String msg) {
		log(Level.DEBUG, msg, null);
	}

	public static void debug(String msg, Throwable t) {
		log(Level.DEBUG, msg, t);
	}

	public static void debug(Throwable t) {
		log(Level.DEBUG, null, t);
	}

	public static void info(String msg) {
		log(Level.INFO, msg, null);
	}

	public static void info(String msg, Throwable t) {
		log(Level.INFO, msg, t);
	}

	public static void info(Throwable t) {
		log(Level.INFO, null, t);
	}

	public static void warn(String msg) {
		log(Level.WARN, msg, null);
	}

	public static void warn(String msg, Throwable t) {
		log(Level.WARN, msg, t);
	}

	public static void warn(Throwable t) {
		log(Level.WARN, null, t);
	}

	public static void fatal(String msg) {
		log(Level.FATAL, msg, null);
	}

	public static void fatal(String msg, Throwable t) {
		log(Level.FATAL, msg, t);
	}

	public static void fatal(Throwable t) {
		log(Level.FATAL, null, t);
	}

	public static void accessLog(String path, Element rawForm, Element validated, Element output, long duration, String remoteIp, Session session) {
		if (ls != null) {
			ls.accessLog(path, rawForm, validated, output, duration, remoteIp, session);
		}
	}

	/**
	 * Call a fatal lockdown.
	 * 
	 * Once this method has been called, Nectar will begin an emergency shutdown
	 * asap.
	 * 
	 * Only use this if you KNOW the System has been compromised.
	 * 
	 * Dump all relevant info BEFORE you call this method.
	 */
	public static void fatalHack() {
		// fw = FileWriter("gotHacked.dump");
		// stackTrace >> fw
		System.exit(-1);
		// TODO: implement me
	}

	public static String getStackTrace() {
		StackTraceElement[] stes = Thread.currentThread().getStackTrace();
		StringBuffer sb = new StringBuffer();

		for (int t = 2; t < stes.length; t++) {
			sb.append("      at " + stes[t].getClassName()+"."+stes[t].getMethodName()+"("+stes[t].getFileName()+":"+stes[t].getLineNumber()+")\n");
		}
		
		return sb.toString();
	}
	
	public static String getStackTrace(Throwable te) {
		StackTraceElement[] stes = te.getStackTrace();

		StringBuffer sb = new StringBuffer();

		for (int t = 0; t < stes.length; t++) {
			sb.append("      at " + stes[t].getClassName()+"."+stes[t].getMethodName()+"("+stes[t].getFileName()+":"+stes[t].getLineNumber()+")\n");
		}
		
		return sb.toString();
		
	}

}
