package org.nectarframework.base.service.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class SqlTransactionHandle {

	Connection connection;
	SqlService my;

	public SqlTransactionHandle(Connection connection, SqlService my) {
		this.connection = connection;
		this.my = my;
	}

	
	public Vector<Long> insert(SqlPreparedStatement mps) throws SQLException {
		PreparedStatement prepStat = connection.prepareStatement(mps.getSql(), Statement.RETURN_GENERATED_KEYS);
		mps._applyToJavaSQLPreparedStatement(prepStat, my.getMaxInsertBatchSize());
		Vector<Long> ids = new Vector<Long>();
		ResultSet keys = prepStat.getGeneratedKeys();
		while (keys.next()) {
			ids.add(keys.getLong(1));
		}
		prepStat.close();
		return ids;
	}
	
	public int update(SqlPreparedStatement mps) throws SQLException {
		PreparedStatement prepStat = connection.prepareStatement(mps.getSql());
		mps._applyToJavaSQLPreparedStatement(prepStat);
		int rows = prepStat.executeUpdate();
		prepStat.close();
		return rows;
	}
	
	public void rollback() throws SQLException {
		connection.rollback();
		connection.setAutoCommit(false);
		my.returnConnection(connection);
	}
	
	public void commit() throws SQLException {
		connection.commit();
		connection.setAutoCommit(true);
		my.returnConnection(connection);
	}
	
}
