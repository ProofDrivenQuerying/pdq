package uk.ac.ox.cs.pdq.planner.dominance;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;


// TODO: Auto-generated Javadoc
/**
 * Performs fast fact dominance checks.
 * A source configuration is fact dominated by a target configuration if any
 * inferred accessible fact plus in the source configuration also appears
 * in the target configuration. In order to perform this kind of check Skolem constants must be assigned to formula variables during chasing.
 *
 * @author Efthymia Tsamoura
 */
public class FastFactDominance implements FactDominance{

	/** The is strict. */
	private final boolean hasStrictlyFewerFactsCheck;

	/**
	 * Constructor for FastFactDominance.
	 * @param hasStrictlyFewerFactsCheck boolean
	 */
	public FastFactDominance(boolean hasStrictlyFewerFactsCheck) {
		this.hasStrictlyFewerFactsCheck = hasStrictlyFewerFactsCheck;
	}

	/**
	 * Checks if is dominated.
	 *
	 * @param source C
	 * @param target C
	 * @return true if the source configuration is dominated by target configuration
	 */
	@Override
	public boolean isDominated(Configuration source, Configuration target) {
		if (source.equals(target)) 
			return false;
		if (source instanceof DAGChaseConfiguration && 
				target instanceof DAGChaseConfiguration && 
				source.getInput().containsAll(target.getInput()) && 
				((DAGChaseConfiguration)target).getState().getInferredAccessibleFacts().containsAll(((DAGChaseConfiguration)source).getState().getInferredAccessibleFacts())) {
			if (!this.hasStrictlyFewerFactsCheck || this.hasStrictlyFewerFactsCheck && ((DAGChaseConfiguration)source).getOutputFacts().size() < ((DAGChaseConfiguration)target).getOutputFacts().size()) 
				return true;
		}
		return false;
	}
	
	@Override
	public FastFactDominance clone() {
		return new FastFactDominance(this.hasStrictlyFewerFactsCheck);
	}
}
