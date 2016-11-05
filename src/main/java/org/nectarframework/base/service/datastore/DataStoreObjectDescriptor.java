package org.nectarframework.base.service.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nectarframework.base.service.datastore.DataStoreObjectDescriptorColumn.Type;
import org.nectarframework.base.tools.Sanity;
import org.nectarframework.base.tools.Tuple;

public final class DataStoreObjectDescriptor {
	private String table;
	private DataStoreObjectDescriptorKey primaryKey;

	private ArrayList<DataStoreObjectDescriptorColumn> colDescriptors;
	private HashMap<String, DataStoreObjectDescriptorColumn> columnNameLookupMap;
	private Class<? extends DataStoreObject> dsoClass;

	public DataStoreObjectDescriptor(String table, DataStoreObjectDescriptorKey primaryKey,
			DataStoreObjectDescriptorColumn[] colDescriptors, Class<? extends DataStoreObject> dsoClass) {
		Sanity.nn(table);
		Sanity.nn(primaryKey);
		Sanity.nn(dsoClass);
		Sanity.nn(colDescriptors);
		
		this.table = table;
		this.primaryKey = primaryKey;
		this.dsoClass = dsoClass;

		if (colDescriptors.length == 0) {
			throw new IllegalArgumentException();
		}
		this.colDescriptors = new ArrayList<>(colDescriptors.length);
		for (DataStoreObjectDescriptorColumn cd : colDescriptors) {
			this.colDescriptors.add(cd);
		}
		
		columnNameLookupMap = new HashMap<>();
		this.colDescriptors.forEach(c -> columnNameLookupMap.put(c.getName(), c));
	}

	public int getColumnCount() {
		return colDescriptors.size();
	}

	public List<String> getColumnNames() {
		LinkedList<String> l = new LinkedList<>();
		colDescriptors.forEach(c -> l.add(c.getName()));
		return l;
	}

	protected List<Tuple<String, Type>> getColumnNamesAndTypes() {
		LinkedList<Tuple<String, Type>> l = new LinkedList<>();
		colDescriptors.forEach(c -> l.add(new Tuple<String, Type>(c.getName(), c.getType())));
		return l;
	}

	public List<Type> getColumnTypes() {
		LinkedList<Type> l = new LinkedList<>();
		colDescriptors.forEach(c -> l.add(c.getType()));
		return l;
	}

	public List<DataStoreObjectDescriptorColumn> getColumnDescriptors() {
		return colDescriptors;
	}

	public int getColumnIndex(String colName) {
		DataStoreObjectDescriptorColumn c = columnNameLookupMap.get(colName);
		if (c == null) {
			throw new IndexOutOfBoundsException();
		}
		return c.getIndex();
	}

	public String getTableName() {
		return table;
	}

	public Class<? extends DataStoreObject> getDsoClass() {
		return dsoClass;
	}

	public DataStoreObjectDescriptorKey getPrimaryKey() {
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

	public int getNullAllowedCount() {
		int n = 0;
		for (DataStoreObjectDescriptorColumn c : colDescriptors) {
			if (c.isNullAllowed())
				n++;
		}
		return n;
	}

	public boolean isNullAllowed(int columnIndex) {
		return colDescriptors.get(columnIndex).isNullAllowed();
	}
}
