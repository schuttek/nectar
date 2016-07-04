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
}
