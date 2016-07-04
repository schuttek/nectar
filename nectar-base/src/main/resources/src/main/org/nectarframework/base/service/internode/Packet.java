package org.nectarframework.base.service.internode;

import java.io.Serializable;

public class Packet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5426059288386028615L;

	
	public String packetType;
	public byte[] data;
	public Object dataObject;
}
