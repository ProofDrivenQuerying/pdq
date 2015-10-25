package uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.AnnotatedPlan;


/**
 * Performs structural equivalence detection.
 * Two configurations c and c' are structurally equivalent if there exists an homomorphism from the facts of c to the facts of c' and vice-versa.
 *
 * @author Efthymia Tsamoura
 */
public interface StructuralEquivalence extends Equivalence<AnnotatedPlan>  {

	/**
	 * @return StructuralEquivalence
	 */
	StructuralEquivalence clone();
}
