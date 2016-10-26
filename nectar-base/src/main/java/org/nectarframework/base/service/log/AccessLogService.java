package org.nectarframework.base.service.log;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.session.Session;
import org.nectarframework.base.service.sql.SqlPreparedStatement;
import org.nectarframework.base.service.sql.mysql.MysqlService;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;

public abstract class AccessLogService extends Service {

	@Override
	protected void checkParameters(ServiceParameters sp) throws ConfigurationException {
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		return true;
	}

	@Override
	protected boolean init() {
		return true;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	public abstract void accessLog(String path, Element rawForm, Element validated, Element output, long duration,
			String remoteIp, Session session);

}
