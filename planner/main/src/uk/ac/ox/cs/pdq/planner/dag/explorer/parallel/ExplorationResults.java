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
	private final List<DAGChaseConfiguration> output;
	private final Set<DAGChaseConfiguration> successful;
	private final DAGChaseConfiguration best;

	/**
	 * Constructor for FinalIterationThreadResults.
	 * @param output List<DAGChaseConfiguration>
	 * @param successfulConfigurations Set<DAGChaseConfiguration>
	 * @param bestConfiguration DAGChaseConfiguration
	 */
	public ExplorationResults(List<DAGChaseConfiguration> output,
			Set<DAGChaseConfiguration> successfulConfigurations,
			DAGChaseConfiguration bestConfiguration) {
		this.output = output;
		this.best = bestConfiguration;
		this.successful = successfulConfigurations;
	}

	/**
	 * @return List<DAGChaseConfiguration>
	 */
	public List<DAGChaseConfiguration> getOutput() {
		return this.output;
	}

	/**
	 * @return DAGChaseConfiguration
	 */
	public DAGChaseConfiguration getBest() {
		return this.best;
	}

	/**
	 * @return Set<DAGChaseConfiguration>
	 */
	public Set<DAGChaseConfiguration> getSuccessful() {
		return this.successful;
	}
}