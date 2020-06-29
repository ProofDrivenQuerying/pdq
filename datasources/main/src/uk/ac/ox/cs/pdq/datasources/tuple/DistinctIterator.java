// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.tuple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;

/**
 * Wrapper for an iterator to ignore duplicate elements.
 * 
 * @author Tim Hobson
 *
 * @param <E> the type of elements returned by this iterator
 */
public class DistinctIterator<E> implements Iterator<E> {

	private Iterator<E> it;
	private List<E> tupleHistory = new ArrayList<E>();
	private E peeked = null;

	public DistinctIterator(Iterator<E> it) {
		Preconditions.checkArgument(it != null);
		this.it = it;
	}

	@Override
	public boolean hasNext() {
		// Peek at the next element in order to determine whether there exists 
		// another distinct element.
		if (this.peeked != null)
			return true;
		if (!this.it.hasNext())
			return false;
		this.peeked = it.next();
		while (tupleHistory.contains(this.peeked)) {
			if (!this.it.hasNext())
				return false;
			this.peeked = it.next();
		}
		return true;
	}

	@Override
	public E next() {

		// If this.peeked is not null, treat that as the next element.
		// Otherwise return the next tuple not found in the history.
		E tuple;
		if (this.peeked == null)
			tuple = it.next();
		else  {
			tuple = this.peeked;
			this.peeked = null;
		} 
		while (tupleHistory.contains(tuple)) {
			if (!it.hasNext())
				throw new NoSuchElementException();
			tuple = it.next();
		}
		// Update the tuple history.
		this.tupleHistory.add(tuple);
		return tuple;
	}
}
