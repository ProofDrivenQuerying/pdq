package uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;


/**
 * Performs fast equivalence checks.
 * According to this implementation two configurations c and c' are equivalent if the have the same inferred accessible facts.
 * In order to perform this kind of check Skolem constants must be assigned to formula variables during chasing.
 *
 * @author Efthymia Tsamoura
 */
public class FastStructuralEquivalence implements StructuralEquivalence{

	/**
	 * @param source ChaseConfiguration
	 * @param target ChaseConfiguration
	 * @return true if source and target configurations are structurally equivalent
	 */
	@Override
	public boolean isEquivalent(ChaseConfiguration source, ChaseConfiguration target) {
		if (source.getInferred().equals(target.getInferred())) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return FastFactDominance
	 */
	@Override
	public FastStructuralEquivalence clone() {
		return new FastStructuralEquivalence();
	}
}