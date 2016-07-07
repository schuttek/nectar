package org.nectarframework.base.tools;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

public abstract class StringTools {
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

	@SuppressWarnings("rawtypes")
	public static String mapToString(Map map) {
		if (map == null) return "null";
		String s = "map(";
		boolean trip = false;
		for (Object o : map.keySet()) {
			if (trip)
				s += " ";
			trip = true;
			s += "(" + o.toString() + ", ";
			if (map.get(o) instanceof Map) {
				s += mapToString((Map) map.get(o));
			} else {
				s += map.get(o).toString() + ")";
			}
		}
		s += ")";
		return s;
	}

	private static char[] xmlEntities = { '"', '&', '\'', '<', '>' };
	private static String[] xmlEntitiesEncoded = { "&quot;", "&amp;", "&apos;", "&lt;", "&gt;" };

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
				if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
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
				if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
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

	public static String filterClassName(String s) {
		// TODO: implement me (check the argument against a regex that would be
		// considered a class name.
		return s;
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
			int io = pie.indexOf(knife, i);
			if (io < 0) {
				v.add(pie.substring(i, pie.length()));
			} else {
				v.add(pie.substring(i, io));
			}
			i = io + 1;
		}

		return v;
	}

	/**
	 * Create a random String visible, typeable characters in ASCII byte for, of
	 * given length
	 */
	public static String randomPassword(int length) {
		return randomString(length, length);
	}

	/**
	 * Create a String of between i and j in length, of random characters A
	 * through Z.
	 * 
	 * @param i
	 * @param j
	 * @return
	 */

	public static String randomString(int i, int j) {
		Random rand = new Random();
		int len = rand.nextInt(Math.abs((j + 1) - i)) + i;
		byte[] sa = new byte[len];
		for (int t = 0; t < len; t++) {
			sa[t] = (byte) (65 + rand.nextInt(26));
		}
		return new String(sa);
	}

	/**
	 * Create a String of between i and j in length, of random characters a
	 * through z.
	 * 
	 * @param i
	 * @param j
	 * @return
	 */

	public static String randomStringLowerCase(int i, int j) {
		Random rand = new Random();
		int len = rand.nextInt(Math.abs((j + 1) - i)) + i;
		byte[] sa = new byte[len];
		for (int t = 0; t < len; t++) {
			sa[t] = (byte) (97 + rand.nextInt(26));
		}
		return new String(sa);
	}

	public static String[] appendTo(String[] dataColums, String string) {
		String[] ret = new String[dataColums.length];
		for (int i = 0; i < dataColums.length; i++) {
			ret[i] = dataColums[i] + string;
		}
		return ret;
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
}
