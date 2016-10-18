package org.nectarframework.base.service.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.cache.CacheService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.tools.StringTools;

public abstract class SqlService extends Service {
	// TODO: handle some basic connection errors, and attempt reconnection.

	/**
	 * connections that aren't currently in use.
	 */
	protected Vector<Connection> idleConnections;
	/**
	 * all the connections that this service has opened.
	 */
	protected Vector<Connection> poolConnections;

	/**
	 * Configurable: number of connections to open during startup.
	 */
	protected int startupConnections;
	/**
	 * Configurable: number of connections to open after startup.
	 */
	protected int poolSize;

	protected String host;
	protected String port;
	protected String database;
	protected String user;
	protected String password;

	protected int maxInsertBatchSize = 100;

	public int getMaxInsertBatchSize() {
		return maxInsertBatchSize;
	}

	protected ThreadService threadService;
	protected CacheService cacheService;

	private boolean closeConnections() {
		try {
			for (Connection c : poolConnections) {
				c.close();
			}
		} catch (SQLException e) {
			Log.fatal("Unable to close mysql", e);
			return false;
		}
		Log.trace(this.getClass().getName() + ": closed all Connections.");
		return true;
	}

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		host = sp.getValue("host");
		port = sp.getValue("port");
		database = sp.getValue("database");
		user = sp.getValue("user");
		password = sp.getValue("password");
		poolSize = sp.getInt("poolSize", 1, 1000, 10);
		startupConnections = sp.getInt("startupConnections", 1, 1000, 2);
		maxInsertBatchSize = sp.getInt("maxInsertBatchSize", 1, 1000, 100);
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		threadService = (ThreadService) dependency(ThreadService.class);
		cacheService = (CacheService) dependency(CacheService.class);
		return true;
	}

	@Override
	protected boolean init() {
		idleConnections = new Vector<Connection>();
		poolConnections = new Vector<Connection>();

		if (!openConnections(startupConnections)) { // open just a few
													// connections for quick
													// startup
			return false;
		}
		return true;
	}

	protected abstract boolean openConnections(int startupConnections);

	@Override
	protected boolean run() {

		// open the rest of the pool connections in a ThreadTask
		OpenRemainingConnectionsTask orct = new OpenRemainingConnectionsTask(this,
				this.poolSize - this.startupConnections);
		threadService.execute(orct);

		return true;
	}

	@Override
	protected boolean shutdown() {
		closeConnections();
		Log.info(this.getClass().getName() + " shut down.");
		return true;
	}

	private Connection getConnection() {
		synchronized (idleConnections) {
			while (idleConnections.isEmpty()) {
				try {
					idleConnections.wait();
				} catch (InterruptedException e) {
				}
			}
			Connection c = idleConnections.remove(0);
			return c;
		}
	}

	void returnConnection(Connection conn) {
		synchronized (idleConnections) {
			idleConnections.add(conn);
			idleConnections.notifyAll();
		}
	}

	public SqlTransactionHandle beginTransaction() throws SQLException {
		Connection conn = getConnection();
		conn.setAutoCommit(false);
		SqlTransactionHandle mth = new SqlTransactionHandle(conn, this);
		return mth;
	}

	public int update(String sql) throws SQLException {
		if (Log.isWarn()) {
			this.verifyUpdate(sql);
		}

		Connection conn = getConnection();
		Statement stat = conn.createStatement();
		int rows = stat.executeUpdate(sql);
		stat.close();
		returnConnection(conn);
		return rows;
	}

	public int update(SqlPreparedStatement ps) throws SQLException {
		if (Log.isWarn()) {
			this.verifyUpdate(ps.getSql());
		}
		Connection conn = getConnection();
		PreparedStatement prepStat = conn.prepareStatement(ps.getSql());
		ps._applyToJavaSQLPreparedStatement(prepStat);
		int rows = prepStat.executeUpdate();
		prepStat.close();
		returnConnection(conn);
		return rows;
	}

	/**
	 * For INSERT statements, returns the list of auto generated IDs.
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public Vector<Long> insert(String sql) throws SQLException {
		if (Log.isWarn()) {
			this.verifyUpdate(sql);
		}

		Connection conn = getConnection();
		Statement stat = conn.createStatement();
		stat.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		ResultSet keys = stat.getGeneratedKeys();
		Vector<Long> ids = new Vector<Long>();
		while (keys.next()) {
			ids.add(keys.getLong(1));
		}
		stat.close();
		returnConnection(conn);
		return ids;
	}

	/**
	 * For INSERT statements, returns the list of auto generated IDs.
	 * 
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
	public Vector<Long> insert(SqlPreparedStatement ps) throws SQLException {
		if (Log.isWarn()) {
			this.verifyUpdate(ps.getSql());
		}
		Connection conn = getConnection();
		PreparedStatement prepStat = conn.prepareStatement(ps.getSql(), Statement.RETURN_GENERATED_KEYS);
		ps._applyToJavaSQLPreparedStatement(prepStat, this.maxInsertBatchSize);
		Vector<Long> ids = new Vector<Long>();
		ResultSet keys = prepStat.getGeneratedKeys();
		while (keys.next()) {
			ids.add(keys.getLong(1));
		}
		prepStat.close();
		returnConnection(conn);
		return ids;
	}

	public void delayedInsert(SqlPreparedStatement ps) {
		DelayedInsertTask dit = new DelayedInsertTask(this, ps);
		threadService.execute(dit);
	}

	public AsyncTicket asyncUpdate(SqlPreparedStatement ps) {
		AsyncTicket at = new AsyncTicket();
		at.setReady(false);

		AsyncUpdateTask ast = new AsyncUpdateTask(this, ps, at);
		try {
			threadService.execute(ast);
		} catch (Exception e) {
			Log.fatal(e);
		}
		return at;
	}

	public AsyncTicket asyncInsert(SqlPreparedStatement ps) {
		AsyncTicket at = new AsyncTicket();
		at.setReady(false);

		AsyncInsertTask ast = new AsyncInsertTask(this, ps, at);
		try {
			threadService.execute(ast);
		} catch (Exception e) {
			Log.fatal(e);
		}
		return at;
	}

	/**
	 * Synchronous - Blocking query, for those that want to wait. this will
	 * bypass cache.
	 * 
	 * @param sql
	 *            - the SELECT etc... query.
	 * @return ResultTable
	 * @throws SQLException
	 */
	public ResultTable select(String sql) throws SQLException {
		if (Log.isWarn()) {
			this.verifySelect(sql);
		}

		Connection conn = getConnection();
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery(sql);

		ResultTable rt = resultSetToResultTable(rs);
		rs.close();
		stat.close();
		returnConnection(conn);
		return rt;
	}

	/**
	 * Synchronous select query that doesn't use any local cache like
	 * CacheService. MySQL may still use it's own cache unless you specify
	 * NO_CACHE in the query.
	 * 
	 * @param mps
	 * @return
	 * @throws SQLException
	 */
	public ResultTable select(SqlPreparedStatement mps) throws SQLException {
		if (Log.isWarn()) {
			this.verifySelect(mps);
		}

		Connection conn = getConnection();
		PreparedStatement prepStat = conn.prepareStatement(mps.getSql(), ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
		prepStat.setFetchDirection(ResultSet.FETCH_FORWARD);
		mps._applyToJavaSQLPreparedStatement(prepStat);
		ResultSet rs = prepStat.executeQuery();
		ResultTable rt = resultSetToResultTable(rs);
		rs.close();
		prepStat.close();
		returnConnection(conn);
		return rt;
	}

	/**
	 * Synchronous select query, with a cache timer hint. If the same SQL
	 * request was performed in the last cacheExpiry milliseconds, then return
	 * the cached version (CacheService).
	 * 
	 * Else, perform the request, and try to load it in cache.
	 * 
	 * @param sql
	 * @param cacheExpiry
	 * @return
	 * @throws SQLException
	 */
	public ResultTable select(String sql, long cacheExpiry) throws SQLException {
		ResultTable rt = getCachedResultTable(sql, false);
		if (rt == null) {
			rt = select(sql);
			cacheService.add(sql, rt, cacheExpiry);
		}
		return rt;
	}

	/**
	 * Synchronous select query, with a cache timer hint. If the same SQL
	 * request was performed in the last cacheExpiry milliseconds, then return
	 * the cached version (CacheService).
	 * 
	 * Else, perform the request, and try to load it in cache.
	 * 
	 * @param sql
	 * @param cacheExpiry
	 * @return
	 * @throws SQLException
	 */
	public ResultTable select(SqlPreparedStatement mps, long cacheExpiry) throws SQLException {
		ResultTable rt = getCachedResultTable(mps, false);
		if (rt == null) {
			rt = select(mps);
			addResultTableToCache(mps, rt, cacheExpiry);
		}
		return rt;
	}

	/**
	 * Begins an asynchronous query. The SQL query will be performed in a
	 * separate thread. Use AsyncTicket to get the result.
	 * 
	 * @param sql
	 * @return
	 */
	public AsyncTicket asyncSelect(String sql) {
		AsyncTicket at = new AsyncTicket();
		at.setReady(false);

		AsyncSelectTask ast = new AsyncSelectTask(this, sql, at);
		try {
			threadService.execute(ast);
		} catch (Exception e) {
			Log.fatal(e);
		}
		return at;
	}

	/**
	 * Begins an asynchronous query. The SQL query will be performed in a
	 * separate thread. Use AsyncTicket to get the result.
	 * 
	 * @param sql
	 * @return
	 */
	public AsyncTicket asyncSelect(SqlPreparedStatement sql) {
		AsyncTicket at = new AsyncTicket();
		at.setReady(false);

		AsyncSelectTask ast = new AsyncSelectTask(this, sql, at);
		try {
			threadService.execute(ast);
		} catch (Exception e) {
			Log.fatal(e);
		}
		return at;
	}

	/**
	 * Begins an asynchronous query in a separate thread unless this request has
	 * already been cached. Use AsyncTicket to get the results.
	 * 
	 * @param sql
	 *            the SQL query to perform
	 * @param cacheExpiry
	 *            the amount of time to keep this query in cache.
	 * @return
	 */
	public AsyncTicket asyncSelect(String sql, long cacheExpiry) {
		ResultTable rt = getCachedResultTable(sql, false);
		AsyncTicket at;
		if (rt == null) {
			at = new AsyncTicket();
			at.setReady(false);

			AsyncSelectTask ast = new AsyncSelectTask(this, sql, at, cacheExpiry);
			try {
				threadService.execute(ast);
			} catch (Exception e) {
				Log.fatal(e);
			}
			return at;
		} else {
			at = new AsyncTicket();
			at.setReady(true);
			at.setResultTable(rt);
		}
		return at;
	}

	/**
	 * Converts a java.sql.ResultSet into a
	 * nectar.base.service.mysql.ResultTable
	 * 
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */

	// TODO: this needs some performance benchmarking and trimming.

	// We're essentially loading the result set into memory twice here, and
	// that's not viable for large result sets.

	protected ResultTable resultSetToResultTable(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		JavaTypes[] typesMap = new JavaTypes[colCount];
		HashMap<String, Integer> keyMap = new HashMap<String, Integer>();
		for (int i = 0; i < colCount; i++) {
			keyMap.put(rsmd.getColumnName(i + 1), i);
			switch (rsmd.getColumnType(i + 1)) {
			case Types.DECIMAL:
				typesMap[i] = JavaTypes.BigDecimal;
				break;
			case Types.BIT:
			case Types.BOOLEAN:
				typesMap[i] = JavaTypes.Boolean;
				break;
			case Types.CHAR:
			case Types.LONGNVARCHAR:
			case Types.LONGVARCHAR:
			case Types.VARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
				typesMap[i] = JavaTypes.String;
				break;
			case Types.DOUBLE:
				typesMap[i] = JavaTypes.Double;
				break;
			case Types.FLOAT:
				typesMap[i] = JavaTypes.Float;
				break;
			case Types.TINYINT:
				typesMap[i] = JavaTypes.Byte;
				break;
			case Types.SMALLINT:
				typesMap[i] = JavaTypes.Short;
				break;
			case Types.INTEGER:
				typesMap[i] = JavaTypes.Int;
				break;
			case Types.BIGINT:
				typesMap[i] = JavaTypes.Long;
				break;
			case Types.BLOB:
			case Types.CLOB:
			case Types.VARBINARY:
				typesMap[i] = JavaTypes.Blob;
				break;
			case Types.TIME:
			case Types.TIMESTAMP:
			case Types.DATE:
			case Types.ARRAY:
			case Types.BINARY:
			case Types.DATALINK:
			case Types.DISTINCT:
			case Types.JAVA_OBJECT:
			case Types.LONGVARBINARY:
			case Types.NCLOB:
			case Types.NULL:
			case Types.NUMERIC:
			case Types.OTHER:
			case Types.REAL:
			case Types.REF:
			case Types.ROWID:
			case Types.SQLXML:
			case Types.STRUCT:
				typesMap[i] = JavaTypes.Unknown;
				break;
			}
		}
		long memorySize = 0;

		String stringPtr = null;
		byte[] baPtr = null;
		int rowCount = 0;
		LinkedList<Object[]> rowList = new LinkedList<Object[]>();
		while (rs.next()) {
			Object[] rowData = new Object[colCount];
			for (int col = 0; col < colCount; col++) { // k
				switch (rsmd.getColumnType(col + 1)) {
				case Types.DECIMAL:
					rowData[col] = rs.getBigDecimal(col + 1);
					memorySize += 20; // FIXME: wild ass guess...
					break;
				case Types.BIT:
				case Types.BOOLEAN:
					rowData[col] = rs.getBoolean(col + 1);
					memorySize += 1;
					break;
				case Types.CHAR:
				case Types.LONGNVARCHAR:
				case Types.LONGVARCHAR:
				case Types.VARCHAR:
				case Types.NCHAR:
				case Types.NVARCHAR:
					stringPtr = rs.getString(col + 1);
					rowData[col] = stringPtr;
					if (stringPtr != null)
						memorySize += stringPtr.length();
					break;
				case Types.DOUBLE:
					rowData[col] = new Double(rs.getDouble(col + 1));
					memorySize += 8;
					break;
				case Types.FLOAT:
					rowData[col] = new Float(rs.getFloat(col + 1));
					memorySize += 4;
					break;
				case Types.TINYINT:
					rowData[col] = new Byte(rs.getByte(col + 1));
					memorySize += 1;
					break;
				case Types.SMALLINT:
					rowData[col] = new Short(rs.getShort(col + 1));
					memorySize += 1;
					break;
				case Types.INTEGER:
					rowData[col] = new Integer(rs.getInt(col + 1));
					memorySize += 4;
					break;
				case Types.BIGINT:
					rowData[col] = new Long(rs.getLong(col + 1));
					memorySize += 8;
					break;
				case Types.TIME:
					rowData[col] = rs.getTime(col + 1);
					memorySize += 8;
					break;
				case Types.TIMESTAMP:
					rowData[col] = rs.getTimestamp(col + 1);
					memorySize += 8;
					break;
				case Types.DATE:
					rowData[col] = rs.getDate(col + 1);
					memorySize += 8;
					break;

				case Types.BLOB:
				case Types.CLOB:
				case Types.VARBINARY:
					baPtr = rs.getBytes(col + 1);
					rowData[col] = baPtr;
					if (baPtr != null)
						memorySize += baPtr.length;
					break;
				case Types.ARRAY:
				case Types.BINARY:
				case Types.DATALINK:
				case Types.DISTINCT:
				case Types.JAVA_OBJECT:
				case Types.LONGVARBINARY:
				case Types.NCLOB:
				case Types.NULL:
				case Types.NUMERIC:
				case Types.OTHER:
				case Types.REAL:
				case Types.REF:
				case Types.ROWID:
				case Types.SQLXML:
				case Types.STRUCT:
					rowData[col] = rs.getObject(col + 1); // not supported
					break;
				}
			}
			rowList.add(rowData);
			rowCount++;
		}
		Object[] table = new Object[rowCount * colCount];

		Object[] rowData = null;
		for (int row = 0; row < rowCount; row++) {
			rowData = rowList.pop();
			for (int t = 0; t < colCount; t++) {
				table[row * colCount + t] = rowData[t];
			}
		}

		memorySize += rowCount * colCount * 8 + colCount * 10; // size of the
																// table itself,
																// plus the
																// keyMap.

		return new ResultTable(typesMap, keyMap, table, colCount, memorySize);

	}

	private void verifyUpdate(String sql) throws SQLException {
		if (!sql.substring(0, 7).toLowerCase().startsWith("update ")
				&& !sql.substring(0, 7).toLowerCase().startsWith("insert ")
				&& !sql.substring(0, 7).toLowerCase().startsWith("delete ")) {
			Log.warn(this.getClass().getName() + ".update called with SQL query other than UPDATE, INSERT, DELETE: "
					+ sql);
			throw new SQLException(this.getClass().getName()
					+ ".update must begin with UPDATE, INSERT, DELETE - called with invalid sql " + sql);
		}
	}

	private void verifySelect(String selectSql) throws SQLException {
		if (!selectSql.substring(0, 7).toLowerCase().startsWith("select ")
				&& !selectSql.toLowerCase().startsWith("show ")) {
			Log.warn(this.getClass().getName() + ".select called with SQL query other than SELECT: " + selectSql);
			throw new SQLException(this.getClass().getName()
					+ ".select must begin with SELECT - called with invalid sql " + selectSql);
		}
	}

	private void verifySelect(SqlPreparedStatement preparedStatement) throws SQLException {
		this.verifySelect(preparedStatement.getSql());
	}

	public void flushTables(String[] tableNames) throws SQLException {
		String s = "";
		if (tableNames != null) {
			s = StringTools.implode(tableNames, ", ");
		}
		String sql = "FLUSH TABLES " + s;
		execute(sql);
	}

	/**
	 * Execute an "uncommon" SQL command (eg. CREATE, ALTER, TRUNCATE, etc)
	 * 
	 * @param sql
	 * @throws SQLException
	 */
	public void execute(String sql) throws SQLException {
		Connection conn = getConnection();
		Statement stat = conn.createStatement();
		// boolean result =
		stat.execute(sql);
		stat.close();
		returnConnection(conn);
	}

	private ResultTable getCachedResultTable(SqlPreparedStatement mps, boolean refresh) {
		// TODO Auto-generated method stub
		return null;
	}

	private ResultTable getCachedResultTable(String sql, boolean refresh) {
		// TODO Auto-generated method stub
		return null;
	}

	private void addResultTableToCache(SqlPreparedStatement mps, ResultTable rt, long cacheExpiry) {
		// TODO Auto-generated method stub

	}

}
