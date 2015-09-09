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

	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param initialConfigurations
	 * 		ApplyRule configurations to initilise the explorer
	 * @param filter
	 * 		Filters out configurations at the end of each iteration
	 * @param validator
	 * 		Validates pairs of configurations to be composed
	 * @param maxDepth
	 * 		The maximum depth to explore
	 * @param orderAware True if pair selection is order aware
	 * @throws PlannerException
	 */
	public DAGChaseFriendlyDP(
			EventBus eventBus, boolean collectStats,
			List<DAGChaseConfiguration> initialConfigurations,
			Filter filter, List<Validator> validators,
			int maxDepth, boolean orderAware) throws PlannerException {
		super(eventBus, collectStats,
				initialConfigurations, filter,  validators, maxDepth, orderAware);
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
				if (ExplorerUtils.getPotential(configuration, this.bestPlan)) {
					configuration.chase();
					if (ExplorerUtils.isDominated(this.getRight(), configuration) == null
							&& ExplorerUtils.isDominated(last.values(), configuration) == null) {
						if (configuration.isClosed()
								&& configuration.isSuccessful()) {
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
