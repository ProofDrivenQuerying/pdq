package uk.ac.ox.cs.pdq.planner.equivalence;

import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;


// TODO: Auto-generated Javadoc
/**
 * Configuration equivalence.
 *
 * @author Efthymia Tsamoura
 * @param <C> the generic type
 */
public interface Equivalence<C extends Configuration> {

	/**
	 * Checks if is equivalent.
	 *
	 * @param source C
	 * @param target C
	 * @return true if source and target configurations are equivalent.
	 */
	boolean isEquivalent(C source, C target);

	/**
	 * Clone.
	 *
	 * @return Equivalence
	 */
	Equivalence<C> clone();
}
