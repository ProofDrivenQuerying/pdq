package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
// TODO: Auto-generated Javadoc
/**
 * Iterates over the input collection of configurations to identify the minimum-cost one.
 * Given a set if input configuration C it removes from C the dominated and success dominated configurations 
 * and returns the minimum cost configurations.
 * 
 *
 * @author Efthymia Tsamoura
 *
 */
public class ConfigurationSpaceExplorationThread implements Callable<DAGChaseConfiguration> {

	/**  The input query*. */
	private final ConjunctiveQuery query;
	
	/**  Performs success dominance checks. */
	private final SuccessDominance successDominance;
	
	/**  Performs domination checks*. */
	private final Dominance[] dominance;

	/**  Input configurations. */
	private final Queue<DAGChaseConfiguration> input;

	/**  Classes of structurally equivalent configurations. */
	private final DAGEquivalenceClasses equivalenceClasses;

	/**  The minimum cost closed and successful configuration found so far. */
	private DAGChaseConfiguration best = null;

	/**  The output non-dominated and not successful configurations. */
	private final Set<DAGChaseConfiguration> output;

	/**  The output non-dominated and successful (and not closed) configurations. */
	private final Set<DAGChaseConfiguration> successful;

	/**
	 * Instantiates a new exploration thread.
	 *
	 * @param query the query
	 * @param input 		Input configurations
	 * @param equivalenceClasses 		Classes of structurally equivalent configurations
	 * @param best 		The minimum cost closed and successful configuration found so far
	 * @param detector 		 Detects query matches
	 * @param successDominance 		Performs success dominance checks
	 * @param dominance the dominance
	 * @param output 		The output non-dominated and not successful configurations
	 * @param successfulConfigurations 		The output non-dominated and successful (and not closed) configurations
	 */
	public ConfigurationSpaceExplorationThread(
			ConjunctiveQuery query,
			Queue<DAGChaseConfiguration> input,
			DAGEquivalenceClasses equivalenceClasses,
			DAGChaseConfiguration best,
			SuccessDominance successDominance,
			Dominance[] dominance,
			Set<DAGChaseConfiguration> output,
			Set<DAGChaseConfiguration> successfulConfigurations
			) {	
		Preconditions.checkNotNull(query);
	
		this.best = best == null ? null : best.clone();
		this.query = query;
		this.input = input;
		this.equivalenceClasses = equivalenceClasses;
		this.output = output;
		this.successful = successfulConfigurations;
		this.successDominance = successDominance;
		this.dominance = dominance;
	}

	/**
	 * Call.
	 *
	 * @return DAGChaseConfiguration
	 * @throws Exception the exception
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public DAGChaseConfiguration call() throws Exception {
		DAGChaseConfiguration configuration;
		//Poll the next configuration
		while((configuration = this.input.poll()) != null) {			
			//If the configuration is not dominated
			DAGChaseConfiguration dominator = this.equivalenceClasses.dominate(this.dominance, configuration);
			if (dominator != null
					//ExplorerUtils.isDominated(this.dag, binConfig) != null || ExplorerUtils.isDominated(binConfigs, binConfig) != null
					) {
			} else {
				//Assess its potential
				if (ConfigurationUtility.getPotential(configuration, this.best == null ? null : this.best.getPlan(), this.best == null ? null : this.best.getCost(), this.successDominance)) {
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
	 * Sets the best configuration.
	 *
	 * @param configuration DAGChaseConfiguration
	 */
	private void setBestConfiguration(DAGChaseConfiguration configuration) {
		if (this.best != null && this.best.getCost() == null && configuration!=null && configuration.getCost() != null) {
			this.best = configuration;
			return;
		}
		if (this.best == null
				|| (this.best != null && configuration != null
				&& this.best.getCost().greaterThan(configuration.getCost()))) {
			this.best = configuration;
		}
	}
}
