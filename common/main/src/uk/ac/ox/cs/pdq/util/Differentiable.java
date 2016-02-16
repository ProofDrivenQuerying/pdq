package uk.ac.ox.cs.pdq.util;

// TODO: Auto-generated Javadoc
/**
 * Common interface to thing that can be differentiated, i.e. tell whether two
 * object are the same, equivalent or different, and provide some human-readable
 * representation of their difference if any.
 * 
 * Note, Differentiable has nothing to do with the Comparable interface.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public interface Differentiable<T> {

	/**
	 * The Enum Levels.
	 */
	static enum Levels {
/** The identical. */
IDENTICAL, 
 /** The equivalent. */
 EQUIVALENT, 
 /** The different. */
 DIFFERENT}

	/**
	 * How different.
	 *
	 * @param o the o
	 * @return Levels
	 */
	Levels howDifferent(T o);

	/**
	 * Diff.
	 *
	 * @param o T
	 * @return a String representation of the difference between this object
	 * and o
	 */
	String diff(T o);
}
