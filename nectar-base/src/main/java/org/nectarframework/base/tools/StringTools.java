package org.nectarframework.base.tools;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;

import org.nectarframework.base.service.Log;

public abstract class StringTools {
	private static SecureRandom rand = new SecureRandom();

	/**
	 * Get this Nectar instance's charset encoding.
	 * 
	 * @return
	 */
	public static Charset getCharset() {
		return Charset.defaultCharset();
	}

	/**
	 * Decode parameters from a URL, handing the case where a single parameter
	 * name might have been supplied several times, by return lists of values.
	 * In general these lists will contain a single element.
	 * 
	 * @param queryString
	 *            a query string pulled from the URL.
	 * @return a MIME_TYPES of <code>String</code> (parameter name) to
	 *         <code>List&lt;String&gt;</code> (a list of the values supplied).
	 */
	public static HashMap<String, List<String>> decodeParameters(String queryString) {
		HashMap<String, List<String>> parms = new HashMap<String, List<String>>();
		if (queryString != null) {
			StringTokenizer st = new StringTokenizer(queryString, "&");
			while (st.hasMoreTokens()) {
				String e = st.nextToken();
				int sep = e.indexOf('=');
				String propertyName = sep >= 0 ? decodePercent(e.substring(0, sep)).trim() : decodePercent(e).trim();
				if (!parms.containsKey(propertyName)) {
					parms.put(propertyName, new ArrayList<String>());
				}
				String propertyValue = sep >= 0 ? decodePercent(e.substring(sep + 1)) : null;
				if (propertyValue != null) {
					parms.get(propertyName).add(propertyValue);
				}
			}
		}
		return parms;
	}

	/**
	 * Decode percent encoded <code>String</code> values.
	 * 
	 * @param str
	 *            the percent encoded <code>String</code>
	 * @return expanded form of the input, for example "foo%20bar" becomes "foo
	 *         bar"
	 */
	public static String decodePercent(String str) {
		String decoded = null;
		try {
			decoded = URLDecoder.decode(str, "UTF8");
		} catch (UnsupportedEncodingException ignored) {
			Log.warn("Encoding not supported, ignored", ignored);
		}
		return decoded;
	}

	/**
	 * Parse the integer value in a String, make sure it's between the min and
	 * max values. If anything fails, return the def.
	 * 
	 * @param str
	 * @param minimum
	 * @param maximum
	 * @return
	 */
	public static int parseInt(String str, int minimum, int maximum, int def) {
		try {
			int test = Integer.parseInt(str);
			if (test >= minimum && test <= maximum) {
				return test;
			}
		} catch (NumberFormatException e) {
		}

		return def;
	}

	/**
	 * Attempts to give a string representation of the contents of a map for
	 * debug logging purposes.
	 * 
	 * @param map
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String mapToString(Map map) {
		if (map == null)
			return "null";
		String s = "map(";
		boolean trip = false;
		for (Object o : map.keySet()) {
			if (trip)
				s += " ";
			trip = true;
			s += "(" + o.toString() + ", ";
			if (map.get(o) instanceof Map) {
				s += mapToString((Map) map.get(o));
			} else if (map.get(o) instanceof List) {
				for (Object o1 : (List) (map.get(o))) {
					s += " " + o1.toString();
				}
			} else {
				s += map.get(o).toString() + ")";
			}
		}
		s += ")";
		return s;
	}

	public static String mapSSToString(Map<String, String> map) {
		if (map == null)
			return "null";
		Vector<String> vs = new Vector<String>();
		for (Entry<String, String> es : map.entrySet()) {
			vs.add(es.getKey() + "=" + es.getValue());
		}
		return implode(vs, " ");
	}

	private static char[] xmlEntities = { '"', '&', '\'', '<', '>' };
	private static String[] xmlEntitiesEncoded = { "&quot;", "&amp;", "&apos;", "&lt;", "&gt;" };

	/**
	 * Replaces those five characters that XML considers special into their XML
	 * entities.
	 * 
	 * @param s
	 * @return
	 */
	public static String xmlEncode(String s) {
		StringBuffer out = new StringBuffer();

		for (int i = 0; i < s.length(); i++) {
			boolean entity = false;
			char c = s.charAt(i);
			for (int k = 0; k < xmlEntities.length; k++) {
				if (c == xmlEntities[k]) {
					entity = true;
					out.append(xmlEntitiesEncoded[k]);
					break;
				}
			}

			if (!entity) {
				out.append(c);
			}
		}
		return out.toString();
	}

