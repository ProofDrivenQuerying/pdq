package uk.ac.ox.cs.pdq.planner.dominance;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

/**
 * Fact dominance.
 * A configuration c and c' is fact dominated by another configuration c' if there exists an homomorphism from the facts of c to the facts of c' and
 * the input constants are preserved.
 *
 * @author Efthymia Tsamoura
 */
public interface FactDominance extends Dominance<ChaseConfiguration> {

	/**
	 * @return FactDominance<C>
	 */
	@Override
	FactDominance clone();
}
