package uk.ac.ox.cs.pdq.datasources;

import java.util.Iterator;

// TODO: Auto-generated Javadoc
/**
 * An iterator that can be reset, i.e. the cursor can be placed back to the
 * beginning of the underlying Iterable at any time.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public interface ResetableIterator<T> extends Iterator<T> {

	/** Opens the iterator to its initial position. */
	void open();

	/** Resets the iterator to its initial position. */
	void reset();

//	/**
//	 * Deep copy.
//	 *
//	 * @return a copy of the iterator.
//	 */
//	ResetableIterator<T> deepCopy();

}
