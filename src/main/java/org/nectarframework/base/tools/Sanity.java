package org.nectarframework.base.tools;

import java.util.Collection;

/**
 * This class contains a few sanity check methods to put the blame back where it
 * belongs. 
 * 
 * @author skander
 *
 */
public class Sanity {

	/**
	 * Not Null!
	 * 
	 * 
	 * @param o
	 * @throws NullPointerException if o == null.
	 */
	public static void nn(Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
	}

	
	/**
	 * Not empty
	 * 
	 * @param os
	 * @throws NullPointerException if os == null.
	 * @throws IllegalArgumentException if os.length <= 0
	 */
	public static void ne(Object... os) {
		nn(os);
		if (os.length <= 0) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Not empty
	 * 
	 * @param os
	 * @throws NullPointerException if os == null.
	 * @throws IllegalArgumentException if os.size() <= 0
	 */
	public static void ne(Collection<?> os) {
		nn(os);
		if (os.size() <= 0) {
			throw new IllegalArgumentException();
		}
	}
}
