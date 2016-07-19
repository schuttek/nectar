package org.nectarframework.base.service.xml;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.xerces.impl.dv.util.Base64;
import org.nectarframework.base.tools.StringTools;

/**
 * Structured data. docs/Data Transport.txt
 * 
 * @author skander
 *
 */

public class Element {
	private final String name;
	private Map<String, String> attributes = new HashMap<String, String>();
	private LinkedList<Element> children = new LinkedList<Element>();

	/**
	 * 
	 * @param name
	 *            name of this element.
	 */
	public Element(String name) {
		this.name = name;
	}

	/**
	 * Construct an element with the given name and attributes
	 * 
	 * @param name
	 * @param attributes
	 */
	public Element(String name, Map<String, String> attributes) {
		this.name = name;
		this.attributes = attributes;
	}

	/**
	 * Construct an element with the given name, attributes and children.
	 * 
	 * @param name
	 * @param attributes
	 * @param elms
	 */
	public Element(String name, Map<String, String> attributes, Collection<Element> elms) {
		this.name = name;
		this.attributes = attributes;
		this.children.addAll(elms);
	}

	/**
	 * Construct an Element with the given name and children.
	 * 
	 * @param name
	 * @param elms
	 */
	public Element(String name, Collection<Element> elms) {
		this.name = name;
		this.children.addAll(elms);
	}

	public Element(String name, Element child) {
		this.name = name;
		this.children.add(child);
	}

	/**
	 * Add an attribute. if value is null, remove the attribute.
	 * 
	 * @param key
	 * @param value
	 * @return this element
	 */

	public Element add(String key, String value) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (value == null) {
			attributes.remove(key);
		} else {
			attributes.put(key, value);
		}
		return this;
	}

	/**
	 * Add an attribute. if value is null, remove the attribute. This is a
	 * shorthand for adding numbers.
	 * 
	 * @param key
	 * @param value
	 * @return this element
	 */
	public Element add(String key, Number value) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (value == null) {
			attributes.remove(key);
		} else {
			attributes.put(key, value.toString());
		}
		return this;
	}

	/**
	 * Add a binary attribute, which is stored internally as Base64 encoded
	 * String... so using this for huge byteArrays might not be a great idea. if
	 * byteArray == null, remove the attribute.
	 * 
	 * @param key
	 * @param byteArray
	 * @return this element
	 */
	public Element addBinary(String key, byte[] byteArray) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (byteArray == null) {
			attributes.remove(key);
		} else {
			attributes.put(key, Base64.encode(byteArray));
		}

		return this;
	}

	/**
	 * Retrieves the value of the attribute key as binary data (Base64 decoded).
	 * 
	 * @param key
	 * @return
	 */
	public byte[] getBinary(String key) {
		String s = attributes.get(key);
		if (s != null) {
			return Base64.decode(s);
		}
		return null;
	}

	/**
	 * Adds the given Element as a child.
	 * 
	 * @param e
	 * @return this Element
	 */
	public Element add(Element e) {
		if (e != null)
			children.add(e);
		return this;
	}

	public Element add(Collection<Element> children) {
		this.children.addAll(children);
		return this;
	}

	/**
	 * The name of this Element
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * The attributes as a key/value map. This is a live map. Changes you make
	 * to the returned value affect this Element.
	 * 
	 * @return
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * The children Elements of this Element. This is a live LinkedList. Changes
	 * you make to the returned value affect this Element.
	 * 
	 * @return
	 */
	public LinkedList<Element> getChildren() {
		return children;
	}

	/**
	 * The children Elements of this Element that match the given name.
	 * 
	 * @param name
	 * @return
	 */

	public LinkedList<Element> getChildren(String name) {
		LinkedList<Element> list = new LinkedList<Element>();
		for (Element e : children) {
			if (e.isName(name)) {
				list.add(e);
			}
		}
		return list;
	}

	/**
	 * Gets the value of the attribute by key. returns null if key is not
	 * defined.
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return attributes.get(key);
	}

	public Integer getAsInt(String key) {
		String s = get(key);
		if (s == null)
			return null;
		return Integer.parseInt(s);
	}

	public Long getAsLong(String key) {
		String s = get(key);
		if (s == null)
			return null;
		return Long.parseLong(s);
	}

	public Float getAsFloat(String key) {
		String s = get(key);
		if (s == null)
			return null;
		return Float.parseFloat(s);
	}

	public Double getAsDouble(String key) {
		String s = get(key);
		if (s == null)
			return null;
		return Double.parseDouble(s);
	}

	/**
	 * removes the attribute by key.
	 * 
	 * @param key
	 * @return this Element
	 */
	public Element removeAttribute(String key) {
		attributes.remove(key);
		return this;
	}

	public boolean equals(Element e) {
		if (name.compareTo(e.name) != 0)
			return false;
		if (attributes.size() != attributes.size())
			return false;
		if (children.size() != children.size())
			return false;
		for (String key : attributes.keySet()) {
			if (attributes.get(key).compareTo(e.attributes.get(key)) != 0)
				return false;
		}
		for (int t = 0; t < children.size(); t++) {
			if (!children.get(t).equals(e.children.get(t)))
				return false;
		}
		return true;
	}

	public String toString() {
		String s = name + " " + StringTools.mapToString(attributes);
		for (Element c : children) {
			s += " {" + c.toString() + "}";
		}
		return s;
	}

	public boolean isName(String name) {
		return this.name.compareTo(name) == 0;
	}

	public boolean isAttribute(String key, String value) {
		String myValue = attributes.get(key);

		if (myValue == null) {
			if (value == null) {
				return true;
			} else {
				return false;
			}
		}
		if (myValue.compareTo(value) == 0) {
			return true;
		}
		return false;
	}

	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

}
