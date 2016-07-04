package org.nectarframework.base.service.cache;

import org.nectarframework.base.service.mysql.ResultTable;

public class ResultTableCacheWrapper extends CacheWrapper {

	private static final long serialVersionUID = 4277656493112928934L;

	public ResultTableCacheWrapper(long l) {
		super(l);
	}

	private ResultTable resultTable;

	public void setResultTable(ResultTable resultTable) {
		this.resultTable = resultTable;
	}

	public ResultTable getResultTable() {
		return resultTable;
	}

	@Override
	public long estimateMemorySize() {
		return resultTable.estimateMemorySize();
	}
}
