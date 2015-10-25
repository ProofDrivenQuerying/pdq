package uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.AnnotatedPlan;



/**
 * Performs fast fact dominance checks.
 * A source configuration is fact dominated by a target configuration if any
 * inferred accessible fact plus in the source configuration also appears
 * in the target configuration.
 * In order to perform this kind of check Skolem constants must be assigned to 
 * formula variables during chasing.
 *
 * @author Efthymia Tsamoura
 */
public class FastFactDominance implements FactDominance{

	private final boolean isStrict;

	/**
	 * Constructor for FastFactDominance.
	 * @param isStrict boolean
	 */
	public FastFactDominance(boolean isStrict) {
		this.isStrict = isStrict;
	}

	/**
	 * @param source C
	 * @param target C
	 * @return true if the source configuration is dominated by target configuration
	 */
	@Override
	public boolean isDominated(AnnotatedPlan source, AnnotatedPlan target) {
		if (source.equals(target)) {
			return false;
		}
		if ( target.getState().getFacts().containsAll(source.getState().getFacts())) {
			if (!this.isStrict ||  ( this.isStrict && source.getOutputFacts().size() < target.getOutputFacts().size())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return FastFactDominance<C>
	 */
	@Override
	public FastFactDominance clone() {
		return new FastFactDominance(this.isStrict);
	}
}
