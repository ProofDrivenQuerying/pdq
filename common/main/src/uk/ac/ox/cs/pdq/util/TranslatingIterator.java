package uk.ac.ox.cs.pdq.util;


/**
 * An iterator consuming another iterator.
 *
 * @author Julien Leblay
 *
 * @param <T>
 */
public interface TranslatingIterator<I, O> {

	/**
	 * @param input I
	 * @return the next output for the given input.
	 */
	O next(I input);

	/**
	 * @param input I
	 * @return true if there is a next output for the given input.
	 */
	boolean hasNext(I input);

	/** @return a copy of the iterator. */
	TranslatingIterator<I, O> deepCopy();
}
