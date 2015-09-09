package uk.ac.ox.cs.pdq.util;

/**
 * Common interface to thing that can be differentiated, i.e. tell whether two
 * object are the same, equivalent or different, and provide some human-readable
 * representation of their difference if any.
 *
 * Note, Differentiable has nothing to do with the Comparable interface.
 *
 * @author Julien Leblay
 *
 * @param <T>
 */
public interface Differentiable<T> {

	/**
	 */
	static enum Levels {IDENTICAL, EQUIVALENT, DIFFERENT}

	/**
	 * @param o
	 * @return Levels
	 */
	Levels howDifferent(T o);

	/**
	 * @param o T
	 * @return a String representation of the difference between this object
	 * and o
	 */
	String diff(T o);
}
