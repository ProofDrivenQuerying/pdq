// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.equivalence;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.ChaseConfiguration;


/**
 * Fast equivalence.
 * According to this implementation two configurations c and c' are equivalent if the have the same inferred accessible facts.
 * In order to perform this kind of check Skolem constants must be assigned to formula variables during chasing.
 *
 * @author Efthymia Tsamoura
 */
public class FastStructuralEquivalence implements StructuralEquivalence{

	/**
	 * Checks if is equivalent.
	 *
	 * @param source ChaseConfiguration
	 * @param target ChaseConfiguration
	 * @return true if source and target configurations are structurally equivalent
	 */
	@Override
	public boolean isEquivalent(ChaseConfiguration source, ChaseConfiguration target) {
		if (source.getState().getInferredAccessibleFacts().equals(target.getState().getInferredAccessibleFacts())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Clone.
	 *
	 * @return FastFactDominance
	 */
	@Override
	public FastStructuralEquivalence clone() {
		return new FastStructuralEquivalence();
	}
}