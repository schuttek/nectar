package org.nectarframework.base.service.internode;

import java.nio.channels.SocketChannel;


public class NodeContext {

	protected SocketChannel socketChannel;
	protected String nodeName;
	protected String nodeGroup;
	
	public NodeContext(SocketChannel socketChannel, String nodeName, String nodeGroup) {
		
	}
}
