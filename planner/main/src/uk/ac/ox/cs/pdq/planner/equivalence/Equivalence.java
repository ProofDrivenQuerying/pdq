// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.equivalence;

import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;


/**
 * Configuration equivalence.
 *
 * @author Efthymia Tsamoura
 * @param <C> the generic type
 */
public interface Equivalence<C extends Configuration> {

	/**
	 *
	 * @param source C
	 * @param target C
	 * @return true if source and target configurations are equivalent.
	 */
	boolean isEquivalent(C source, C target);

	/**
	 *
	 * @return Equivalence
	 */
	Equivalence<C> clone();
}
