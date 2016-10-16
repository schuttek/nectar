package org.nectarframework.base.service.datastore;

import java.sql.SQLException;

import org.nectarframework.base.service.sql.ResultRow;
import org.nectarframework.base.service.sql.SqlPreparedStatement;
import org.nectarframework.base.tools.Base64;
import org.nectarframework.base.tools.BitMap;
import org.nectarframework.base.tools.ByteArray;
import org.nectarframework.base.tools.StringTools;

// well, ain't that a long name...

public class DataStoreObjectDescriptorColumn {
	private String name;
	private Type type;
	private boolean nullAllowed;
	private int index;

	public DataStoreObjectDescriptorColumn(String name, Type type, boolean nullAllowed) {
		super();
		this.name = name;
		this.type = type;
		this.nullAllowed = nullAllowed;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public boolean isNullAllowed() {
		return nullAllowed;
	}

	public int getIndex() {
		return index;
	}

	protected void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "DataStoreObjectDescriptorColumn [name=" + name + ", type=" + type + ", nullAllowed=" + nullAllowed
				+ "]";
	}

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

		public ByteArray toBytes(Object value, ByteArray bq) {
			int len, i;
			switch (this) {
			case BOOLEAN:
				bq.add((Boolean) value);
				return bq;
			case BYTE:
				bq.add((Byte) value);
				return bq;
			case SHORT:
				bq.add((Short) value);
				return bq;
			case INT:
				bq.add((Integer) value);
				return bq;
			case LONG:
				bq.add((Long) value);
				return bq;
			case FLOAT:
				bq.add((Float) value);
				return bq;
			case DOUBLE:
				bq.add((Double) value);
				return bq;
			case STRING:
				bq.add((String) value);
				return bq;
			case BLOB:
				bq.addByteArray((byte[]) value);
				return bq;
			case BYTE_ARRAY:
				bq.addByteArray((byte[]) value);
				return bq;
			case SHORT_ARRAY:
				len = ((short[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((short[]) value)[i]);
				}
				return bq;
			case INT_ARRAY:
				len = ((int[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((int[]) value)[i]);
				}
				return bq;
			case LONG_ARRAY:
				len = ((long[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((long[]) value)[i]);
				}
				return bq;
			case FLOAT_ARRAY:
				len = ((float[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((float[]) value)[i]);
				}
				return bq;
			case DOUBLE_ARRAY:
				len = ((double[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((double[]) value)[i]);
				}
				return bq;
			case STRING_ARRAY:
				len = ((String[]) value).length;
				bq.add(len);
				for (i = 0; i < len; i++) {
					bq.add(((String[]) value)[i]);
				}
				return bq;
			}
			throw new IllegalArgumentException("invalid type:" + this.toString());
		}

		public Object fromResultRow(ResultRow rr, String colName) throws SQLException {
			int len, i;
			ByteArray ba = null;
			if (rr.isNull(colName)) {
				return null;
			}
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
				throw new IllegalArgumentException(this.toString() + " type cannot be a key");
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
				ba.add(((short[]) value).length);
				for (i = 0; i < ((short[]) value).length; i++) {
					ba.add(((short[]) value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getByteArray());
			case INT_ARRAY:
				ba = new ByteArray();
				ba.add(((int[]) value).length);
				for (i = 0; i < ((int[]) value).length; i++) {
					ba.add(((int[]) value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getByteArray());
			case LONG_ARRAY:
				ba = new ByteArray();
				ba.add(((long[]) value).length);
				for (i = 0; i < ((long[]) value).length; i++) {
					ba.add(((long[]) value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getByteArray());
			case FLOAT_ARRAY:
				ba = new ByteArray();
				ba.add(((float[]) value).length);
				for (i = 0; i < ((float[]) value).length; i++) {
					ba.add(((float[]) value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getByteArray());
			case DOUBLE_ARRAY:
				ba = new ByteArray();
				ba.add(((double[]) value).length);
				for (i = 0; i < ((double[]) value).length; i++) {
					ba.add(((double[]) value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getByteArray());
			case STRING_ARRAY:
				ba = new ByteArray();
				ba.add(((String[]) value).length);
				for (i = 0; i < ((String[]) value).length; i++) {
					ba.add(((String[]) value)[i]);
				}
				mps.setBytes(mpsIndex, ba.getByteArray());

			}
			throw new IllegalArgumentException("invalid type:" + this.toString());

		}

		public String stringValue(Object value) {
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
				return Base64.encode((byte[]) value);
			case SHORT_ARRAY:
			case INT_ARRAY:
			case LONG_ARRAY:
			case FLOAT_ARRAY:
			case DOUBLE_ARRAY:
			case STRING_ARRAY:
				throw new IllegalArgumentException(this.toString() + " type cannot be a key");
			}
			throw new IllegalArgumentException("invalid type:" + this.toString());
		}

		public Object copyObject(Object o) {

			if (o == null) {
				return null;
			}
			int len, t;
			switch (this) {
			// raw types
			case BOOLEAN:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
			case STRING:
				return o;
			case BLOB:
			case BYTE_ARRAY:
				len = ((byte[]) o).length;
				byte arrb[] = new byte[len];
				System.arraycopy((byte[]) o, 0, arrb, 0, len);
				return arrb;
			case DOUBLE_ARRAY:
				len = ((double[]) o).length;
				double arrd[] = new double[len];
				for (t = 0; t < len; t++)
					arrd[t] = ((double[]) o)[t];
				return arrd;
			case FLOAT_ARRAY:
				len = ((float[]) o).length;
				float arrf[] = new float[len];
				for (t = 0; t < len; t++)
					arrf[t] = ((float[]) o)[t];
				return arrf;
			case INT_ARRAY:
				len = ((int[]) o).length;
				int[] arri = new int[len];
				for (t = 0; t < len; t++)
					arri[t] = ((int[]) o)[t];
				return arri;
			case LONG_ARRAY:
				len = ((long[]) o).length;
				long arrl[] = new long[len];
				for (t = 0; t < len; t++)
					arrl[t] = ((long[]) o)[t];
				return arrl;
			case SHORT_ARRAY:
				len = ((short[]) o).length;
				short arrs[] = new short[len];
				for (t = 0; t < len; t++)
					arrs[t] = ((short[]) o)[t];
				return arrs;
			case STRING_ARRAY:
				len = ((String[]) o).length;
				String arrss[] = new String[len];
				for (t = 0; t < len; t++)
					arrss[t] = new String(((String[]) o)[t]);
				return arrss;
			}
			return null;
		}
		

		public int compareTo(Object a, Object b) {
			switch (this) {
			case BOOLEAN:
				return ((Boolean) a).compareTo((Boolean) b);
			case STRING:
				return ((String) a).compareTo((String) b);
			case BYTE:
				return ((Byte) a).compareTo((Byte) b);
			case DOUBLE:
				return ((Double) a).compareTo((Double) b);
			case FLOAT:
				return ((Float) a).compareTo((Float) b);
			case INT:
				return ((Integer) a).compareTo((Integer) b);
			case LONG:
				return ((Long) a).compareTo((Long) b);
			case SHORT:
				return ((Short) a).compareTo((Short) b);
			default:
				return 0;
			}
		}
		
	}
}