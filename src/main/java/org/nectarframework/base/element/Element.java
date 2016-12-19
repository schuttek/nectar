package org.nectarframework.base.element;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.xerces.impl.dv.util.Base64;
import org.nectarframework.base.tools.StringTools;

/**
 * The Element is basically a <String name, HashMap<String attribute, String value>, List<Element> children>
 * 
 * It mimics the least common ground between data structures that can be written in xml, json, yaml and others.
 * 
 * The Element allows you to pass structured data throughout Nectar and between various markup languages. 
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
		add(attributes);
	}

	/**
	 * Construct an element with the given name, attributes and children.
	 * 
	 * @param name
	 * @param attributes
	 * @param elms
	 */
	public Element(String name, Map<String, String> attributes, Collection<Element> children) {
		this.name = name;
		this.attributes = attributes;
		addAll(children);
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
		infiniteLoopTest(e.getChildren());
		if (e != null)
			children.add(e);
		return this;
	}

	private void infiniteLoopTest(Collection<Element> children) {
		for (Element e : children) {
			if (e == this) {
				throw new IllegalArgumentException("An element cannot be added to itself as a child.");
			}
			infiniteLoopTest(e.getChildren());
		}
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


	public String toString() {
		StringBuffer s = new StringBuffer("\nroot=" + name + " " + StringTools.mapSSToString(attributes));
		for (Element c : children) {
			s.append("\n");
			c.toStringBuffer(s, 1);
		}
		return s.toString();
	}

	private String toStringAddTabs(int tabc) {
		String s = "";
		for (int t = 0; t < tabc; t++) {
			s += "\t";
		}
		return s;
	}

	public StringBuffer toStringBuffer(StringBuffer sb, int tabInc) {
		sb.append(toStringAddTabs(tabInc)+name+ " "+StringTools.mapSSToString(attributes));
		for (Element c : children) {
			sb.append("\n");
			c.toStringBuffer(sb, tabInc+1);
		}
		return sb;
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

	/**
	 * returns a deep copy of this Element
	 * 
	 * @return
	 */
	public Element copy() {
		Element e = new Element(this.name);
		for (String k : this.attributes.keySet()) {
			e.add(k, attributes.get(k));
		}
		for (Element c : this.children) {
			e.add(c.copy());
		}
		return e;
	}

	public void removeChild(Element elm) {
		this.children.removeFirstOccurrence(elm);
	}

	public void add(Map<String, String> attributes) {
		attributes.putAll(attributes);
	}

	public void addAll(Collection<Element> children) {
		children.forEach(c -> add(c));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Element other = (Element) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
