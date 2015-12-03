package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;

/**
 * Returns the results of a call to IterativeExecutor.finalIteration
 *
 * @author Efthymia Tsamoura
 *
 */
public class ExplorationThreadResults {
	private final List<DAGAnnotatedPlan> output;
	private final Set<DAGAnnotatedPlan> successful;
	private final DAGAnnotatedPlan best;

	/**
	 * Constructor for FinalIterationThreadResults.
	 * @param output List<DAGAnnotatedPlan>
	 * @param successfulConfigurations Set<DAGAnnotatedPlan>
	 * @param bestConfiguration DAGAnnotatedPlan
	 */
	public ExplorationThreadResults(List<DAGAnnotatedPlan> output,
			Set<DAGAnnotatedPlan> successfulConfigurations,
			DAGAnnotatedPlan bestConfiguration) {
		this.output = output;
		this.best = bestConfiguration;
		this.successful = successfulConfigurations;
	}

	/**
	 * @return List<DAGAnnotatedPlan>
	 */
	public List<DAGAnnotatedPlan> getOutput() {
		return this.output;
	}

	/**
	 * @return DAGAnnotatedPlan
	 */
	public DAGAnnotatedPlan getBest() {
		return this.best;
	}

	/**
	 * @return Set<DAGAnnotatedPlan>
	 */
	public Set<DAGAnnotatedPlan> getSuccessful() {
		return this.successful;
	}
}