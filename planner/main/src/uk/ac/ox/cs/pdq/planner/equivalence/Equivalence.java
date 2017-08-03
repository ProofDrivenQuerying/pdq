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
