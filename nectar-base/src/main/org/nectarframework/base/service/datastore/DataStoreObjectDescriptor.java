package org.nectarframework.base.service.datastore;

import java.sql.SQLException;
import java.util.HashMap;

import org.nectarframework.base.service.sql.SqlPreparedStatement;
import org.nectarframework.base.service.sql.ResultRow;
import org.nectarframework.base.tools.ByteArray;
import org.nectarframework.base.tools.StringTools;

public class DataStoreObjectDescriptor {
	private String table;
	private DataStoreKey primaryKey;
	private String[] colNames;
	private Type[] colTypes;
	private HashMap<String, Integer> colMap;
	private Class<? extends DataStoreObject> dsoClass;

	public enum Type {
		BOOLEAN, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, STRING, BLOB, BYTE_ARRAY, SHORT_ARRAY, INT_ARRAY, LONG_ARRAY, FLOAT_ARRAY, DOUBLE_ARRAY, STRING_ARRAY;

		public Object fromBytes(ByteArray bq) {
			int len, i;
			switch (this) {
			case BOOLEAN:
				return new Boolean(bq.getByte() != 0);
			case BYTE:
				return new Byte(bq.getByte());
			case SHORT:
				return new Short(bq.getShort());
			case INT:
				return new Integer(bq.getInt());
			case LONG:
				return new Long(bq.getLong());
			case FLOAT:
				return new Float(bq.getFloat());
			case DOUBLE:
				return new Double(bq.getDouble());
			case STRING:
				return bq.getString();
			case BLOB:
				len = bq.getInt();
				return bq.remove(len);
			case BYTE_ARRAY:
				len = bq.getInt();
				return bq.remove(len);
			case SHORT_ARRAY:
				len = bq.getInt();
				short[] shortArray = new short[len];
				for (i = 0; i < len; i++) {
					shortArray[i] = bq.getShort();
				}
				return shortArray;
			case INT_ARRAY:
				len = bq.getInt();
				int[] intArray = new int[len];
				for (i = 0; i < len; i++) {
					intArray[i] = bq.getInt();
				}
				return intArray;
			case LONG_ARRAY:
				len = bq.getInt();
				long[] longArray = new long[len];
				for (i = 0; i < len; i++) {
					longArray[i] = bq.getLong();
				}
				return longArray;
			case FLOAT_ARRAY:
				len = bq.getInt();
				float[] floatArray = new float[len];
				for (i = 0; i < len; i++) {
					floatArray[i] = bq.getFloat();
				}
				return floatArray;
			case DOUBLE_ARRAY:
				len = bq.getInt();
				double[] doubleArray = new double[len];
				for (i = 0; i < len; i++) {
					doubleArray[i] = bq.getDouble();
				}
				return doubleArray;
			case STRING_ARRAY:
				len = bq.getInt();
				String[] stringArray = new String[len];
				for (i = 0; i < len; i++) {
					stringArray[i] = bq.getString();
				}
				return stringArray;
			}
			throw new IllegalArgumentException("invalid type:" + this.toString());
		}