	/**
	 * This is the charset-neutral version. If you have to use this instead of
	 * xmlEncode(String s), you're probably doing something wrong with your
	 * Character sets... In Nectar, everything should be UTF-8.
	 * 
	 * @param byteArr
	 * @return
	 */
	public static String xmlEncode(byte[] byteArr) {
		StringBuffer out = new StringBuffer();

		for (int i = 0; i < byteArr.length; i++) {
			boolean entity = false;
			byte c = byteArr[i];
			for (int k = 0; k < xmlEntities.length; k++) {
				if (c == xmlEntities[k]) {
					entity = true;
					out.append(xmlEntitiesEncoded[k]);
					break;
				}
			}

			if (!entity) {
				out.append(c);
			}
		}
		return out.toString();
	}

	public static String jsonEncode(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				// Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F')
						|| (ch >= '\u2000' && ch <= '\u20FF')) {
					String ss = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < 4 - ss.length(); k++) {
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				} else {
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}

	public static String jsonEncode(byte[] byteArr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteArr.length; i++) {
			byte ch = byteArr[i];
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				// Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F')
						|| (ch >= '\u2000' && ch <= '\u20FF')) {
					String ss = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < 4 - ss.length(); k++) {
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				} else {
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * equivalent to PHP's implode or join functions, returns a String in which
	 * the pieces are seperated by glue.
	 * 
	 * @param pieces
	 * @param glue
	 * @return
	 */
	public static String implode(Collection<String> pieces, String glue) {
		int k = pieces.size();
		if (k == 0) {
			return null;
		}
		StringBuilder out = new StringBuilder();
		Iterator<String> iter = pieces.iterator();
		out.append(iter.next());
		for (int x = 1; x < k; ++x) {
			out.append(glue).append(iter.next());
		}
		return out.toString();
	}

	/**
	 * equivalent to PHP's implode or join functions, returns a String in which
	 * the pieces are seperated by glue.
	 * 
	 * @param pieces
	 * @param glue
	 * @return
	 */
	public static String implode(String[] pieces, String glue) {
		int k = pieces.length;
		if (k == 0) {
			return null;
		}
		StringBuffer out = new StringBuffer();

		out.append(pieces[0]);
		for (int x = 1; x < k; x++) {
			out.append(glue);
			out.append(pieces[x]);
		}
		return out.toString();
	}

	/**
	 * equivalent to PHP's slice or split string functions, returns a Collection
	 * of Strings separated by knife.
	 * 
	 * @param pie
	 * @param knife
	 * @return
	 */
	public static Vector<String> slice(String pie, String knife) {
		Vector<String> v = new Vector<String>();

		int i = 0;
		while (i < pie.length()) {
			int indexOf = pie.indexOf(knife, i);
			if (indexOf < 0) {
				v.add(pie.substring(i, pie.length()));
				return v;
			} else {
				v.add(pie.substring(i, indexOf));
				i = indexOf + 1;
			}
		}

		return v;
	}

	/**
	 * @author maybeWeCouldStealAVan on stackoverflow.com
	 */
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String toHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String msTimeToDatetime(long timestamp) {
		return (new SimpleDateFormat("d MMM yyyy HH:mm")).format(new Date(timestamp));
	}

	public static String throwableStackTracetoString(Throwable t) {
		if (t == null) {
			return null;
		}
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(buff);
		t.printStackTrace(ps);
		ps.flush();
		ps.close();
		return new String(buff.toByteArray());
	}

}
