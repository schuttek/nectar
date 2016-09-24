package org.nectarframework.base.service.datastore;

import org.nectarframework.base.service.datastore.DataStoreObjectDescriptor.Type;

public class DataStoreKey {
	private String columnName;
	private Type type;
	private int length;
	private boolean autoIncrement;

	public DataStoreKey(String columnName, Type type, int length, boolean autoIncrement) {
		this.columnName = columnName;
		this.type = type;
		this.length = length;
		this.autoIncrement = autoIncrement;
	}

	public String getColumnName() {
		return columnName;
	}

	public Type getType() {
		return type;
	}

	public int getLength() {
		return length;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public int compareTo(Object a, Object b) {
		switch (type) {
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
