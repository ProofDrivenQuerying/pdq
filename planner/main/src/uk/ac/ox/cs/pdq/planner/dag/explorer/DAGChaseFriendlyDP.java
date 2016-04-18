package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
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
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Chase friendly dag explorer. The exploration proceeds similarly to the GenericExplorer.
 * First, it checks whether or not the configurations
 * to be composed could lead to the optimal solution prior to creating the corresponding binary configuration.
 * If yes, then it creates a new binary configuration which is only further considered
 * if it is not dominated by an existing configuration.
 *
 * @author Efthymia Tsamoura
 *
 */
public class DAGChaseFriendlyDP extends DAGGeneric {

	/**  Removes dominated configurations *. */
	private final Dominance[] dominance;
	
	/**
	 * Instantiates a new DAG chase friendly dp.
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
	 * @param dominance 		Removes dominated configurations
	 * @param filter 		Filters out configurations at the end of each iteration
	 * @param validators the validators
	 * @param maxDepth 		The maximum depth to explore
	 * @param orderAware True if pair selection is order aware
	 * @throws PlannerException the planner exception
	 */
	public DAGChaseFriendlyDP(
			EventBus eventBus, 
			boolean collectStats,
			PlannerParameters parameters,
			ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery,
			Schema schema,
			AccessibleSchema accessibleSchema, 
			Chaser chaser,
			HomomorphismDetector detector,
			CostEstimator<DAGPlan> costEstimator,
			SuccessDominance successDominance,
			Dominance[] dominance,
			Filter filter, 
			List<Validator> validators,
			int maxDepth, 
			boolean orderAware) throws PlannerException {
		super(eventBus, collectStats, parameters, query, accessibleQuery, schema, accessibleSchema, chaser, detector, costEstimator,
				successDominance, filter, validators, maxDepth, orderAware);
		Preconditions.checkNotNull(dominance);
		Preconditions.checkNotNull(successDominance);
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
				if (this.bestPlan == null || !this.successDominance.isDominated(configuration.getPlan(), this.bestPlan)) {
					configuration.reasonUntilTermination(this.chaser, this.accessibleQuery, this.accessibleSchema.getInferredAccessibilityAxioms());
					if (ExplorerUtils.isDominated(this.dominance, this.getRight(), configuration) == null
							&& ExplorerUtils.isDominated(this.dominance, last.values(), configuration) == null) {
						if (configuration.isClosed()
								&& configuration.isSuccessful(this.accessibleQuery)) {
							this.setBestPlan(configuration);
						} else {
							last.put(pair, configuration);
						}
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
