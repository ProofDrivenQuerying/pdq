package uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance;

import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;



/**
 * Configuration dominance.
 *
 * @author Efthymia Tsamoura
 */
public interface Dominance<C extends Configuration> {
	/**
	 * @param source
	 * @param target
	 * @return true if the source is dominated by the target
	 */
	boolean isDominated(C source, C target);

	/**
	 * @return Dominance
	 */
	Dominance<C> clone();
}
