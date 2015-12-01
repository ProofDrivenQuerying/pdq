package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

/**
 * Iterates over the input collection of configurations to identify the minimum-cost one
 *
 * @author Efthymia Tsamoura
 *
 */
public class ExplorationThread implements Callable<DAGChaseConfiguration> {

	private final Query<?> query;
	
	/** Performs success dominance checks*/
	private final SuccessDominance successDominance;
	
	private final Dominance[] dominance;

	/** Detects homomorphisms*/
	private final HomomorphismDetector detector;

	/** Input configurations*/
	private final Queue<DAGChaseConfiguration> input;

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
	 * @param input
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
	public ExplorationThread(
			Query<?> query,
			Queue<DAGChaseConfiguration> input,
			DAGEquivalenceClasses equivalenceClasses,
			DAGChaseConfiguration best,
			HomomorphismDetector detector,
			SuccessDominance successDominance,
			Dominance[] dominance,
			Set<DAGChaseConfiguration> output,
			Set<DAGChaseConfiguration> successfulConfigurations
			) {	
		Preconditions.checkNotNull(query);
		Preconditions.checkNotNull(detector);
	
		this.best = best == null ? null : best.clone();
		this.query = query;
		this.detector = detector;
		this.input = input;
		this.equivalenceClasses = equivalenceClasses;
		this.output = output;
		this.successful = successfulConfigurations;
		this.successDominance = successDominance;
		this.dominance = dominance;
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
		while((configuration = this.input.poll()) != null) {
			if(configuration.getState() instanceof DatabaseChaseState) {
				((DatabaseChaseState)configuration.getState()).setManager((DBHomomorphismManager) this.detector);
			}
			//If the configuration is not dominated
			DAGChaseConfiguration dominator = this.equivalenceClasses.dominate(this.dominance, configuration);
			if (dominator != null
					//ExplorerUtils.isDominated(this.dag, binConfig) != null || ExplorerUtils.isDominated(binConfigs, binConfig) != null
					) {
			} else {
				//Assess its potential
				if (this.getPotential(configuration, this.best == null ? null : this.best.getPlan(), this.successDominance)) {
					//Find the configurations dominated by the current one and remove them
					Collection<DAGChaseConfiguration> dominated = this.equivalenceClasses.dominatedBy(this.dominance, configuration);
					if(!dominated.isEmpty()) {
						this.output.removeAll(dominated);
						this.equivalenceClasses.removeAll(dominated);
						this.successful.removeAll(dominated);
					}
					//Check for query match
					boolean matchesQuery = false;
					if (configuration.isClosed() && (matchesQuery = configuration.isSuccessful(this.query)) == true) {
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
	
	/**
	 *
	 * @param configuration
	 * @param bestPlan
	 * 		Best plan found so far
	 * @param successDominance
	 * 		Performs success dominance checks
	 * @return true if the input configuration is not success dominated by the best plan
	 */
	protected Boolean getPotential(DAGChaseConfiguration configuration, DAGPlan bestPlan, SuccessDominance successDominance) {
		return ConfigurationUtility.getPotential(configuration, bestPlan, successDominance);
	}
}
