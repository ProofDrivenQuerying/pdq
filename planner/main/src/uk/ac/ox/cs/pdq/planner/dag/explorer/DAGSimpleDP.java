package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Simple dag explorer. The exploration proceeds similarly to the GenericExplorer.
 * First, it creates a new binary configuration and checks whether or not it could lead to the optimal solution.
 * Each newly created configuration is only further considered
 * if it is not dominated by an existing configuration.
 *
 * @author Efthymia Tsamoura
 *
 */
public class DAGSimpleDP extends DAGGeneric {

	/**  Removes dominated configurations *. */
	private final Dominance[] dominance;
	
	/**
	 * Instantiates a new DAG simple dp.
	 *
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param parameters the parameters
	 * @param query 		The input user query
	 * @param accessibleQuery 		The accessible counterpart of the user query
	 * @param schema 		The input schema
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 * @param chaser 		Saturates configurations using the chase algorithm
	 * @param detector 		Detects homomorphisms during chasing
	 * @param costEstimator 		Estimates the cost of a plan
	 * @param successDominance 		Removes success dominated configurations
	 * @param dominance the dominance
	 * @param filter 		Filters out configurations at the end of each iteration
	 * @param validators 		Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions.
	 * @param maxDepth 		The maximum depth to explore
	 * @param orderAware the order aware
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	public DAGSimpleDP(
			EventBus eventBus, 
			boolean collectStats,
			PlannerParameters parameters,
			ReasoningParameters reasoningParameters,
			ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery,
			Schema schema,
			AccessibleSchema accessibleSchema, 
			Chaser chaser,
			DatabaseConnection dbConn,
			CostEstimator<DAGPlan> costEstimator,
			SuccessDominance successDominance,
			Dominance[] dominance,
			Filter filter, 
			List<Validator> validators,
			int maxDepth, 
			boolean orderAware) throws PlannerException, SQLException {
		super(eventBus, collectStats, parameters,reasoningParameters, query, accessibleQuery, schema, accessibleSchema, chaser, dbConn, costEstimator, successDominance, filter, validators, maxDepth, orderAware);
		Preconditions.checkNotNull(dominance);
		this.dominance = dominance;
	}

	/**
	 * Main loop.
	 *
	 * @return Collection<DAGChaseConfiguration>
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	protected Collection<DAGChaseConfiguration> mainLoop() throws PlannerException, LimitReachedException {
		Map<Pair<DAGChaseConfiguration,DAGChaseConfiguration>,DAGChaseConfiguration> last = new HashMap<>();
		Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
		while ((pair = this.selector.getNext(this.depth)) != null) {
			if(!last.containsKey(pair)) {
				BinaryConfiguration configuration = new BinaryConfiguration(
						pair.getLeft(),
						pair.getRight());
				this.costEstimator.cost(configuration.getPlan());
				configuration.reasonUntilTermination(this.chaser, this.accessibleQuery, this.accessibleSchema.getInferredAccessibilityAxioms());
				if (this.bestPlan == null || !this.successDominance.isDominated(configuration.getPlan(), this.bestPlan) &&
						ExplorerUtils.isDominated(this.dominance, this.getRight(), configuration) == null &&
						ExplorerUtils.isDominated(this.dominance, last.values(), configuration) == null
								) {
					if (configuration.isClosed()
							&& configuration.isSuccessful(this.accessibleQuery)) {
						this.setBestPlan(configuration);
					} else {
						last.put(pair, configuration);
					}
				}
			}
			if (this.checkLimitReached()) {
				this.forcedTermination = true;
				break;
			}
		}
		return last.values();
	}
}
