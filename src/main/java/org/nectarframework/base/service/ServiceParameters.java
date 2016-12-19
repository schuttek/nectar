package org.nectarframework.base.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

import org.nectarframework.base.element.Element;

public class ServiceParameters {
	public static final int PORT_MAX = 65535;

	private HashMap<String, String> params;
	private HashMap<String, LinkedList<String>> paramSets;

	public ServiceParameters(HashMap<String, String> params, HashMap<String, LinkedList<String>> paramSets) {
		this.params = params;
		this.paramSets = paramSets; 
	}

	public Optional<String> getValue(String key) {
		return Optional.ofNullable(this.params.get(key));
	}

	public String toString() {
		String str = "(";
		for (String k : params.keySet()) {
			str += k + "=" + params.get(k) + ",";
		}
		return str;
	}

	public static ServiceParameters parsesp(Element serviceElement) {
		HashMap<String, String> params = new HashMap<String, String>();
		HashMap<String, LinkedList<String>> paramSets = new HashMap<String, LinkedList<String>>();

		for (Element paramElm : serviceElement.getChildren("param")) {
			String key = paramElm.get("name");
			String value = paramElm.get("value");
			if (key != null && key != "" && value != null && value != "") {
				params.put(key, value);
			}
		}
		for (Element paramSet : serviceElement.getChildren("paramSet")) {
			String key = paramSet.get("name");
			if (key != null && key != "") {
				LinkedList<String> valueList = new LinkedList<String>();
				for (Element paramElm : paramSet.getChildren("param")) {
					String value = paramElm.get("value");
					if (value != null && value != "") {
						valueList.add(value);
					}
				}
				paramSets.put(key, valueList);
			}
		}
		
		return new ServiceParameters(params, paramSets);
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

	public LinkedList<String> getSet(String key) {
		return paramSets.get(key);
	}

}
