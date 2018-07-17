package uk.ac.ox.cs.pdq.datasources.resultstable;

/**
 * Common interface to things that can be differentiated, i.e. tell whether two
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
	 */
	static enum Levels {
		IDENTICAL, 
		EQUIVALENT, 
		DIFFERENT}

	/**
	 * Returns the "level" of difference.
	 *
	 * @param o the o
	 * @return Levels
	 */
	Levels howDifferent(T o);

	/**
	 * The difference between this object and the input.
	 *
	 * @param o T
	 * @return a String representation of the difference between this object and the input
	 * and o
	 */
	String diff(T o);
}
