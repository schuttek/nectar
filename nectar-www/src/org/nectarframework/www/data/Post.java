package org.nectarframework.www.data;

import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.datastore.DataStoreKey;
import org.nectarframework.base.service.datastore.DataStoreObject;
import org.nectarframework.base.service.datastore.DataStoreObjectDescriptor;
import org.nectarframework.base.service.datastore.DataStoreService;

// THIS CLASS IS AUTO GENERATED by nectar.base.service.datastore.dsobuilder.DataStoreObjectBuilder, and configured by config/dataStoreObjects.xml . 
// Edits to this file will be overwritten!
public class Post extends DataStoreObject {
	
	public Post() {
	}
	
	@Override
	public void initDataStoreObjectDescriptor(DataStoreService dss) {
		dss.initDataStoreObjectDescriptor(new DataStoreObjectDescriptor("org_nectarframework_www_post", new DataStoreKey("postId", DataStoreObjectDescriptor.Type.LONG, 8, true), new String[] { "postId", "threadId", "authorUserId", "createdTimestamp", "lastEditTimestamp", "message" }, new DataStoreObjectDescriptor.Type[] {DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.LONG, DataStoreObjectDescriptor.Type.STRING}, new boolean[] {false, false, false, false, true, false}, Post.class));
	}
	
	public static Post load(Long key) throws Exception {
		DataStoreService dss = (DataStoreService) ServiceRegister.getService(DataStoreService.class);
		DataStoreObject dso = dss.loadDSO(dss.getDataStoreObjectDescriptor(Post.class), key);
		if (dso == null) 
			return null;
		return (Post)dso;
	}
	
	public Long getPostId() {
		return getLong("postId");
	}
	
	public void setPostId(Long postId) {
		set("postId", postId);
	}
	
	public Long getThreadId() {
		return getLong("threadId");
	}
	
	public void setThreadId(Long threadId) {
		set("threadId", threadId);
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
	
	public String getMessage() {
		return getString("message");
	}
	
	public void setMessage(String message) {
		set("message", message);
	}
	
	public org.nectarframework.www.data.Thread getThread() throws Exception {
		return org.nectarframework.www.data.Thread.load(getLong("threadId"));
	}
	
	public org.nectarframework.www.data.User getAuthor() throws Exception {
		return org.nectarframework.www.data.User.load(getLong("authorUserId"));
	}
	
}
