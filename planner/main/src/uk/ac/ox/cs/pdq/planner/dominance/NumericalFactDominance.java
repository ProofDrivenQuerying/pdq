package uk.ac.ox.cs.pdq.planner.dominance;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.Configuration;

// TODO: Auto-generated Javadoc
/**
 * Numerical success dominance. A configuration c dominates a configuration c', if c has >= facts than c'.
 * @author Efthymia Tsamoura
 */
public class NumericalFactDominance implements FactDominance{

	/**
	 * Checks if is dominated.
	 *
	 * @param source C
	 * @param target C
	 * @return BijectiveMap<Constant,Constant>
	 */
	@Override
	public boolean isDominated(Configuration source, Configuration target) {
		if(source.equals(target)) 
			return false;
		else if (source instanceof DAGChaseConfiguration && target instanceof DAGChaseConfiguration && 
				source.getInput().containsAll(target.getInput()) && 
				((DAGChaseConfiguration)source).getOutputFacts().size() <= ((DAGChaseConfiguration)target).getOutputFacts().size()) 
			return true;
		return false;
	}
	
	/**
	 * Clone.
	 *
	 * @return NumericalFactDominance<C>
	 */
	@Override
	public NumericalFactDominance clone() {
		return new NumericalFactDominance();
	}
}