// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.equivalence;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.ChaseConfiguration;

/**
 * Fact equivalence.
 * Two configurations c and c' are equivalent if they have the same input constants and 
 * there exists an homomorphism from the facts of c to the facts of c' and vice-versa.
 *
 * @author Efthymia Tsamoura
 */
public interface FactEquivalence extends Equivalence<ChaseConfiguration>  {

	/**
	 * Clone.
	 *
	 * @return StructuralEquivalence
	 */
	FactEquivalence clone();
}
