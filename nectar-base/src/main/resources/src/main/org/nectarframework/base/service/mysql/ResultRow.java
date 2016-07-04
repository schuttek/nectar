package org.nectarframework.base.service.mysql;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Iterator;

public class ResultRow implements Iterator<ResultRow> {
	private ResultTable resultTable;
	private int rowIndex;

	public ResultRow(ResultTable resultTable) {
		this.resultTable = resultTable;
		this.rowIndex = -1;
	}

	public ResultRow(ResultTable resultTable, int rowIndex) {
		this.resultTable = resultTable;
		this.rowIndex = rowIndex;
	}

	public boolean hasNext() {
		if (resultTable.rowCount() > rowIndex + 1)
			return true;
		return false;
	}

	public ResultRow next() {
		if (hasNext()) {
			rowIndex++;
			return this;
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public int colCount() {
		return this.resultTable.colCount();
	}

	public int rowCount() {
		return this.resultTable.colCount();
	}

	public int getRowIndex() {
		return this.rowIndex;
	}

	public String getString(String column) throws SQLException {
		return this.resultTable.getString(rowIndex, column);
	}

	public String getString(int column) throws SQLException {
		return this.resultTable.getString(rowIndex, column);
	}

	public Byte getByte(String column) throws SQLException {
		return this.resultTable.getByte(rowIndex, column);
	}

	public Short getShort(String column) throws SQLException {
		return this.resultTable.getShort(rowIndex, column);
	}

	public Short getShort(int column) throws SQLException {
		return this.resultTable.getShort(rowIndex, column);
	}

	public Integer getInt(String column) throws SQLException {
		return this.resultTable.getInt(rowIndex, column);
	}

	public Integer getInt(int column) throws SQLException {
		return this.resultTable.getInt(rowIndex, column);
	}

	public BigDecimal getBigDecimal(String column) throws SQLException {
		return this.resultTable.getBigDecimal(rowIndex, column);
	}

	public BigDecimal getBigDecimal(int column) throws SQLException {
		return this.resultTable.getBigDecimal(rowIndex, column);
	}

	public Boolean getBoolean(String column) throws SQLException {
		return this.resultTable.getBoolean(rowIndex, column);
	}

	public Boolean getBoolean(int column) throws SQLException {
		return this.resultTable.getBoolean(rowIndex, column);
	}

	public Long getLong(String column) throws SQLException {
		return this.resultTable.getLong(rowIndex, column);
	}

	public Long getLong(int column) throws SQLException {
		return this.resultTable.getLong(rowIndex, column);
	}

	public Float getFloat(String column) throws SQLException {
		return this.resultTable.getFloat(rowIndex, column);
	}

	public Float getFloat(int column) throws SQLException {
		return this.resultTable.getFloat(rowIndex, column);
	}

	public Double getDouble(String column) throws SQLException {
		return this.resultTable.getDouble(rowIndex, column);
	}

	public Double getDouble(int column) throws SQLException {
		return this.resultTable.getDouble(rowIndex, column);
	}

	public byte[] getBlob(String column) throws SQLException {
		return this.resultTable.getBlob(rowIndex, column);
	}

	public byte[] getBlob(int column) throws SQLException {
		return this.resultTable.getBlob(rowIndex, column);
	}

	public Object getObject(String column) throws SQLException {
		return this.resultTable.getObject(rowIndex, column);
	}

}
