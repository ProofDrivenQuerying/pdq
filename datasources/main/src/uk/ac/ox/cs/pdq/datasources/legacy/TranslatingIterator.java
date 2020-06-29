// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.legacy;


/**
 * An iterator consuming another iterator.
 *
 * @author Julien Leblay
 * @param <I> the generic type
 * @param <O> the generic type
 */
public interface TranslatingIterator<I, O> {

	/**
	 * Next.
	 *
	 * @param input I
	 * @return the next output for the given input.
	 */
	O next(I input);

	/**
	 *
	 * @param input I
	 * @return true if there is a next output for the given input.
	 */
	boolean hasNext(I input);

	/**
	 * Deep copy.
	 *
	 * @return a copy of the iterator.
	 */
	TranslatingIterator<I, O> deepCopy();
}
