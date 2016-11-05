package org.nectarframework.base.service.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Vector;

import org.nectarframework.base.service.Log;

public class SqlPreparedStatement {

	public SqlPreparedStatement() {
	}

	public SqlPreparedStatement(String sql) {
		setSql(sql);
	}

	private String sql = null;

	private Vector<HashMap<Integer, PreparedParameter>> paramMapBatch = null;
	private HashMap<Integer, PreparedParameter> paramMap = new HashMap<Integer, PreparedParameter>();

	/**
	 * for inserting or updating several rows in one go, call this method after
	 * setting the parameters of each row.
	 * 
	 * lastInsertId() or updated rows count is not currently supported by batch
	 * inserts.
	 * 
	 * You won't get any real performance improvement unless you wrap the final
	 * insert in a transaction, because of how MySQL handles table locks in
	 * InnoDB. (a transaction locks the table once, processes the whole batch,
	 * then unlocks the table. Without the transaction, MySQL will lock the
	 * table for each insert. Nothing Nectar can do about that...).
	 * 
	 */
	public void addBatch() {
		if (this.paramMapBatch == null) {
			this.paramMapBatch = new Vector<HashMap<Integer, PreparedParameter>>();
		}
		this.paramMapBatch.add(paramMap);
		paramMap = new HashMap<Integer, PreparedParameter>();
	}

	public int getBatchSize() {
		if (this.paramMapBatch == null) {
			return 1;
		}
		return this.paramMapBatch.size();
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}

	public void reset() {
		paramMapBatch = null;
		paramMap = new HashMap<Integer, PreparedParameter>();
	}

	public void _applyToJavaSQLPreparedStatement(java.sql.PreparedStatement ps) throws SQLException {
		if (this.paramMapBatch != null) {
			Log.trace("batch query of " + paramMapBatch.size() + " rows.");
			for (HashMap<Integer, PreparedParameter> map : this.paramMapBatch) {
				this._applyToJavaSQLPreparedStatement(ps, map);
				ps.addBatch();
			}
		} else {
			this._applyToJavaSQLPreparedStatement(ps, paramMap);
		}
	}

	public void _applyToJavaSQLPreparedStatement(java.sql.PreparedStatement ps, int maxBatchSize) throws SQLException {
		if (this.paramMapBatch != null) {
			Log.trace("batch query of " + paramMapBatch.size() + " rows.");
			int k = 0;
			for (HashMap<Integer, PreparedParameter> map : this.paramMapBatch) {
				this._applyToJavaSQLPreparedStatement(ps, map);
				ps.addBatch();
				k++;
				if (k >= maxBatchSize) {
					ps.executeBatch();
					k = 0;
				}
			}
			if (k != 0) {
				ps.executeBatch();
			}
		} else {
			this._applyToJavaSQLPreparedStatement(ps, paramMap);
			ps.executeUpdate();
		}
	}

