// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.equivalence;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.ChaseConfiguration;

/**
 * Structural equivalence.
 * Two configurations c and c' are structurally equivalent if there exists an homomorphism from the facts of c to the facts of c' and vice-versa.
 *
 * @author Efthymia Tsamoura
 */
public interface StructuralEquivalence extends Equivalence<ChaseConfiguration>  {

	/**
	 * Clone.
	 *
	 * @return StructuralEquivalence
	 */
	StructuralEquivalence clone();
}
