package org.nectarframework.base.service.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLogService extends LogService {

	@Override
	protected void log(LogEvent e) {
		if (isLevelEnabled(e.getLevel())) {
			SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss]SSS");
			System.out.println(sdf.format(new Date(e.getTimestamp())) + " <" + e.getLevel().name() + "> " + e.getMessage());
			if (e.getThrowable() != null) {
				e.getThrowable().printStackTrace(System.out);
			}
			System.out.flush();
		}
	}

}
