package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;

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

	/**
	 * @param eventBus
	 * @param collectStats
	 * @param schema
	 * 		The input schema
	 * @param accessibleSchema
	 *      The accessible version of the input schema
	 * @param query
	 *      The input query
	 * @param accessibleQuery
	 * @param factory
	 * 		Returns the ApplyRule configurations
	 * @param maxDepth
	 *      The maximum depth in the dag we can explore
	 * @param filter Filter
	 * @param validator Validator
	 * @throws PlannerException
	 */
	public DAGSimpleDP(
			EventBus eventBus, boolean collectStats,
			List<DAGChaseConfiguration> initialConfigurations,
			Filter filter, List<Validator> validators,
			int maxDepth, boolean orderAware) throws PlannerException {
		super(eventBus, collectStats,
				initialConfigurations, filter, validators, maxDepth, orderAware);
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
//						pair.getLeft().getSchema(),
						pair.getLeft().getAccessibleSchema(),
						pair.getLeft().getQuery(),
						pair.getLeft().getChaser(),
						null,
						pair.getLeft().getDominanceDetectors(),
						pair.getLeft().getSuccessDominanceDetector(),
						pair.getLeft().getCostEstimator(),
						pair.getLeft(),
						pair.getRight(),
						true);
				if (ExplorerUtils.isDominated(this.getRight(), configuration) == null &&
						ExplorerUtils.isDominated(last.values(), configuration) == null &&
						ExplorerUtils.getPotential(configuration, this.bestPlan)) {
					if (configuration.isClosed()
							&& configuration.isSuccessful()) {
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
