package org.nectarframework.base.service;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ServiceParameters {
	private HashMap<String, String> params;

	public ServiceParameters(HashMap<String, String> params) {
		this.params = params;
	}

	public String getValue(String key) {
		return this.params.get(key);
	}

	public String toString() {
		String str = "(";
		for (String k : params.keySet()) {
			str += k + "=" + params.get(k) + ",";
		}
		return str;
	}

	public static ServiceParameters parseServiceParameters(Element serviceElement) {
		HashMap<String, String> params = new HashMap<String, String>();

		NodeList list = serviceElement.getElementsByTagName("param");
		for (int i = 0; i < list.getLength(); i++) {
			Element paramElm = (Element) list.item(i);
			String key = paramElm.getAttribute("name");
			String value = paramElm.getAttribute("value");
			if (key != null && key != "" && value != null && value != "") {
				params.put(key, value);
			}
		}
		return new ServiceParameters(params);
	}
	
	public String getString(String key, String def) {
		String str = this.params.get(key);
		if (str == null) {
			return def;
		}
		return str;
	}

	public int getInt(String key, int minimum, int maximum, int def) {
		String str = this.params.get(key);
		if (str == null) {
			return def;
		}
		try {
			int test = Integer.parseInt(str);
			if (test >= minimum && test <= maximum) {
				return test;
			}
		} catch (NumberFormatException e) {
		}
		return def;
	}

	public long getLong(String key, long minimum, long maximum, long def) {
		String str = this.params.get(key);
		if (str == null) {
			return def;
		}
		try {
			long test = Long.parseLong(str);
			if (test >= minimum && test <= maximum) {
				return test;
			}
		} catch (NumberFormatException e) {
		}
		return def;
	}
	
	public boolean getBoolean(String key, boolean def) {
		String str = this.params.get(key);
		if (str == null) {
			return def;
		} else if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("1")) {
			return true;
		} else if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no") || str.equalsIgnoreCase("0")) {
			return false;
		}
		return def;
	}

	public float getFloat(String key, float minimum, float maximum, float def) {
		String str = this.params.get(key);
		if (str == null) {
			return def;
		}
		try {
			float test = Float.parseFloat(str);
			if (test >= minimum && test <= maximum) {
				return test;
			}
		} catch (NumberFormatException e) {
		}
		return def;
	}

}
