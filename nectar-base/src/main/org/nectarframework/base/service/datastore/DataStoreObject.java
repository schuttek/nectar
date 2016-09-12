package org.nectarframework.base.service.datastore;

import java.sql.SQLException;

import org.nectarframework.base.Main;
import org.nectarframework.base.service.cache.CacheableObject;
import org.nectarframework.base.service.datastore.DataStoreObjectDescriptor.Type;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.sql.ResultRow;
import org.nectarframework.base.tools.BitMap;
import org.nectarframework.base.tools.ByteArray;

public abstract class DataStoreObject implements CacheableObject {
	private DataStoreObjectDescriptor dsod = null;
	protected Object[] data;
	protected byte[] nullMap;

	public DataStoreObject() {
	}

	public abstract void initDataStoreObjectDescriptor(DataStoreService dss);

	public final void setDataStoreObjectDescriptor(DataStoreObjectDescriptor dsod) {
		this.dsod = dsod;
	}

	public final DataStoreObject copy() {
		DataStoreObject dso;
		try {
			dso = this.getClass().newInstance();
		} catch (InstantiationException e) {
			Log.fatal(e);
			Main.exit();
			return null;
		} catch (IllegalAccessException e) {
			Log.fatal(e);
			Main.exit();
			return null;
		}

		dso.dsod = dsod;
		this.nullMap = new byte[dso.nullMap.length];
		System.arraycopy(dso.nullMap, 0, this.nullMap, 0, dso.nullMap.length);
		data = new Object[dsod.getColumnCount()];
		Type[] colType = dsod.getColumnTypes();
		int len, t;
		for (int i = 0; i < data.length; i++) {
			switch (colType[i]) {
			// raw types
			case BOOLEAN:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
			case STRING:
				data[i] = dso.data[i];
				break;
			case BLOB:
			case BYTE_ARRAY:
				if (BitMap.is(dso.nullMap, i)) {
					data[i] = null;
				} else {
					len = ((byte[]) dso.data[i]).length;
					data[i] = new byte[len];
					System.arraycopy((byte[]) dso.data[i], 0, (byte[]) data[i], 0, len);
				}
				break;
			case DOUBLE_ARRAY:
				if (BitMap.is(dso.nullMap, i)) {
					data[i] = null;
				} else {
					len = ((double[]) dso.data[i]).length;
					data[i] = new double[len];
					for (t = 0; t < len; t++)
						((double[]) data[i])[t] = ((double[]) dso.data[i])[t];
				}
				break;
			case FLOAT_ARRAY:
				if (BitMap.is(dso.nullMap, i)) {
					data[i] = null;
				} else {

					len = ((float[]) dso.data[i]).length;
					data[i] = new float[len];
					for (t = 0; t < len; t++)
						((float[]) data[i])[t] = ((float[]) dso.data[i])[t];
				}
				break;
			case INT_ARRAY:
				if (BitMap.is(dso.nullMap, i)) {
					data[i] = null;
				} else {
					len = ((int[]) dso.data[i]).length;
					data[i] = new int[len];
					for (t = 0; t < len; t++)
						((int[]) data[i])[t] = ((int[]) dso.data[i])[t];
				}
				break;
			case LONG_ARRAY:
				if (BitMap.is(dso.nullMap, i)) {
					data[i] = null;
				} else {
					len = ((long[]) dso.data[i]).length;
					data[i] = new long[len];
					for (t = 0; t < len; t++)
						((long[]) data[i])[t] = ((long[]) dso.data[i])[t];
				}
				break;
			case SHORT_ARRAY:
				if (BitMap.is(dso.nullMap, i)) {
					data[i] = null;
				} else {
					len = ((short[]) dso.data[i]).length;
					data[i] = new short[len];
					for (t = 0; t < len; t++)
						((short[]) data[i])[t] = ((short[]) dso.data[i])[t];
				}
				break;
			case STRING_ARRAY:
				if (BitMap.is(dso.nullMap, i)) {
					data[i] = null;
				} else {
					len = ((String[]) dso.data[i]).length;
					data[i] = new byte[len];
					for (t = 0; t < len; t++)
						((String[]) data[i])[t] = new String(((String[]) dso.data[i])[t]);
				}
				break;
			}
		}
		return dso;
	}

