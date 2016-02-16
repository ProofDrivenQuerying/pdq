package uk.ac.ox.cs.pdq.util;


// TODO: Auto-generated Javadoc
/**
 * Common interface to query/plan results.
 *
 * @author Julien Leblay
 */
public interface Result extends Cloneable, Differentiable<Result> {

	/**
	 * Checks if is empty.
	 *
	 * @return true, if this result is empty.
	 */
	boolean isEmpty();

	/**
	 * Size.
	 *
	 * @return the size of the result.
	 */
	int size();

	/**
	 * Diff.
	 *
	 * @param o Result
	 * @return a String representation of the difference between this object and
	 *         o.
	 */
	@Override
	String diff(Result o);

	/**
	 * How different.
	 *
	 * @param o Result
	 * @return true, if this object is a equivalent to o.
	 */
	@Override
	Levels howDifferent(Result o);
}
