package org.nectarframework.base.service.sql;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.tools.StringTools;

public class ResultTable implements Serializable, Iterable<ResultRow> {
	private static final long serialVersionUID = -4894318142244242418L;
	private HashMap<String, Integer> keyMap;
	private Object[] table;
	private int colCount;
	private HashMap<String, HashMap<Long, ArrayList<ResultRow>>> searchMap = null;

	/**
	 * estimated memory usage of this result table.
	 */
	private long memorySize = 0;

	protected ResultTable(HashMap<String, Integer> keyMap, Object[] table, int colCount, long memorySize) {
		this.keyMap = keyMap;
		this.table = table;
		this.colCount = colCount;
		this.memorySize = memorySize;
	}

	public int colCount() {
		return colCount;
	}

	public int rowCount() {
		return this.table.length / colCount;
	}

	public String getString(int row, String column) throws SQLException {
		return getString(row, lookupColumn(column));
	}

	public String getString(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof String) {
			return (String) obj;
		}
		throw new SQLException("Type mismatch. Column " + column + " is a " + obj.getClass().getName());
	}

	public Byte getByte(int row, String column) throws SQLException {
		return getByte(row, lookupColumn(column));
	}

	public Byte getByte(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof Byte) {
			return (Byte) obj;
		}
		throw new SQLException("Type mismatch. Column " + column + " is a " + obj.getClass().getName());
	}

	public Short getShort(int row, String column) throws SQLException {
		return getShort(row, lookupColumn(column));
	}

	public Short getShort(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof Short) {
			return (Short) obj;
		}
		throw new SQLException("Type mismatch. Column " + column + " is a " + obj.getClass().getName());
	}

	public Integer getInt(int row, String column) throws SQLException {
		return getInt(row, lookupColumn(column));
	}

	public Integer getInt(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof Integer) {
			return (Integer) obj;
		}
		throw new SQLException("Type mismatch. Column " + column + " is a " + obj.getClass().getName());
	}

	public BigDecimal getBigDecimal(int row, String column) throws SQLException {
		return getBigDecimal(row, lookupColumn(column));
	}

	public BigDecimal getBigDecimal(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof BigDecimal) {
			return (BigDecimal) obj;
		}
		throw new SQLException("Type mismatch. Column " + column + " is a " + obj.getClass().getName());
	}

	public Boolean getBoolean(int row, String column) throws SQLException {
		return getBoolean(row, lookupColumn(column));
	}

	public Boolean getBoolean(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof Boolean) {
			return (Boolean) obj;
		}
		throw new SQLException("Type mismatch. Column " + column + " is a " + obj.getClass().getName());
	}

	public Long getLong(int row, String column) throws SQLException {
		return getLong(row, lookupColumn(column));
	}

	public Long getLong(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof Long) {
			return (Long) obj;
		}
		throw new SQLException("Type mismatch column " + column + " is a " + obj.getClass().getName());
	}
	
	public Long getNumberAsLong(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof Long) {
			return (Long) obj;
		}
		if (obj instanceof Integer) {
			return ((Integer) obj).longValue();
		}
		if (obj instanceof Short) {
			return ((Short) obj).longValue();
		}
		if (obj instanceof Byte) {
			return ((Byte) obj).longValue();
		}
		throw new SQLException("Type mismatch column " + column + " is a " + obj.getClass().getName());
	}

	public Float getFloat(int row, String column) throws SQLException {
		return getFloat(row, lookupColumn(column));
	}

	public Float getFloat(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof Float) {
			return (Float) obj;
		}
		throw new SQLException("Type mismatch. Column " + column + " is a " + obj.getClass().getName());
	}

	public Double getDouble(int row, String column) throws SQLException {
		return getDouble(row, lookupColumn(column));
	}

	public Double getDouble(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof Double) {
			return (Double) obj;
		}
		throw new SQLException("Type mismatch. Column " + column + " is a " + obj.getClass().getName());
	}

	public byte[] getBlob(int row, String column) throws SQLException {
		return getBlob(row, lookupColumn(column));
	}

	public byte[] getBlob(int row, int column) throws SQLException {
		Object obj = table[row * colCount + column];
		if (obj == null) {
			return null;
		}
		if (obj instanceof byte[]) {
			return (byte[]) obj;
		}
		throw new SQLException("Type mismatch. Column " + column + " is a " + obj.getClass().getName());
	}

	public Object getObject(int row, String column) throws SQLException {
		return getObject(row, lookupColumn(column));
	}

	private Object getObject(int row, int column) {
		return table[row * colCount + column];
	}

	/**
	 * Returns the first ResultRow of this table.
	 * 
	 */
	public ResultRow iterator() {
		ResultRow rr = new ResultRow(this);
		return rr;
	}

	private Integer lookupColumn(String column) throws SQLException {
		if (!keyMap.containsKey(column)) {
			Log.trace("column " + column + " not found in " + StringTools.mapToString(keyMap));
			throw new SQLException("Unknown column " + column);
		}
		return keyMap.get(column);
	}

	/**
	 * The names of the columns defined in this result table, in no particular
	 * order.
	 * 
	 * @return
	 */
	public Set<String> getColumNames() {
		return keyMap.keySet();
	}


	/**
	 * Returns a map indexed by the long values in the given column
	 *  
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	public HashMap<Long, ResultRow> mapByColumn(String column) throws SQLException {
		HashMap<Long, ResultRow> map = new HashMap<Long, ResultRow>();
		int colIdx = this.lookupColumn(column);
		for (int rowIdx = 0; rowIdx < this.rowCount(); rowIdx++) {
			
			map.put(this.getNumberAsLong(rowIdx, colIdx), new ResultRow(this, rowIdx));
		}
		return map;
	}
	
	/**
	 * Returns a list of rows where the given column is equal to the given
	 * match. The first call will build a HashMap in O(n) time, subsequent calls
	 * are O(1).
	 * 
	 * Intended for joining the results of 2 queries programmatically.
	 * 
	 * @param column
	 * @param match
	 * @return
	 * @throws SQLException
	 */
	public List<ResultRow> subList(String column, Long match) throws SQLException {
		if (searchMap == null) {
			searchMap = new HashMap<String, HashMap<Long, ArrayList<ResultRow>>>();
		}
		if (!searchMap.containsKey(column)) {
			HashMap<Long, ArrayList<ResultRow>> matchMap = new HashMap<Long, ArrayList<ResultRow>>();
			int colIdx = this.lookupColumn(column);
			int rowCount = this.rowCount();
			for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
				Long cellValue = getLong(rowIdx, colIdx);
				List<ResultRow> list = null;
				if (matchMap.containsKey(cellValue)) {
					list = matchMap.get(cellValue);
				} else {
					list = new ArrayList<ResultRow>();
				}
				list.add(new ResultRow(this, rowIdx));
			}
			searchMap.put(column, matchMap);
		}

		ArrayList<ResultRow> subList = searchMap.get(column).get(match);
		if (subList == null) {
			subList = new ArrayList<ResultRow>();
		}
		return subList;
	}

	public long estimateMemorySize() {
		return memorySize;
	}

	public String debugString() throws SQLException {
		String s = "";
		for (String key : keyMap.keySet()) {
			for (ResultRow rr : this) {
				s += "> " + key + "=" + rr.getObject(key);
			}
		}
		return s;
	}

	/**
	 * Appends the rows of rt2 to rt1, with the columns defined in rt1. rt2 must
	 * have all the same column names and types as rt1, in no specific order.
	 * 
	 * @param rt1
	 * @param rt2
	 * @return a new ResultTable contains the rows of rt1 and rt2
	 * @throws SQLException 
	 */
	public static ResultTable append(ResultTable rt1, ResultTable rt2) {
		Set<String> rt2colNames = rt2.getColumNames();
		for (String key : rt1.getColumNames()) {
			if (!rt2colNames.contains(key)) {
				throw new IllegalArgumentException("Second ResultTable is missing column " + key);
			}
		}

		HashMap<String, Integer> retKeyMap = rt1.keyMap;
		int retColCount = rt1.colCount();
		Object[] retTable = new Object[retColCount * (rt1.rowCount() + rt2.rowCount())];

		int tableIdx;
		for (tableIdx = 0; tableIdx < rt1.table.length; tableIdx++) {
			retTable[tableIdx] = rt1.table[tableIdx];
		}

		for (ResultRow rr : rt2) {
			for (String key : rt1.getColumNames()) {
				try {
					retTable[tableIdx] = rr.getObject(key);
				} catch (SQLException e) {
					// can't happen, we checked at start.
				}
				tableIdx++;
			}
		}

		ResultTable ret = new ResultTable(retKeyMap, retTable, retColCount, rt1.memorySize + rt1.memorySize);

		return ret;

	}
}