package org.nectarframework.base.form;

import java.util.List;

public class Formvar {

	private String name = null;
	private Object value = null;
	private List<Long> intList = null;
	private List<String> strList = null;
	private Form.VarType type = null;
	private boolean nullAllowed = false;


	public Formvar(String name, Form.VarType type, boolean nullAllowed) {
		this.name = name;
		this.type = type;
		this.nullAllowed = nullAllowed;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public String getString() {
		return getString(0);
	}

	public int length() {
		switch (type) {
		case StringArray:
			return this.strList.size();
		case IntegerArray:
			return this.intList.size();
		case String:
		case Long:
		default:
			return 1;
		}
	}

	public String getString(int index) {
		switch (type) {
		case StringArray:
			if (this.strList == null) {
				return null;
			}
			return this.strList.get(index);
		case IntegerArray:
			if (this.intList == null) {
				return null;
			}
			Integer i = this.intList.get(index).intValue();
			return i.toString();
		case String:
			if (index > 0) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (this.value == null) {
				return null;
			}
			return (String) this.value;
		case Long:
			if (index > 0) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (this.value == null) {
				return null;
			}
			return ((Integer) this.value).toString();
		case Double:
			if (index > 0) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (this.value == null) {
				return null;
			}
			return ((Double) this.value).toString();
		default: // can't happen...
			return null;
		}
	}

	public Integer getInt() {
		return getInt(0);
	}

	public Integer getInt(int index) {
		switch (type) {
		case StringArray:
			if (this.strList == null) {
				return null;
			}
			String str = this.strList.get(index);
			if (str == null) {
				return null;
			}
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				return new Integer(0);
			}
		case IntegerArray:
			if (this.intList == null) {
				return null;
			}
			return this.intList.get(index).intValue();
		case String:
			if (index > 0) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (this.value == null) {
				return null;
			}
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				return new Integer(0);
			}
		case Long:
			if (index > 0) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (this.value == null) {
				return null;
			}
			return ((Long)this.value).intValue();
		case Double:
			if (index > 0) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (this.value == null) {
				return null;
			}
			return ((Double)this.value).intValue();
		default: // can't happen...
			return null;
		}
	}
	
	public Long getLong() {
		return getLong(0);
	}

	public Long getLong(int index) {
		switch (type) {
		case StringArray:
			if (this.strList == null) {
				return null;
			}
			String str = this.strList.get(index);
			if (str == null) {
				return null;
			}
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
				return new Long(0);
			}
		case IntegerArray:
			if (this.intList == null) {
				return null;
			}
			return this.intList.get(index);
		case String:
			if (index > 0) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (this.value == null) {
				return null;
			}
			try {
				return Long.parseLong((String) value);
			} catch (NumberFormatException e) {
				return new Long(0);
			}
		case Long:
			if (index > 0) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (this.value == null) {
				return null;
			}
			return (Long) this.value;
		case Double:
			if (index > 0) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (this.value == null) {
				return null;
			}
			return ((Double)this.value).longValue();
		default: // can't happen...
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public boolean isNullAllowed() {
		return nullAllowed;
	}
}
