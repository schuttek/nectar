package org.nectarframework.base.service.event;

public class EventChannel {

	private String channelName;
	
	public EventChannel(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelName() {
		return channelName;
	}

	protected void setChannelName(String channelName) {
		this.channelName = channelName;
	}
}
