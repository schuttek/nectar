package org.nectarframework.base.service.sql.simplesql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;

public class SimpleSqlService extends Service {

	private Connection dbConnection;

	private boolean openDatabase() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			Log.fatal("Couldn't find sqlite driver!", e);
			return false;
		}
		String path = "C:/Users/skander/My Documents/workspace/Nectar/nectar_directory.sqlite"; // TODO: get this from
		// service config
		try {
			dbConnection = DriverManager.getConnection("jdbc:sqlite:" + path);
		} catch (SQLException e) {
			Log.fatal("Unable to open sqlite database: " + path, e);
			return false;
		}
		return true;
	}

	private boolean closeDatabase() {
		try {
			dbConnection.close();
		} catch (SQLException e) {
			Log.fatal("Unable to close sqlite database", e);
			return false;
		}
		return true;
	}

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
	}

	@Override
	public boolean establishDependencies() {

		return true;
	}

	@Override
	protected boolean init() {
		if (!openDatabase()) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean run() {

		return true;
	}

	@Override
	protected boolean shutdown() {
		closeDatabase();
		return true;
	}

	public ResultSet select(String sql) throws SQLException {
		Statement stat = dbConnection.createStatement();
		ResultSet rs = stat.executeQuery(sql);

		return rs;
	}

}
