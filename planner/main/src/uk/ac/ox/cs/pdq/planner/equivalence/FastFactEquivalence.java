// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.equivalence;

import java.util.Set;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.ChaseConfiguration;


/**
 * Fast fact equivalence.
 * According to this implementation two configurations c and c' are equivalent if the have the same inferred accessible facts. 
 * In order to perform this kind of equivalence check Skolem constants must be assigned to formula variables during chasing.
 *
 * @author Efthymia Tsamoura
 */
public class FastFactEquivalence implements FactEquivalence{

	/**
	 * Checks if is equivalent.
	 *
	 * @param source ChaseConfiguration
	 * @param target ChaseConfiguration
	 * @return true if source and target configurations are equivalent
	 */
	@Override
	public boolean isEquivalent(ChaseConfiguration source, ChaseConfiguration target) {
		Set<Constant> inputs1 = Sets.newLinkedHashSet(source.getInput());
		Set<Constant> inputs2 = Sets.newLinkedHashSet(target.getInput());
		if(inputs1.equals(inputs2)) {
			if (source.getState().getInferredAccessibleFacts().equals(target.getState().getInferredAccessibleFacts())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Clone.
	 *
	 * @return FastFactDominance
	 */
	@Override
	public FastFactEquivalence clone() {
		return new FastFactEquivalence();
	}
}