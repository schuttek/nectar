package org.nectarframework.base.service.datastore;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nectarframework.base.service.sql.SqlPreparedStatement;
import org.nectarframework.base.service.datastore.DataStoreObjectDescriptorColumn.Type;
import org.nectarframework.base.service.sql.ResultRow;
import org.nectarframework.base.tools.Base64;
import org.nectarframework.base.tools.ByteArray;
import org.nectarframework.base.tools.StringTools;
import org.nectarframework.base.tools.Tuple;

public final class DataStoreObjectDescriptor {
	private String table;
	private DataStoreObjectDescriptorKey primaryKey;

	private LinkedList<DataStoreObjectDescriptorColumn> colDescriptors;
	private HashMap<String, DataStoreObjectDescriptorColumn> columnNameLookupMap;
	private Class<? extends DataStoreObject> dsoClass;

	public DataStoreObjectDescriptor(String table, DataStoreObjectDescriptorKey primaryKey,
			DataStoreObjectDescriptorColumn[] colDescriptorsArray, Class<? extends DataStoreObject> dsoClass) {
		this.table = table;
		this.primaryKey = primaryKey;
		this.dsoClass = dsoClass;

		colDescriptors = new LinkedList<>();
		colDescriptors.addAll(colDescriptors);

		columnNameLookupMap = new HashMap<>();
		colDescriptors.forEach(c -> columnNameLookupMap.put(c.getName(), c));
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
			if (c.isNullAllowed()) n++;
		}
		return n;
	}
	
	public boolean isNullAllowed(int columnIndex) {
		return colDescriptors.get(columnIndex).isNullAllowed();
	}
}
