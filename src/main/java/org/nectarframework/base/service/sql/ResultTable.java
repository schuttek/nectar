package org.nectarframework.base.service.sql;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.cache.CacheableObject;
import org.nectarframework.base.tools.BitMap;
import org.nectarframework.base.tools.ByteArray;
import org.nectarframework.base.tools.StringTools;

public class ResultTable implements CacheableObject, Iterable<ResultRow> {
	private JavaTypes[] typesByColumn;
	private HashMap<String, Integer> keyMap;
	private Object[] table;
	private int colCount;
	private HashMap<String, HashMap<Long, ArrayList<ResultRow>>> searchMap = null;

	/**
	 * estimated memory usage of this result table.
	 */
	private long memorySize = 0;

	public ResultTable() {
	}

	protected ResultTable(JavaTypes[] typesByColumn, HashMap<String, Integer> keyMap, Object[] table, int colCount,
			long memorySize) {
		this.typesByColumn = typesByColumn;
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

	public boolean isNull(int row, String column) throws SQLException {
		return isNull(row, lookupColumn(column));
	}

	public boolean isNull(int row, int column) {
		return table[row * colCount + column] == null ? true : false;
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

		ResultTable ret = new ResultTable(rt1.typesByColumn, rt1.keyMap, retTable, retColCount,
				rt1.memorySize + rt1.memorySize);

		return ret;

	}

	@Override
	public ResultTable fromBytes(ByteArray ba) {
		colCount = ba.getInt();
		typesByColumn = new JavaTypes[colCount];
		for (int t = 0; t < colCount; t++) {
			typesByColumn[t] = JavaTypes.lookup(ba.getInt());
		}

		int keyMapSize = ba.getInt();
		this.keyMap = new HashMap<>();
		for (int t = 0; t < keyMapSize; t++) {
			String k = ba.getString();
			int v = ba.getInt();
			keyMap.put(k, v);
		}
		this.table = new Object[ba.getInt()];

		
		BitMap nullBitMap = new BitMap().fromBytes(ba);

		for (int t = 0; t < table.length; t++) {
			if (nullBitMap.is(t)) {
				switch (typesByColumn[t % colCount]) {
				case BigDecimal:
					table[t] = new BigDecimal(ba.getString());
					break;
				case Blob:
					table[t] = ba.getByteArray();
					break;
				case Boolean:
					table[t] = new Boolean(ba.getByte() > 0 ? true : false);
					break;
				case Byte:
					table[t] = new Byte(ba.getByte());
					break;
				case Double:
					table[t] = new Double(ba.getDouble());
					break;
				case Float:
					table[t] = new Float(ba.getFloat());
					break;
				case Int:
					table[t] = new Integer(ba.getInt());
					break;
				case Long:
					table[t] = new Long(ba.getLong());
					break;
				case Short:
					table[t] = new Short(ba.getShort());
					break;
				case String:
					table[t] = new String(ba.getString());
					break;
				case Unknown:
					table[t] = null;
					break;
				}
			}
		}
		return this;
	}

	@Override
	public ByteArray toBytes(ByteArray ba) {
		ba.add(this.colCount);
		for (int t = 0; t < colCount; t++) {
			ba.add(typesByColumn[t].getTypeId());
		}

		ba.add(keyMap.size());
		for (String k : keyMap.keySet()) {
			ba.add(k);
			ba.add(keyMap.get(k));
		}

		ba.add(table.length);

		// null bitmap
		BitMap nullBitMap = new BitMap(table.length);
		for (int t = 0; t < table.length; t++) {
			if (table[t] == null) {
				nullBitMap.set(t);
			}
		}
		nullBitMap.toBytes(ba);

		for (int t = 0; t < table.length; t++) {
			if (table[t] != null) {
				switch (typesByColumn[t % colCount]) {
				case BigDecimal:
					ba.add(((BigDecimal) table[t]).toString());
					break;
				case Blob:
					ba.addByteArray((byte[]) table[t]);
					break;
				case Boolean:
					ba.add((byte) ((Boolean) table[t] ? 1 : 0));
					break;
				case Byte:
					ba.add(((Byte) table[t]).byteValue());
					break;
				case Double:
					ba.add(((Double) table[t]).doubleValue());
					break;
				case Float:
					ba.add(((Float) table[t]).floatValue());
					break;
				case Int:
					ba.add(((Integer) table[t]).intValue());
					break;
				case Long:
					ba.add(((Long) table[t]).longValue());
					break;
				case Short:
					ba.add(((Short) table[t]).shortValue());
					break;
				case String:
					ba.add((String) table[t]);
					break;
				case Unknown:
					break;
				}
			}
		}
		return ba;
	}

}