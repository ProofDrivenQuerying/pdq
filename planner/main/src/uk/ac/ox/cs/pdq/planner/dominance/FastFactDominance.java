// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dominance;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

/**
 * Performs fast fact dominance checks. A source configuration is fact dominated
 * by a target configuration if any inferred accessible fact plus in the source
 * configuration also appears in the target configuration. In order to perform
 * this kind of check Skolem constants must be assigned to formula variables
 * during chasing.
 *
 * @author Efthymia Tsamoura
 */
public class FastFactDominance implements FactDominance {

	/** The is strict. */
	private final boolean hasStrictlyFewerFactsCheck;

	/**
	 * Constructor for FastFactDominance.
	 * 
	 * @param hasStrictlyFewerFactsCheck
	 *            boolean
	 */
	public FastFactDominance(boolean hasStrictlyFewerFactsCheck) {
		this.hasStrictlyFewerFactsCheck = hasStrictlyFewerFactsCheck;
	}

	/**
	 * Checks if is dominated.
	 *
	 * @param source
	 *            C
	 * @param target
	 *            C
	 * @return true if the source configuration is dominated by target configuration
	 */
	@Override
	public boolean isDominated(Configuration source, Configuration target) {
		if (!(source instanceof DAGChaseConfiguration)) {
			return false;
		}
		if (!(target instanceof DAGChaseConfiguration)) {
			return false;
		}
		Collection<Constant> srcInput = source.getInput();
		Collection<Constant> srcTarget = target.getInput();
		Collection<Atom> dTargetStateInfAccFacts = ((DAGChaseConfiguration) target).getState().getInferredAccessibleFacts();
		Collection<Atom> dSourceStateInfAccFacts = ((DAGChaseConfiguration) source).getState().getInferredAccessibleFacts();
		if (srcInput.size() < srcTarget.size()) {
			return false;
		}
		if (!checkSrcToTargetInputContainment(srcInput,srcTarget)) {
			return false;
		}
		if (dTargetStateInfAccFacts.size() < dSourceStateInfAccFacts.size()) {
			return false;
		}
		if (!checkSrcToTargetInfAccFactsContainment(dTargetStateInfAccFacts,dSourceStateInfAccFacts)) { 
			return false;
		}
		if (!this.hasStrictlyFewerFactsCheck || dSourceStateInfAccFacts.size() < dTargetStateInfAccFacts.size())
			return true;
		return false;
	}

	private boolean checkSrcToTargetInputContainment(Collection<Constant> srcInput, Collection<Constant> srcTarget) {
		return srcInput.containsAll(srcTarget);
	}
	private boolean checkSrcToTargetInfAccFactsContainment(Collection<Atom> dSourceStateInfAccFacts, Collection<Atom> dTargetStateInfAccFacts) {
//		return dTargetStateInfAccFacts.containsAll(dSourceStateInfAccFacts);
		return dSourceStateInfAccFacts.containsAll(dTargetStateInfAccFacts);
	}

	@Override
	public FastFactDominance clone() {
		return new FastFactDominance(this.hasStrictlyFewerFactsCheck);
	}
}
