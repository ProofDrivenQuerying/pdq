/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.reasoning.chase.dominance;

import uk.ac.ox.cs.pdq.cardinality.reasoning.Configuration;



// TODO: Auto-generated Javadoc
/**
 * Configuration dominance.
 *
 * @author Efthymia Tsamoura
 * @param <C> the generic type
 */
public interface Dominance<C extends Configuration> {
	
	/**
	 * Checks if is dominated.
	 *
	 * @param source the source
	 * @param target the target
	 * @return true if the source is dominated by the target
	 */
	boolean isDominated(C source, C target);

	/**
	 * Clone.
	 *
	 * @return Dominance
	 */
	Dominance<C> clone();
}
