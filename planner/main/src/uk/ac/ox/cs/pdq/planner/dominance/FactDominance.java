// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dominance;

/**
 * Fact dominance. A configuration c and c' is fact dominated by another configuration c' if there exists an homomorphism from the facts of c to the facts of c' and
 * the input constants are preserved.
 *
 * @author Efthymia Tsamoura
 */
public interface FactDominance extends Dominance {

	/**
	 * Clone.
	 *
	 * @return FactDominance<C>
	 */
	@Override
	FactDominance clone();
}
