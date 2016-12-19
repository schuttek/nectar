package org.nectarframework.base.service.datastore;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.nectarframework.base.Main;
import org.nectarframework.base.element.Element;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.cache.CacheableObject;
import org.nectarframework.base.service.datastore.DataStoreObjectDescriptorColumn.Type;
import org.nectarframework.base.service.sql.ResultRow;
import org.nectarframework.base.tools.BitMap;
import org.nectarframework.base.tools.ByteArray;
import org.nectarframework.base.tools.Tuple;

public abstract class DataStoreObject implements CacheableObject {
	private DataStoreObjectDescriptor dsod = null;
	protected List<Object> data;

	public DataStoreObject() {
	}

	public abstract void initDataStoreObjectDescriptor(DataStoreService dss);

	public final void setDataStoreObjectDescriptor(DataStoreObjectDescriptor dsod) {
		this.dsod = dsod;
	}

	public final DataStoreObject copy() throws InstantiationException, IllegalAccessException {
		DataStoreObject dso = this.getClass().newInstance();
		dso.dsod = dsod;
		dso.data = new ArrayList<>(dsod.getColumnCount());
		List<DataStoreObjectDescriptorColumn> colDescs = dsod.getColumnDescriptors();

		colDescs.forEach(desc -> dso.data.add(desc.getType().copyObject(data.get(desc.getIndex()))));
		return dso;
	}

	public final DataStoreObject fromBytes(ByteArray bq) {
		int colCount = dsod.getColumnCount();
		data = new ArrayList<>(colCount);
		List<Type> colType = dsod.getColumnTypes();
		BitMap nullMap = new BitMap().fromBytes(bq);
		for (int i = 0; i < data.size(); i++) {
			if (nullMap.is(i)) {
				data.add(null);
			} else {
				data.add(colType.get(i).fromBytes(bq));
			}
		}
		return this;
	}

	public final ByteArray toBytes(ByteArray bq) {
		List<Type> colType = dsod.getColumnTypes();
		BitMap nullMap = new BitMap(dsod.getColumnCount());
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i) == null) {
				nullMap.set(i);
			}
		}
		nullMap.toBytes(bq);
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i) != null) {
				colType.get(i).toBytes(data.get(i), bq);
			}
		}
		return bq;
	}

	public final void loadFromResultRow(ResultRow rr) throws SQLException {
		data = new ArrayList<>(dsod.getColumnCount());
		List<DataStoreObjectDescriptorColumn> cds = dsod.getColumnDescriptors();
		for (DataStoreObjectDescriptorColumn cd : cds) {
			data.add(cd.getType().fromResultRow(rr, cd.getName()));
		}
	}

	public final Boolean getBoolean(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof Boolean) {
			return (Boolean) o;
		}
		throw new ClassCastException();
	}

	public final Byte getByte(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof Byte) {
			return (Byte) o;
		}
		throw new ClassCastException();
	}

	public final Short getShort(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof Short) {
			return (Short) o;
		}
		throw new ClassCastException();
	}

	public final Integer getInteger(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof Integer) {
			return (Integer) o;
		}
		throw new ClassCastException();
	}

	public final Long getLong(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof Long) {
			return (Long) o;
		}
		throw new ClassCastException();
	}

	public final Float getFloat(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof Float) {
			return (Float) o;
		}
		throw new ClassCastException();
	}

	public final Double getDouble(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof Double) {
			return (Double) o;
		}
		throw new ClassCastException();
	}

	public final String getString(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof String) {
			return (String) o;
		}
		throw new ClassCastException();
	}

	public final byte[] getBlob(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof byte[]) {
			return (byte[]) o;
		}
		throw new ClassCastException();
	}

	public final byte[] getByteArray(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof byte[]) {
			return (byte[]) o;
		}
		throw new ClassCastException();
	}

	public final short[] getShortArray(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof short[]) {
			return (short[]) o;
		}
		throw new ClassCastException();
	}

	public final int[] getIntArray(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof int[]) {
			return (int[]) o;
		}
		throw new ClassCastException();
	}

	public final long[] getLongArray(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof long[]) {
			return (long[]) o;
		}
		throw new ClassCastException();
	}

	public final float[] getFloatArray(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof float[]) {
			return (float[]) o;
		}
		throw new ClassCastException();
	}

	public final double[] getDoubleArray(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof double[]) {
			return (double[]) o;
		}
		throw new ClassCastException();
	}

	public final String[] getStringArray(String columnName) {
		Object o = getObject(columnName);
		if (o == null) {
			return null;
		}
		if (o instanceof String[]) {
			return (String[]) o;
		}
		throw new ClassCastException();
	}

	public final Object getObject(int index) {
		return data.get(index);
	}

	public final Object getObject(String colName) {
		return data.get(dsod.getColumnIndex(colName));
	}

	protected final void set(String colName, Object value) {
		set(dsod.getColumnIndex(colName), value);
	}

	protected final void set(int columnIndex, Object value) {
		if (value == null && !dsod.isNullAllowed(columnIndex)) {
			throw new IllegalArgumentException("Value on this field may not be null");
		}
		data.set(columnIndex, value);
	}

	public DataStoreObjectDescriptor getDataStoreObjectDescriptor() {
		return dsod;
	}

	public Object getPrimaryKey() {
		return getObject(dsod.getPrimaryKey().getColumnName());
	}

	public Element getElement() {
		Element e = new Element(getClass().getSimpleName().toLowerCase());
		for (Tuple<String, Type> t : dsod.getColumnNamesAndTypes()) {
			e.add(t.getLeft(), t.getRight().stringValue(getObject(t.getLeft())));
		}
		return e;
	}
}
