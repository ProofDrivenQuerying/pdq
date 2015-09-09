package uk.ac.ox.cs.pdq.util;

import java.util.Iterator;

/**
 * An iterator that can be reset, i.e. the cursor can be placed back to the
 * beginning of the underlying Iterable at any time.
 *
 * @author Julien Leblay
 *
 * @param <T>
 */
public interface ResetableIterator<T> extends Iterator<T> {

	/** Opens the iterator to its initial position. */
	void open();

	/** Resets the iterator to its initial position. */
	void reset();

	/** @return a copy of the iterator. */
	ResetableIterator<T> deepCopy();

}