	private void _applyToJavaSQLPreparedStatement(java.sql.PreparedStatement ps, HashMap<Integer, PreparedParameter> pm) throws SQLException {
		for (Integer index : pm.keySet()) {
			PreparedParameter pp = pm.get(index);
			switch (pp.getType()) {
			case Array:
				ps.setArray(index, (Array) pp.getParameter());
				break;
			case AsciiStream:
				ps.setAsciiStream(index, (InputStream) pp.getParameter());
				break;
			case BigDecimal:
				ps.setBigDecimal(index, (BigDecimal) pp.getParameter());
				break;
			case BinaryStream:
				ps.setBinaryStream(index, (InputStream) pp.getParameter());
				break;
			case Blob:
				if (pp.getParameter() instanceof Blob) {
					ps.setBlob(index, (Blob) pp.getParameter());
				} else {
					ps.setBlob(index, (InputStream) pp.getParameter());
				}
				break;
			case Boolean:
				ps.setBoolean(index, (Boolean) pp.getParameter());
				break;
			case Byte:
				ps.setByte(index, (Byte) pp.getParameter());
				break;
			case Bytes:
				ps.setBytes(index, (byte[]) pp.getParameter());
				break;
			case CharacterStream:
				ps.setCharacterStream(index, (Reader) pp.getParameter());
				break;
			case Clob:
				if (pp.getParameter() instanceof Clob) {
					ps.setClob(index, (Clob) pp.getParameter());
				} else {
					ps.setClob(index, (Reader) pp.getParameter());
				}
				break;
			case Date:
				ps.setDate(index, (Date) pp.getParameter());
				break;
			case Double:
				ps.setDouble(index, (Double) pp.getParameter());
				break;
			case Float:
				ps.setFloat(index, (Float) pp.getParameter());
				break;
			case Int:
				ps.setInt(index, (Integer) pp.getParameter());
				break;
			case Long:
				ps.setLong(index, (Long) pp.getParameter());
				break;
			case NCharacterStream:
				ps.setNCharacterStream(index, (Reader) pp.getParameter());
				break;
			case NClob:
				if (pp.getParameter() instanceof NClob) {
					ps.setNClob(index, (NClob) pp.getParameter());
				} else {
					ps.setNClob(index, (Reader) pp.getParameter());
				}
				break;
			case NString:
				ps.setNString(index, (String) pp.getParameter());
				break;
			case Null:
				ps.setNull(index, Types.NULL);
				break;
			case Ref:
				ps.setRef(index, (Ref) pp.getParameter());
				break;
			case SQLXML:
				ps.setSQLXML(index, (SQLXML) pp.getParameter());
				break;
			case Short:
				ps.setShort(index, (Short) pp.getParameter());
				break;
			case String:
				ps.setString(index, (String) pp.getParameter());
				break;
			case Time:
				ps.setTime(index, (Time) pp.getParameter());
				break;
			case Timestamp:
				ps.setTimestamp(index, (Timestamp) pp.getParameter());
				break;
			case URL:
				ps.setURL(index, (URL) pp.getParameter());
				break;
			}
		}
	}

	public void setArray(int arg0, Array arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Array, arg1));
	}

	public void setAsciiStream(int arg0, InputStream arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.AsciiStream, arg1));
	}

	public void setBigDecimal(int arg0, BigDecimal arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.BigDecimal, arg1));
	}

	public void setBinaryStream(int arg0, InputStream arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.BinaryStream, arg1));
	}

	public void setBlob(int arg0, Blob arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Blob, arg1));
	}

	public void setBlob(int arg0, InputStream arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Blob, arg1));
	}

	public void setBoolean(int arg0, boolean arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Boolean, arg1));
	}

	public void setByte(int arg0, byte arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Byte, arg1));
	}

	public void setBytes(int arg0, byte[] arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Bytes, arg1));
	}

	public void setCharacterStream(int arg0, Reader arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.CharacterStream, arg1));
	}

	public void setClob(int arg0, Clob arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Clob, arg1));
	}

	public void setClob(int arg0, Reader arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Clob, arg1));
	}

	public void setDate(int arg0, Date arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Date, arg1));
	}

	public void setDouble(int arg0, double arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Double, arg1));
	}

	public void setFloat(int arg0, float arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Float, arg1));
	}

	public void setInt(int arg0, int arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Int, arg1));
	}

	public void setLong(int arg0, long arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Long, arg1));
	}

	public void setNCharacterStream(int arg0, Reader arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.NCharacterStream, arg1));
	}

	public void setNClob(int arg0, NClob arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.NClob, arg1));
	}

	public void setNClob(int arg0, Reader arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.NClob, arg1));
	}

	public void setNString(int arg0, String arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.NString, arg1));
	}

	public void setNull(int arg0) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Null, 0));
	}

	public void setRef(int arg0, Ref arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Ref, arg1));
	}

	public void setSQLXML(int arg0, SQLXML arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.SQLXML, arg1));
	}

	public void setShort(int arg0, short arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Short, arg1));
	}

	public void setString(int arg0, String arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.String, arg1));
	}

	public void setTime(int arg0, Time arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Time, arg1));
	}

	public void setTimestamp(int arg0, Timestamp arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.Timestamp, arg1));
	}

	public void setURL(int arg0, URL arg1) {
		paramMap.put(arg0, new PreparedParameter(arg0, PreparedParameter.Type.URL, arg1));
	}

}
