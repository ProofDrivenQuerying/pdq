package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.List;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;

/**
 * Returns the results of a call to IterativeExecutor.finalIteration
 *
 * @author Efthymia Tsamoura
 *
 */
public class ExplorationResults {
	/** The list of non-dominated configurations that the IterativeExecutor.finalIteration method outputs.  **/
	private final List<DAGChaseConfiguration> nonDominatedConfigurations;
	
	/**  The lowest cost configuration*. */
	private final DAGChaseConfiguration best;

	/**
	 * Instantiates a new exploration results.
	 *
	 * @param output 		The list of non-dominated configurations that the IterativeExecutor.finalIteration method outputs.
	 * @param successful 		The list of non-dominated configurations that the IterativeExecutor.finalIteration method outputs and are also successful.
	 * @param bestConfiguration 		The lowest cost configuration
	 */
	public ExplorationResults(List<DAGChaseConfiguration> output,
			DAGChaseConfiguration bestConfiguration) {
		this.nonDominatedConfigurations = output;
		this.best = bestConfiguration;
	}

	/**
	 * Gets the output.
	 *
	 * @return the output
	 */
	public List<DAGChaseConfiguration> getNonDominatedConfigurations() {
		return this.nonDominatedConfigurations;
	}

	/**
	 * Gets the best.
	 *
	 * @return the best
	 */
	public DAGChaseConfiguration getBest() {
		return this.best;
	}
}