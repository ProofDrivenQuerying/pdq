package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;

/**
 * Returns the results of a call to IterativeExecutor.finalIteration
 *
 * @author Efthymia Tsamoura
 *
 */
public class ExplorationResults {
	/** The list of non-dominated configurations that the IterativeExecutor.finalIteration method outputs.  **/
	private final List<DAGChaseConfiguration> output;
	/** The list of non-dominated configurations that the IterativeExecutor.finalIteration method outputs and are also successful. **/
	private final Set<DAGChaseConfiguration> successful;
	/** The lowest cost configuration**/
	private final DAGChaseConfiguration best;

	/**
	 * 
	 * @param output
	 * 		The list of non-dominated configurations that the IterativeExecutor.finalIteration method outputs.
	 * @param successful
	 * 		The list of non-dominated configurations that the IterativeExecutor.finalIteration method outputs and are also successful.
	 * @param bestConfiguration
	 * 		The lowest cost configuration
	 */
	public ExplorationResults(List<DAGChaseConfiguration> output,
			Set<DAGChaseConfiguration> successful,
			DAGChaseConfiguration bestConfiguration) {
		this.output = output;
		this.best = bestConfiguration;
		this.successful = successful;
	}

	public List<DAGChaseConfiguration> getOutput() {
		return this.output;
	}

	public DAGChaseConfiguration getBest() {
		return this.best;
	}

	public Set<DAGChaseConfiguration> getSuccessful() {
		return this.successful;
	}
}