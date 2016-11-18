package org.nectarframework.base.service.log;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.event.EventChannel;
import org.nectarframework.base.service.event.EventListener;
import org.nectarframework.base.service.event.EventService;
import org.nectarframework.base.service.event.NectarEvent;

public abstract class LogService extends Service implements EventListener {
	public static final EventChannel eventChannel = new EventChannel("Log");

	protected LogLevel level = LogLevel.TRACE;

	private EventService eventService;

	@Override
	protected boolean init() {
		return true;
	}

	@Override
	protected boolean run() {
		eventService.registerOnChannel(LogService.eventChannel, this);
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		String logLevelStr = sp.getString("logLevel", Log.DEFAULT_LOG_LEVEL.toString());
		if (logLevelStr == "trace") {
			level = LogLevel.TRACE;
		} else if (logLevelStr == "debug") {
			level = LogLevel.DEBUG;
		} else if (logLevelStr == "info") {
			level = LogLevel.INFO;
		} else if (logLevelStr == "warn") {
			level = LogLevel.WARN;
		} else if (logLevelStr == "fatal") {
			level = LogLevel.FATAL;
		} else if (logLevelStr == null) {
			level = LogLevel.TRACE;
		} else {
			Log.warn("LoggingService configuration had an unrecognized parameter value for 'logLevel'.");
		}
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		eventService = dependency(EventService.class);
		return true;
	}

	public boolean isLevelEnabled(LogLevel trace) {
		return level.compareTo(level) >= 0;
	}

	@Override
	public void handleListenerServiceEvent(NectarEvent e) {
		if (e instanceof LogEvent) {
			log((LogEvent)e);
		}
	}

	protected abstract void log(LogEvent e);
}
