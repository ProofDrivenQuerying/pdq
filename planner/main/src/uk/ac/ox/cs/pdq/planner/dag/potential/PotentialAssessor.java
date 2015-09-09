package uk.ac.ox.cs.pdq.planner.dag.potential;

import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;

/**
 * Assesses the potential of a configuration to lead to the minimum-cost configuration
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public interface PotentialAssessor {

	/**
	 * @param configuration
	 * @return true if the input configuration could lead to the minimum-cost configuration
	 */
	Boolean getPotential(DAGChaseConfiguration configuration);

	/**
	 * @param left
	 * @param right
	 * @return true if the configuration composed from the left and right input configurations could lead to the minimum-cost configuration
	 */
	Boolean getPotential(DAGChaseConfiguration left, DAGChaseConfiguration right);

	/**
	 * Updates the best plan. It is used to perform success domination checks
	 * @param bestPlan
	 */
	void update(DAGPlan bestPlan);

	/**
	 * @param successDominance
	 */
	void update(SuccessDominance successDominance);

	/**
	 * @return PotentialAssessor
	 */
	PotentialAssessor clone();
}
