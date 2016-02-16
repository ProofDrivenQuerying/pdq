/*
 * 
 */
package uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance;

import uk.ac.ox.cs.pdq.planner.cardinality.CardinalityEstimator;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Open configuration domination. A configuration c dominates a configuration c',
 * if c both cost- and fact- dominates c' with one of the two being strict.
 * When both configurations are open, then a simple plan cost estimator is used
 * to assess the configurations' costs;
 * otherwise, the costs of their corresponding (closed) plans are considered.
 *
 * @author Efthymia Tsamoura
 */
public class LooseQualityDominance implements Dominance<DAGAnnotatedPlan> {

	/** The cardinality estimator. */
	private final CardinalityEstimator cardinalityEstimator;
	
	/**
	 * Instantiates a new loose quality dominance.
	 *
	 * @param cardinalityEstimator the cardinality estimator
	 */
	public LooseQualityDominance(CardinalityEstimator cardinalityEstimator) {
		Preconditions.checkNotNull(cardinalityEstimator);
		this.cardinalityEstimator = cardinalityEstimator;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance#isDominated(uk.ac.ox.cs.pdq.planner.reasoning.Configuration, uk.ac.ox.cs.pdq.planner.reasoning.Configuration)
	 */
	@Override
	public boolean isDominated(DAGAnnotatedPlan source, DAGAnnotatedPlan target) {
		return source.getSize().compareTo(target.getSize()) >= 0 &&
				source.getQuality() <= target.getQuality();
			
	}
	
	/**
	 * Clone.
	 *
	 * @return StrictOpenDominance
	 * @see uk.ac.ox.cs.pdq.dag.dominance.Dominance#clone()
	 */
	@Override
	public LooseQualityDominance clone() {
		return new LooseQualityDominance(this.cardinalityEstimator.clone());
	}
}