	public final void fromBytes(ByteArray bq) {
		data = new Object[dsod.getColumnCount()];
		Type[] colType = dsod.getColumnTypes();
		nullMap = bq.getByteArray();
		for (int i = 0; i < data.length; i++) {
			if (BitMap.is(nullMap, i)) {
				data[i] = null;
			} else {
				data[i] = colType[i].fromBytes(bq);
			}
		}
	}

	public final ByteArray toBytes() {
		ByteArray bq = new ByteArray();
		Type[] colType = dsod.getColumnTypes();
		bq.addByteArray(nullMap);
		for (int i = 0; i < data.length; i++) {
			if (!BitMap.is(nullMap, i)) {
				colType[i].toBytes(data[i], bq);
			}
		}
		return bq;
	}

	public final void loadFromResultRow(ResultRow rr) throws SQLException {
		data = new Object[dsod.getColumnCount()];
		Type[] colType = dsod.getColumnTypes();
		String[] colName = dsod.getColumnNames();
		nullMap = BitMap.init(dsod.getColumnCount());
		for (int i = 0; i < data.length; i++) {
			data[i] = colType[i].fromResultRow(rr, colName[i]);
			if (data[i] == null) {
				BitMap.set(nullMap, i);
			} else {
				BitMap.clear(nullMap, i);
			}
		}
	}

	protected final String getKeyColumnName() {
		return dsod.getColumnNames()[0];
	}

	protected final String[] getColumnNames() {
		return dsod.getColumnNames();
	}

	protected final String getTableName() {
		return dsod.getTableName();
	}

	public final Boolean getBoolean(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof Boolean) {
			return (Boolean) o;
		}
		throw new ClassCastException();
	}

	public final Byte getByte(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof Byte) {
			return (Byte) o;
		}
		throw new ClassCastException();
	}

	public final Short getShort(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof Short) {
			return (Short) o;
		}
		throw new ClassCastException();
	}

	public final Integer getInteger(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof Integer) {
			return (Integer) o;
		}
		throw new ClassCastException();
	}

	public final Long getLong(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof Long) {
			return (Long) o;
		}
		throw new ClassCastException();
	}

	public final Float getFloat(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof Float) {
			return (Float) o;
		}
		throw new ClassCastException();
	}

	public final Double getDouble(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof Double) {
			return (Double) o;
		}
		throw new ClassCastException();
	}

	public final String getString(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof String) {
			return (String) o;
		}
		throw new ClassCastException();
	}

	public final byte[] getBlob(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof byte[]) {
			return (byte[]) o;
		}
		throw new ClassCastException();
	}

	public final byte[] getByteArray(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof byte[]) {
			return (byte[]) o;
		}
		throw new ClassCastException();
	}

	public final short[] getShortArray(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof short[]) {
			return (short[]) o;
		}
		throw new ClassCastException();
	}

	public final int[] getIntArray(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof int[]) {
			return (int[]) o;
		}
		throw new ClassCastException();
	}

	public final long[] getLongArray(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof long[]) {
			return (long[]) o;
		}
		throw new ClassCastException();
	}

	public final float[] getFloatArray(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof float[]) {
			return (float[]) o;
		}
		throw new ClassCastException();
	}

	public final double[] getDoubleArray(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof double[]) {
			return (double[]) o;
		}
		throw new ClassCastException();
	}

	public final String[] getStringArray(String columnName) {
		Object o = data[dsod.getColumnIndex(columnName)];
		if (o == null) {
			return null;
		}
		if (o instanceof String[]) {
			return (String[]) o;
		}
		throw new ClassCastException();
	}

	public final Object getObject(int index) {
		return data[index];
	}

	public final Object getObject(String colName) {
		return data[dsod.getColumnIndex(colName)];
	}

	protected final void set(String colName, Object value) {
		set(dsod.getColumnIndex(colName), value);
	}

	protected final void set(int columnIndex, Object value) {
		if (value == null && !dsod.isNullAllowed(columnIndex)) {
			throw new IllegalArgumentException("Value on this field may not be null");
		}
		data[columnIndex] = value;
	}

	public DataStoreObjectDescriptor getDataStoreObjectDescriptor() {
		return dsod;
	}

	public Object getPrimaryKey() {
		return data[dsod.getColumnIndex(dsod.getPrimaryKey().getColumnName())];
	}
}
