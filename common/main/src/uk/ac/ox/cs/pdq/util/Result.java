package uk.ac.ox.cs.pdq.util;


/**
 * Common interface to query/plan results.
 *
 * @author Julien Leblay
 */
public interface Result extends Cloneable, Differentiable<Result> {

	/** @return true, if this result is empty. */
	boolean isEmpty();

	/** @return the size of the result. */
	int size();

	/**
	 * @param o Result
	 * @return a String representation of the difference between this object and
	 *         o.
	 */
	@Override
	String diff(Result o);

	/**
	 * @param o Result
	 * @return true, if this object is a equivalent to o.
	 */
	@Override
	Levels howDifferent(Result o);
}
