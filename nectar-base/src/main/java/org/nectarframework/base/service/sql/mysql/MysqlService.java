package org.nectarframework.base.service.sql.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.sql.SqlService;

/**
 * The MySQL database access Service.
 * 
 * This service provides:
 * 
 * - configurable connections - pooled connections - asynchronous selects,
 * updates and inserts - conversion of java.sql.ResultSet objects into
 * nectar.base.service.mysql.ResultTable
 * 
 * Configuration Example:
 * 
 * <service class="nectar.base.service.mysql.MysqlService"> <param name="host"
 * value="192.168.0.26" /> <param name="port" value="3306" /> <param
 * name="database" value="nectar" /> <param name="user" value="nectar" /> <param
 * name="password" value="123456" /> <param name="startupConnections" value="5"
 * /> <param name="poolSize" value="50" /> </service>
 * 
 * 
 * 
 * 
 * TODO: managing master & slave connections.
 * 
 * TODO: get a connection for a specific table semi-transparently (ie, parse the sql), when tables are hosted on separate servers. Requires a lot of config work, and preparsing of Queries.  
 * 
 * TODO: check that all connections are returned to the pool or recreated, no matter what happens. 
 * 
 * Things to note:
 * 
 * Memory dangers: ResultSets and ResultTables are fully loaded in memory, and very object
 * oriented, so beware of large query results vs small Java VM max memory. For
 * example, a single query that loads a million rows of 10 columns of longs and
 * doubles, technically only 80 MB of data, can provoke the Java VM to allocate
 * up to 2.2 Gigs of RAM temporarily. To be fair, this is mostly the mysql driver's fault.
 * 
 * 
 * 
 * 
 * @author skander
 *
 */
public class MysqlService extends SqlService {

	protected boolean openConnections(int connCount) {

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			Log.fatal("Couldn't find mysql driver!", e);
			return false;
		}

		// Setup the connection with the DB

		try {
			for (int i = 0; i < connCount; i++) {
				Connection c = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + port + "/" + this.database + "?useSSL=false&autoReconnect=true&" + "user=" + this.user + "&password=" + this.password);
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
