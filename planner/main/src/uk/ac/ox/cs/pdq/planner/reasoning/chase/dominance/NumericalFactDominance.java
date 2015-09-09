package uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance;

import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

/**
 * TODO put description
 *
 * @author Efthymia Tsamoura
 */
public class NumericalFactDominance implements FactDominance{

	/**
	 * @param source C
	 * @param target C
	 * @return BijectiveMap<Constant,Constant>
	 */
	@Override
	public boolean isDominated(ChaseConfiguration source, ChaseConfiguration target) {
		if(source.equals(target)) {
			return false;
		}
		if (source.getInput().containsAll(target.getInput())
			&& source.getOutputFacts().size() <= target.getOutputFacts().size()) {
			return true;
		}
		return false;
	}

	/**
	 * @return NumericalFactDominance<C>
	 */
	@Override
	public NumericalFactDominance clone() {
		return new NumericalFactDominance();
	}
}