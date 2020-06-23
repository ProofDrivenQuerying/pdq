// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dominance;

import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

/**
 * Configuration dominance.
 *
 * @author Efthymia Tsamoura
 * @param <C> the generic type
 */
public interface Dominance {
	
	/**
	 * Checks if is dominated.
	 *
	 * @param source the source
	 * @param target the target
	 * @return true if the source is dominated by the target
	 */
	boolean isDominated(Configuration source, Configuration target);

	/**
	 * Clone.
	 *
	 * @return Dominance
	 */
	Dominance clone();
}
