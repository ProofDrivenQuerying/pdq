package uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

/**
 * Performs equivalence detection.
 * Two configurations c and c' are equivalent if they have the same input constants and 
 * there exists an homomorphism from the facts of c to the facts of c' and vice-versa.
 *
 * @author Efthymia Tsamoura
 */
public interface FactEquivalence extends Equivalence<ChaseConfiguration>  {

	/**
	 * @return StructuralEquivalence
	 */
	FactEquivalence clone();
}
