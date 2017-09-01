package uk.ac.ox.cs.pdq.planner.dag.explorer;

import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.CANDIDATES;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.CONFIGURATIONS;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.SynchronizedEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.ExplorationResults;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.IterativeExecutor;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

// TODO: Auto-generated Javadoc
/**
 * An explorer for plans using ideas from dynamic programming. It performs parallel chasing and
 * (success-)dominance, equivalence and success checks in parallel
 * @author Efthymia Tsamoura
 *
 */
public class DAGOptimized extends DAGExplorer {

	/**
	 * The maximum depth we can explore. The exploration ends when
	 * there does not exist any configuration with depth < maxDepth
	 */
	protected final int maxDepth;

	/**  The current exploration depth. */
	protected int depth;

	/**  Performs parallel chasing. */
	private final IterativeExecutor reasoningThreads;

	/**  Iterate over all newly created configurations in parallel and returns the best configuration. */
	private final IterativeExecutor explorationThreads;

	/**  Filters out configurations at the end of each iteration. */
	private final Filter filter;

	/**  Configurations produced during the previous round. */
	private final Queue<DAGChaseConfiguration> left;

	/**  Classes of structurally equivalent configurations. */
	private final DAGEquivalenceClasses equivalenceClasses;

	
	/**
	 * Instantiates a new DAG optimized.
	 *
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param parameters the parameters
	 * @param query 		The input user query
	 * @param accessibleQuery 		The accessible counterpart of the user query
	 * @param schema 		The input schema
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 * @param chaser 		Runs the chase algorithm
	 * @param detector 		Detects homomorphisms during chasing
	 * @param costEstimator 		Estimates the cost of a plan
	 * @param filter 		Filters out configurations at the end of each iteration
	 * @param reasoningThreads 		Performs parallel chasing
	 * @param explorationThreads 		Iterates over all newly created configurations in parallel and returns the best configuration
	 * @param maxDepth 		The maximum depth to explore
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	public DAGOptimized(
			EventBus eventBus, 
			boolean collectStats, 
			PlannerParameters parameters,
			ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery,
			AccessibleSchema accessibleSchema, 
			Chaser chaser, 
			DatabaseConnection connection,
			CostEstimator costEstimator,
			Filter filter,
			IterativeExecutor reasoningThreads,
			IterativeExecutor explorationThreads,
			int maxDepth) throws PlannerException, SQLException {
		super(eventBus, collectStats, parameters,
				query, accessibleQuery, accessibleSchema, chaser, connection, costEstimator);
		Preconditions.checkNotNull(reasoningThreads);
		Preconditions.checkNotNull(explorationThreads);
		this.filter = filter;
		this.reasoningThreads = reasoningThreads;
		this.explorationThreads = explorationThreads;
		this.maxDepth = maxDepth;
		List<DAGChaseConfiguration> initialConfigurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(this.parameters, this.query, this.accessibleQuery, this.accessibleSchema, this.chaser, this.connection);
		if(this.filter != null) {
			Collection<DAGChaseConfiguration> toDelete = this.filter.filter(initialConfigurations);
			initialConfigurations.removeAll(toDelete);
		}
		this.left = new ConcurrentLinkedQueue<>();
		this.equivalenceClasses = new SynchronizedEquivalenceClasses();
		this.left.addAll(initialConfigurations);
		for(DAGChaseConfiguration initialConfiguration: initialConfigurations) {
			this.equivalenceClasses.addEntry(initialConfiguration);
		}
	}

	/**
	 * _explore.
	 *
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	public void _explore() throws PlannerException, LimitReachedException {
		if (this.depth > this.maxDepth) {
			this.forcedTermination = true;
			return;
		}
		//Check the ApplyRule configurations for success
		if (this.depth == 1) {
			for (DAGChaseConfiguration configuration: this.left) {
				Cost cost = this.costEstimator.cost(configuration.getPlan());
				configuration.setCost(cost);
				if (this.bestPlan == null
						|| (configuration.isClosed() && configuration.getCost().lessThan(this.bestCost))) {
					if (configuration.isClosed() && configuration.isSuccessful(this.accessibleQuery)) {
						this.setBestPlan(configuration);
					}
				}
			}
			this.stats.set(CONFIGURATIONS, this.left.size());
		} else if (this.depth > 1) {
			this.checkLimitReached();
			//Perform parallel chasing
			Collection<DAGChaseConfiguration> configurations =
					this.reasoningThreads.reason(this.depth,
							this.left,
							this.equivalenceClasses.getConfigurations(),
							this.accessibleQuery,
							this.accessibleSchema.getInferredAccessibilityAxioms(),
							this.bestConfiguration,
							this.equivalenceClasses,
							true,
							Double.valueOf((this.maxElapsedTime - (this.elapsedTime/1e6))).longValue(),
							TimeUnit.MILLISECONDS);
			if(configurations == null || configurations.isEmpty()) {
				this.forcedTermination = true;
				return;
			}

			this.checkLimitReached();
			//Iterate over all newly created configurations in parallel and return the best configuration
			ExplorationResults results = this.explorationThreads.explore(
					this.accessibleQuery,
					new ConcurrentLinkedQueue<>(configurations),
					this.equivalenceClasses,
					this.bestConfiguration,
					Double.valueOf((this.maxElapsedTime - (this.elapsedTime/1e6))).longValue(),
					TimeUnit.MILLISECONDS);

			//Stop if no new configuration is being found
			if (results == null) {
				this.forcedTermination = true;
				return;
			}
			//Update the best configuration
			List<DAGChaseConfiguration> output = results.getOutput();
			DAGChaseConfiguration bestResult = results.getBest();
			if (bestResult !=  null) {
				this.setBestPlan(bestResult);
			}

			if (output.isEmpty()) {
				this.forcedTermination = true;
				return;
			}

			this.left.clear();
			this.left.addAll(CollectionUtils.intersection(output, this.equivalenceClasses.getConfigurations()));

			//Filter out configurations
			if(this.filter != null) {
				Collection<DAGChaseConfiguration> toDelete = this.filter.filter(this.equivalenceClasses.getConfigurations());
				this.equivalenceClasses.removeAll(toDelete);
				this.left.removeAll(toDelete);
			}

 			this.stats.set(CONFIGURATIONS, this.equivalenceClasses.size());
			this.stats.set(CANDIDATES, this.left.size());
		}
		this.depth++;
	}

}
