package org.nectarframework.base.service.datastore;

import org.nectarframework.base.service.datastore.DataStoreObjectDescriptorColumn.Type;

public class DataStoreObjectDescriptorKey {
	private DataStoreObjectDescriptorColumn colDescriptor;
	private boolean autoIncrement;

	public DataStoreObjectDescriptorKey(DataStoreObjectDescriptorColumn colDescriptor, boolean autoIncrement) {
		this.colDescriptor = colDescriptor;
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
	
	

}
