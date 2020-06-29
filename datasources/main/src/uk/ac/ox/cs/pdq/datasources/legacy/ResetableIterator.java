// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.legacy;

import java.util.Iterator;

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

}
