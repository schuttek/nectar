package org.nectarframework.base.service.xml;

import org.nectarframework.base.service.Service;

public abstract class ConnectionService extends Service {
	
	protected abstract boolean keepRunning();

	protected abstract void notifyDisconnect(Connection connection);

	protected abstract void handlePacket(Connection connection, byte packetOptions, byte[] packet);

	protected abstract XmlService getXmlService();

	public abstract boolean isClient();

	public abstract boolean isEncryptionEnabled();	
	public abstract boolean isCompressionEnabled();	

}