		public void toBytes(Object value, ByteArray bq) {
			int len, i;
			switch (this) {
			case BOOLEAN:
				bq.add((Boolean) value);
				return;
			case BYTE:
				bq.add((Byte) value);
				return;
			case SHORT:
				bq.add((Short) value);
				return;
			case INT:
				bq.add((Integer) value);
				return;
			case LONG:
				bq.add((Long) value);
				return;
			case FLOAT:
				bq.add((Float) value);
				return;
			case DOUBLE:
				bq.add((Double) value);
				return;
			case STRING:
				bq.add((String) value);
				return;
			case BLOB:
				bq.add(((byte[]) value).length);
				bq.add((byte[]) value);
				return;
			case BYTE_ARRAY:
				bq.add(((byte[]) value).length);
				bq.add((byte[]) value);
				return;
			case SHORT_ARRAY:
				len = ((short[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((short[]) value)[i]);
				}
				return;
			case INT_ARRAY:
				len = ((int[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((int[]) value)[i]);
				}
				return;
			case LONG_ARRAY:
				len = ((long[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((long[]) value)[i]);
				}
				return;
			case FLOAT_ARRAY:
				len = ((float[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((float[]) value)[i]);
				}
				return;
			case DOUBLE_ARRAY:
				len = ((double[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((double[]) value)[i]);
				}
				return;
			case STRING_ARRAY:
				len = ((String[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((String[]) value)[i]);
				}
				return;
			}
			throw new IllegalArgumentException("invalid type:" + this.toString());
		}

		public Object fromResultRow(ResultRow rr, String colName) throws SQLException {
			int len, i;
			ByteArray ba = null;
			switch (this) {
			case BOOLEAN:
				return rr.getBoolean(colName);
			case BYTE:
				return rr.getByte(colName);
			case SHORT:
				return rr.getShort(colName);
			case INT:
				return rr.getInt(colName);
			case LONG:
				return rr.getLong(colName);
			case FLOAT:
				return rr.getFloat(colName);
			case DOUBLE:
				return rr.getDouble(colName);
			case STRING:
				return rr.getString(colName);
			case BLOB:
				return rr.getBlob(colName);
			case BYTE_ARRAY:
				return rr.getBlob(colName);
			case SHORT_ARRAY:
				ba = new ByteArray(rr.getBlob(colName));
				len = ba.getInt();
				short[] shortArray = new short[len];
				for (i = 0; i < len; i++) {
					shortArray[i] = ba.getShort();
				}
				return shortArray;
			case INT_ARRAY:
				ba = new ByteArray(rr.getBlob(colName));
				len = ba.getInt();
				int[] intArray = new int[len];
				for (i = 0; i < len; i++) {
					intArray[i] = ba.getInt();
				}
				return intArray;
			case LONG_ARRAY:
				ba = new ByteArray(rr.getBlob(colName));
				len = ba.getInt();
				long[] longArray = new long[len];
				for (i = 0; i < len; i++) {
					longArray[i] = ba.getLong();
				}
				return longArray;

			case FLOAT_ARRAY:
				ba = new ByteArray(rr.getBlob(colName));
				len = ba.getInt();
				float[] floatArray = new float[len];
				for (i = 0; i < len; i++) {
					floatArray[i] = ba.getFloat();
				}
				return floatArray;
			case DOUBLE_ARRAY:
				ba = new ByteArray(rr.getBlob(colName));
				len = ba.getInt();
				double[] doubleArray = new double[len];
				for (i = 0; i < len; i++) {
					doubleArray[i] = ba.getDouble();
				}
				return doubleArray;
			case STRING_ARRAY:
				ba = new ByteArray(rr.getBlob(colName));
				len = ba.getInt();
				String[] stringArray = new String[len];
				for (i = 0; i < len; i++) {
					stringArray[i] = ba.getString();
				}
				return stringArray;
			}
			throw new IllegalArgumentException("invalid type:" + this.toString());
		}

		public String toCacheKeyString(Object value) {
			switch (this) {
			case BOOLEAN:
				return ((Boolean) value).toString();
			case BYTE:
				return ((Byte) value).toString();
			case SHORT:
				return ((Short) value).toString();
			case INT:
				return ((Integer) value).toString();
			case LONG:
				return ((Long) value).toString();
			case FLOAT:
				return ((Float) value).toString();
			case DOUBLE:
				return ((Double) value).toString();
			case STRING:
				return (String) value;
			case BYTE_ARRAY:
			case BLOB:
				return StringTools.toHexString((byte[]) value);
			case SHORT_ARRAY:
			case INT_ARRAY:
			case LONG_ARRAY:
			case FLOAT_ARRAY:
			case DOUBLE_ARRAY:
			case STRING_ARRAY:
				throw new IllegalArgumentException(this.toString() +" type cannot be a key");
			}
			throw new IllegalArgumentException("invalid type:" + this.toString());
		}

		public void toMps(SqlPreparedStatement mps, int mpsIndex, Object value) {
			int i;
			ByteArray ba;
			switch (this) {
			case BOOLEAN:
				mps.setBoolean(mpsIndex, (Boolean) value);
				return;
			case BYTE:
				mps.setByte(mpsIndex, (Byte) value);
				return;
			case SHORT:
				mps.setShort(mpsIndex, (Short) value);
				return;
			case INT:
				mps.setInt(mpsIndex, (Integer) value);
				return;
			case LONG:
				mps.setLong(mpsIndex, (Long) value);
				return;
			case FLOAT:
				mps.setFloat(mpsIndex, (Float) value);
				return;
			case DOUBLE:
				mps.setDouble(mpsIndex, (Double) value);
				return;
			case STRING:
				mps.setString(mpsIndex, value.toString());
				return;
			case BLOB:
			case BYTE_ARRAY:
				mps.setBytes(mpsIndex, (byte[]) value);
				return;
			case SHORT_ARRAY:
				ba = new ByteArray();
				ba.add(((short[])value).length);
				for (i=0;i<((short[])value).length;i++) {
					ba.add(((short[])value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getBytes());
			case INT_ARRAY:
				ba = new ByteArray();
				ba.add(((int[])value).length);
				for (i=0;i<((int[])value).length;i++) {
					ba.add(((int[])value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getBytes());
			case LONG_ARRAY:
				ba = new ByteArray();
				ba.add(((long[])value).length);
				for (i=0;i<((long[])value).length;i++) {
					ba.add(((long[])value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getBytes());
			case FLOAT_ARRAY:
				ba = new ByteArray();
				ba.add(((float[])value).length);
				for (i=0;i<((float[])value).length;i++) {
					ba.add(((float[])value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getBytes());
			case DOUBLE_ARRAY:
				ba = new ByteArray();
				ba.add(((double[])value).length);
				for (i=0;i<((double[])value).length;i++) {
					ba.add(((double[])value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getBytes());
			case STRING_ARRAY:
				ba = new ByteArray();
				ba.add(((String[])value).length);
				for (i=0;i<((String[])value).length;i++) {
					ba.add(((String[])value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getBytes());
				
			}
			throw new IllegalArgumentException("invalid type:" + this.toString());

		}

	}

	public DataStoreObjectDescriptor(String table, DataStoreKey primaryKey, String[] colNames, Type[] colTypes, Class<? extends DataStoreObject> dsoClass) {
		this.table = table;
		this.primaryKey = primaryKey;
		this.colNames = colNames;
		this.colTypes = colTypes;

		if (colNames.length != colTypes.length) {
			throw new IllegalArgumentException(" colNames.length != colTypes.length ");
		}

		this.colMap = new HashMap<String, Integer>();
		for (int i = 0; i < colNames.length; i++) {
			colMap.put(colNames[i], i);
		}

		this.dsoClass = dsoClass;
	}

	public int getColumnCount() {
		return colNames.length;
	}

	public String[] getColumnNames() {
		return colNames;
	}

	public Type[] getColumnTypes() {
		return colTypes;
	}

	public int getColumnIndex(String colName) {
		Integer i = colMap.get(colName);
		if (i != null) {
			return i;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public String getTableName() {
		return table;
	}

	public Class<? extends DataStoreObject> getDsoClass() {
		return dsoClass;
	}

	public DataStoreKey getPrimaryKey() {
		return this.primaryKey;
	}

	public DataStoreObject newDsoInstance() throws InstantiationException, IllegalAccessException {
		DataStoreObject dso = dsoClass.newInstance();
		dso.setDataStoreObjectDescriptor(this);
		return dso;
	}

	public String getCacheKey() {
		return "DSO" + getTableName();
	}
}
