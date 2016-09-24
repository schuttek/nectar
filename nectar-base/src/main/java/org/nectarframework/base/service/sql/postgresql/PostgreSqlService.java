package org.nectarframework.base.service.sql.postgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.sql.SqlService;

public class PostgreSqlService extends SqlService {

	// TODO: there's no auto reconnect option for postgre... each connection would have to be checked for validity before use??
	
	@Override
	protected boolean openConnections(int connCount) {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			Log.fatal("Couldn't find postgresql driver!", e);
			return false;
		}

		// Setup the connection with the DB

		try {
			for (int i = 0; i < connCount; i++) {
				Connection c = DriverManager.getConnection("jdbc:postgresql://" + this.host + ":" + port + "/" + this.database + "?ssl=false&" + "user=" + this.user + "&password=" + this.password);
				poolConnections.add(c);
				idleConnections.add(c);
			}

		} catch (SQLException e) {
			Log.fatal(this.getClass().getName()+": Unable to open database connection", e);
			return false;
		}
		return true;
	}

}
