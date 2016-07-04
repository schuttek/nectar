package org.nectarframework.base.service.internode;

import java.nio.channels.SocketChannel;

public class ChangeRequest {

	public SocketChannel socket;

	public enum Type {
		REGISTER, CHANGEOPS
	};

	public Type type;
	public int ops;

	public ChangeRequest(SocketChannel socket, Type type, int ops) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
	}

}
