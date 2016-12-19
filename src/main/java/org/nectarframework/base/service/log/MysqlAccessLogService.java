package org.nectarframework.base.service.log;

import org.nectarframework.base.element.Element;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.session.Session;
import org.nectarframework.base.service.sql.SqlPreparedStatement;
import org.nectarframework.base.service.sql.mysql.MysqlService;
import org.nectarframework.base.service.xml.XmlService;

public class MysqlAccessLogService extends AccessLogService {

	private MysqlService my;

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		my = dependency(MysqlService.class);
		return super.establishDependencies();
	}

	@Override
	public void accessLog(String path, Element rawForm, Element validated, Element output, long duration,
			String remoteIp, Session session) {

		// TODO: buffering...

		String sql = "INSERT INTO nectar_log_access SET dateMs = ?, path = ?, formRaw = ?, formValid = ?, outputElm = ?, duration = ?, remoteIp = ?, session = ?";

		SqlPreparedStatement ps = new SqlPreparedStatement(sql);

		ps.setLong(1, System.currentTimeMillis());
		ps.setString(2, path);
		if (rawForm != null) {
			ps.setString(3, XmlService.toXmlString(rawForm).toString());
		} else {
			ps.setNull(3);
		}
		if (validated != null) {
			ps.setString(4, XmlService.toXmlString(validated).toString());
		} else {
			ps.setNull(4);
		}
		if (output != null) {
			ps.setString(5, XmlService.toXmlString(output).toString());
		} else {
			ps.setNull(5);
		}
		ps.setLong(6, duration);
		ps.setString(7, remoteIp);
		if (session != null) {
			ps.setString(8, session.toString());
		} else {
			ps.setNull(8);
		}

		my.asyncUpdate(ps);
	}
}
