package org.nectarframework.base.service.sql;

public class PreparedParameter {
	private int index;
	private Type type;
	private Object parameter;

	public enum Type {
		Array, AsciiStream, BigDecimal, BinaryStream, Blob, Boolean, Byte, Bytes, CharacterStream, Clob, Date, Double, Float, Int, Long, NCharacterStream, NClob, NString, Null, Ref, SQLXML, Short, String, Time, Timestamp, URL
	};
	
	PreparedParameter(int index, Type type, Object parameter) {
		this.index = index;
		this.type = type;
		this.parameter = parameter;
	}
	
	public int getIndex() {
		return index;
	}
	
	public Type getType() {
		return type;
	}
	
	public Object getParameter() {
		return parameter;
	}
	
}
