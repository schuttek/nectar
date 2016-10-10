package org.nectarframework.base.service.datastore;

import org.nectarframework.base.service.datastore.DataStoreObjectDescriptorColumn.Type;

public class DataStoreObjectDescriptorKey {
	private DataStoreObjectDescriptorColumn colDescriptor;
	private boolean autoIncrement;
	private int keyLength;

	public DataStoreObjectDescriptorKey(DataStoreObjectDescriptorColumn colDescriptor, int keyLength, boolean autoIncrement) {
		this.colDescriptor = colDescriptor;
		this.keyLength = keyLength;
		this.autoIncrement = autoIncrement;
	}

	public DataStoreObjectDescriptorColumn getColumnDescriptor() {
		return colDescriptor;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public int compareTo(Object k1, Object k2) {
		return colDescriptor.getType().compareTo(k1, k2);
	}

	public Type getType() {
		return colDescriptor.getType();
	}

	public String getColumnName() {
		return colDescriptor.getName();
	}

	public int getKeyLength() {
		return keyLength;
	}
}
