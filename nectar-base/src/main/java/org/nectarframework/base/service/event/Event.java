package org.nectarframework.base.service.event;

import org.nectarframework.base.service.xml.Element;

public interface Event {
	public EventChannel getChannel();
	public Element getMessage();
}
