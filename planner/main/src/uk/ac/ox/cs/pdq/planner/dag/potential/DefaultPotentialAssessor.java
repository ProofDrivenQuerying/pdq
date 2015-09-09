package uk.ac.ox.cs.pdq.planner.dag.potential;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;

/**
 * A plan has the potential to lead to the minimum-cost configuration if it is not success dominated by the best plan found so far
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class DefaultPotentialAssessor implements PotentialAssessor{

	private DAGPlan bestPlan;
	private final CostEstimator<DAGPlan> costEstimator;
	private SuccessDominance successDominance;

	/**
	 * Constructor for DefaultPotentialAssessor.
	 * @param bestPlan Plan
	 * @param costEstimator CostEstimator<DAGPlan>
	 * @param successDominance SuccessDominance
	 */
	public DefaultPotentialAssessor(DAGPlan bestPlan,
			CostEstimator<DAGPlan> costEstimator,
			SuccessDominance successDominance) {
		this.bestPlan = bestPlan;
		this.costEstimator = costEstimator;
		this.successDominance = successDominance;
	}

	/**
	 * @param configuration DAGConfiguration
	 * @return Boolean
	 * @see uk.ac.ox.cs.pdq.potential.PotentialAssessor#getPotential(DAGConfiguration)
	 */
	@Override
	public Boolean getPotential(DAGChaseConfiguration configuration) {
		return ConfigurationUtility.getPotential(configuration, this.bestPlan, this.successDominance);
	}

	/**
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @return Boolean
	 * @see uk.ac.ox.cs.pdq.potential.PotentialAssessor#getPotential(DAGConfiguration, DAGConfiguration)
	 */
	@Override
	public Boolean getPotential(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		return ConfigurationUtility.getPotential(left, right, this.bestPlan, this.costEstimator, this.successDominance);
	}

	/**
	 * @param bestPlan Plan
	 * @see uk.ac.ox.cs.pdq.planner.dag.potential.PotentialAssessor#update(Plan)
	 */
	@Override
	public void update(DAGPlan bestPlan) {
		this.bestPlan = bestPlan;
	}

	/**
	 * @param sd SuccessDominance
	 * @see uk.ac.ox.cs.pdq.planner.dag.potential.PotentialAssessor#update(SuccessDominance)
	 */
	@Override
	public void update(SuccessDominance sd) {
		this.successDominance = sd;
	}

	/**
	 * @return DefaultPotentialAssessor
	 * @see uk.ac.ox.cs.pdq.planner.dag.potential.PotentialAssessor#clone()
	 */
	@Override
	public DefaultPotentialAssessor clone() {
		return new DefaultPotentialAssessor(this.bestPlan, this.costEstimator.clone(), this.successDominance.clone());
	}
}
