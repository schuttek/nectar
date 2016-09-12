package org.nectarframework.base.service.sql;

import java.util.Vector;

import org.nectarframework.base.service.thread.Ticket;

public class AsyncTicket extends Ticket {
	private ResultTable rt = null;
	private int rowCount = -1;
	private Vector<Long> insertIds;


	public ResultTable getResultTable() {
		waitForReady();
		return rt;
	}

	public void setResultTable(ResultTable rt) {
		this.rt = rt;
	}
	
	public void setRowCount(int rows) {
		this.rowCount = rows;
	}
	
	public int getRowCount() {
		return this.rowCount;
	}

	public void setInsertIds(Vector<Long> ids) {
		this.insertIds = ids; 
	}
	
	public Vector<Long> getInsertIds() {
		return this.insertIds;
	}
}
