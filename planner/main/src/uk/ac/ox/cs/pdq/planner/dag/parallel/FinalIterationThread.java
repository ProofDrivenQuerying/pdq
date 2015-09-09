package uk.ac.ox.cs.pdq.planner.dag.parallel;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

/**
 * Iterates over the input collection of configurations to identify the minimum-cost one
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class FinalIterationThread extends ExecutionThread implements Callable<DAGChaseConfiguration> {

	/** Performs success dominance checks*/
	private final SuccessDominance successDominance;

	/** Detects homomorphisms*/
	private final HomomorphismDetector detector;

	/** Input configurations*/
	private final Queue<DAGChaseConfiguration> configurations;

	/** Classes of structurally equivalent configurations */
	private final DAGEquivalenceClasses equivalenceClasses;

	/** The minimum cost closed and successful configuration found so far */
	private DAGChaseConfiguration best = null;

	/** The output non-dominated and not successful configurations */
	private final Set<DAGChaseConfiguration> output;

	/** The output non-dominated and successful (and not closed) configurations */
	private final Set<DAGChaseConfiguration> successful;

	/**
	 *
	 * @param configurations
	 * 		Input configurations
	 * @param equivalenceClasses
	 * 		Classes of structurally equivalent configurations
	 * @param best
	 * 		The minimum cost closed and successful configuration found so far
	 * @param detector
	 * 		Detects homomorphisms
	 * @param successDominance
	 * 		Performs success dominance checks
	 * @param output
	 * 		The output non-dominated and not successful configurations
	 * @param successfulConfigurations
	 * 		The output non-dominated and successful (and not closed) configurations
	 */
	public FinalIterationThread(
			Queue<DAGChaseConfiguration> configurations,
			DAGEquivalenceClasses equivalenceClasses,
			DAGChaseConfiguration best,
			HomomorphismDetector detector,
			SuccessDominance successDominance,
			Set<DAGChaseConfiguration> output,
			Set<DAGChaseConfiguration> successfulConfigurations
			) {
		this.best = best == null ? null : best.clone();
		this.detector = detector;
		this.configurations = configurations;
		this.equivalenceClasses = equivalenceClasses;
		this.output = output;
		this.successful = successfulConfigurations;
		this.successDominance = successDominance;
	}

	/**
	 * @return DAGChaseConfiguration
	 * @throws Exception
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public DAGChaseConfiguration call() throws Exception {
		DAGChaseConfiguration configuration;
		//Poll the next configuration
		while((configuration = this.configurations.poll()) != null) {
			if(configuration.getState() instanceof DatabaseChaseState) {
				((DatabaseChaseState)configuration.getState()).setManager((DBHomomorphismManager) this.detector);
			}
			//If the configuration is not dominated
			DAGChaseConfiguration dominator = this.equivalenceClasses.dominate(configuration);
			if (dominator != null
					//ExplorerUtils.isDominated(this.dag, binConfig) != null || ExplorerUtils.isDominated(binConfigs, binConfig) != null
					) {
			} else {
				//Assess its potential
				if (this.getPotential(configuration, this.best == null ? null : this.best.getPlan(), this.successDominance)) {
					//Find the configurations dominated by the current one and remove them
					Collection<DAGChaseConfiguration> dominated = this.equivalenceClasses.dominatedBy(configuration);
					if(!dominated.isEmpty()) {
						this.output.removeAll(dominated);
						this.equivalenceClasses.removeAll(dominated);
						this.successful.removeAll(dominated);
					}
					//Check for query match
					boolean matchesQuery = configuration.isSuccessful();
					if (configuration.isClosed() && matchesQuery) {
						this.setBestConfiguration(configuration);
						//log.trace(this.bestConfiguration + "\t" + this.bestConfiguration.getPlan().getCost());
					}
					//Update the best configuration
					else {
						this.equivalenceClasses.addEntry(configuration);
						this.output.add(configuration);
					}
					if(matchesQuery) {
						this.successful.add(configuration);
					}
				}
			}
		}
		return this.best;
	}

	/**
	 * @param configuration DAGChaseConfiguration
	 */
	private void setBestConfiguration(DAGChaseConfiguration configuration) {
		if (this.best == null
				|| (this.best != null && configuration != null
				&& this.best.getPlan().getCost().greaterThan(configuration.getPlan().getCost()))) {
			this.best = configuration;
		}
	}
}
