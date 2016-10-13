package org.nectarframework.base.tools;


/**
 * a very simple combination of 2 variables.
 * 
 * @author skander
 *
 * @param <L>
 * @param <R>
 */
public class Tuple<L, R> {
	private final L left;
	private final R right;

	public Tuple(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}
}
