package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

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

	private final Dominance[] dominance;
	
	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param parameters
	 * @param query
	 * @param accessibleSchema
	 * @param chaser
	 * @param detector
	 * @param costEstimator
	 * @param successDominance
	 * @param dominance
	 * @param filter
	 * @param validators
	 * @param maxDepth
	 * @param orderAware
	 * @throws PlannerException
	 */
	public DAGSimpleDP(
			EventBus eventBus, 
			boolean collectStats,
			PlannerParameters parameters,
			Query<?> query,
			Query<?> accessibleQuery,
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
		super(eventBus, collectStats, parameters, query, accessibleQuery, schema, accessibleSchema, chaser, detector, costEstimator, successDominance, filter, validators, maxDepth, orderAware);
		Preconditions.checkNotNull(dominance);
		this.dominance = dominance;
	}

	/**
	 * @return Collection<DAGChaseConfiguration>
	 * @throws PlannerException
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
				configuration.chase(this.chaser, this.accessibleQuery, this.accessibleSchema.getInferredAccessibilityAxioms());
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
