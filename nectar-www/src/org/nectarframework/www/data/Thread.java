package org.nectarframework.www.data;

import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.datastore.DataStoreKey;
import org.nectarframework.base.service.datastore.DataStoreObject;
import org.nectarframework.base.service.datastore.DataStoreObjectDescriptor;
import org.nectarframework.base.service.datastore.DataStoreService;

// THIS CLASS IS AUTO GENERATED by nectar.base.service.datastore.dsobuilder.DataStoreObjectBuilder, and configured by config/dataStoreObjects.xml . 
// Edits to this file will be overwritten!
public class Thread extends DataStoreObject {
	
	public Thread() {
	}
	
	@Override
	public void initDataStoreObjectDescriptor(DataStoreService dss) {
		dss.initDataStoreObjectDescriptor(new DataStoreObjectDescriptor("org_nectarframework_www_thread", new DataStoreKey("threadId", DataStoreObjectDescriptor.Type.LONG, 8, false), new String[] { "threadId", "structureId", "authorUserId", "createdTimestamp", "lastEditTimestamp", "lastReplyTimestamp", "sticky", "flags", "subject", "message" }, new DataStoreObjectDescriptor.Type[] {DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.BYTE, DataStoreObjectDescriptor.Type.INT, DataStoreObjectDescriptor.Type.STRING, DataStoreObjectDescriptor.Type.STRING}, new boolean[] {false, false, false, false, true, true, true, true, false, false}, Thread.class));
	}
	
	public static Thread load(Long key) throws Exception {
		DataStoreService dss = (DataStoreService) ServiceRegister.getService(DataStoreService.class);
		DataStoreObject dso = dss.loadDSO(dss.getDataStoreObjectDescriptor(Thread.class), key);
		if (dso == null) 
			return null;
		return (Thread)dso;
	}
	
	public Long getThreadId() {
		return getLong("threadId");
	}
	
	public void setThreadId(Long threadId) {
		set("threadId", threadId);
	}
	
	public Long getStructureId() {
		return getLong("structureId");
	}
	
	public void setStructureId(Long structureId) {
		set("structureId", structureId);
	}
	
	public Long getAuthorUserId() {
		return getLong("authorUserId");
	}
	
	public void setAuthorUserId(Long authorUserId) {
		set("authorUserId", authorUserId);
	}
	
	public Long getCreatedTimestamp() {
		return getLong("createdTimestamp");
	}
	
	public void setCreatedTimestamp(Long createdTimestamp) {
		set("createdTimestamp", createdTimestamp);
	}
	
	public Long getLastEditTimestamp() {
		return getLong("lastEditTimestamp");
	}
	
	public void setLastEditTimestamp(Long lastEditTimestamp) {
		set("lastEditTimestamp", lastEditTimestamp);
	}
	
	public Long getLastReplyTimestamp() {
		return getLong("lastReplyTimestamp");
	}
	
	public void setLastReplyTimestamp(Long lastReplyTimestamp) {
		set("lastReplyTimestamp", lastReplyTimestamp);
	}
	
	public Byte getSticky() {
		return getByte("sticky");
	}
	
	public void setSticky(Byte sticky) {
		set("sticky", sticky);
	}
	
	public Integer getFlags() {
		return getInteger("flags");
	}
	
	public void setFlags(Integer flags) {
		set("flags", flags);
	}
	
	public String getSubject() {
		return getString("subject");
	}
	
	public void setSubject(String subject) {
		set("subject", subject);
	}
	
	public String getMessage() {
		return getString("message");
	}
	
	public void setMessage(String message) {
		set("message", message);
	}
	
	public org.nectarframework.www.data.Structure getStructure() throws Exception {
		return org.nectarframework.www.data.Structure.load(getLong("structureId"));
	}
	
	public org.nectarframework.www.data.User getAuthor() throws Exception {
		return org.nectarframework.www.data.User.load(getLong("authorUserId"));
	}
	
}
