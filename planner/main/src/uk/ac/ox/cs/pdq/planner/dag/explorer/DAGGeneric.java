package uk.ac.ox.cs.pdq.planner.dag.explorer;

import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.CONFIGURATIONS;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.CANDIDATES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

/**
 * Simple dag explorer. It searches the space of binary configurations exhaustively
 *
 * @author Efthymia Tsamoura
 *
 */
public class DAGGeneric extends DAGExplorer {
	
	/**
	 * The maximum depth we can explore. The exploration ends when
	 * there does not exist any configuration with depth < maxDepth
	 */
	protected final int maxDepth;
	/** Filters out configurations at the end of each iteration*/
	private final Filter filter;
	/** Check whether the binary configuration composed from a given configuration pair satisfies given shape restrictions.*/
	private final List<Validator> validators;
	private final List<DAGChaseConfiguration> left;
	private final List<DAGChaseConfiguration> right;
	/** The current exploration depth */
	protected int depth = 1;
	/** True if pair selection is order aware */
	protected boolean orderAware;
	/** Returns pairs of configurations to combine */
	protected PairSelector selector;
	/** Removes success dominated configurations **/
	protected final SuccessDominance successDominance;

	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param parameters
	 * @param query
	 * 		The input user query
	 * @param accessibleQuery
	 * 		The accessible counterpart of the user query
	 * @param schema
	 * 		The input schema
	 * @param accessibleSchema
	 * 		The accessible counterpart of the input schema
	 * @param chaser
	 * 		Saturates the newly created configurations
	 * @param detector
	 * 		Detects homomorphisms during chasing
	 * @param costEstimator
	 * 		Estimates the cost of a plan
	 * @param successDominance
	 * 		Removes success dominated configurations
	 * @param filter
	 * 		Filters out configurations at the end of each iteration
	 * @param validators
	 * 		Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions.
	 * @param maxDepth
	 * 		The maximum depth to explore
	 * @param orderAware
	 * @throws PlannerException
	 */
	public DAGGeneric(
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
			Filter filter,
			List<Validator> validators,
			int maxDepth,
			boolean orderAware) throws PlannerException {
		super(eventBus, collectStats, parameters, query, accessibleQuery, schema, accessibleSchema, chaser, detector, costEstimator);
		Preconditions.checkNotNull(successDominance);
		Preconditions.checkArgument(validators != null);
		Preconditions.checkArgument(!validators.isEmpty());
		this.successDominance = successDominance;
		this.filter = filter;
		this.validators = validators;
		this.orderAware = orderAware;
		this.maxDepth = maxDepth;
		List<DAGChaseConfiguration> initialConfigurations = this.createInitialConfigurations();
		if(this.filter != null) {
			Collection<DAGChaseConfiguration> toDelete = this.filter.filter(initialConfigurations);
			initialConfigurations.removeAll(toDelete);
		}
		this.left = new ArrayList<>();
		this.right = new ArrayList<>();
		this.left.addAll(initialConfigurations);
		this.right.addAll(initialConfigurations);
		this.selector = new PairSelector<>(this.left, this.right, this.validators, this.orderAware);
	}

	/**
	 * @throws PlannerException
	 */
	@Override
	protected void _explore() throws PlannerException, LimitReachedException {
		//if the current depth exceeds the threshold return
		if (this.depth > this.maxDepth) {
			this.forcedTermination = true;
			return;
		}
		//Check the ApplyRule configurations for success
		if (this.depth == 1) {
			for (DAGChaseConfiguration configuration:this.right) {
				this.costEstimator.cost(configuration.getPlan());
				if (configuration.isClosed()
						&& (this.bestPlan == null
						|| configuration.getPlan().getCost().lessThan(this.bestPlan.getCost()))
						&& configuration.isSuccessful(this.accessibleQuery)) {
					this.setBestPlan(configuration);
				}
			}
			this.stats.set(CONFIGURATIONS, this.right.size());
			this.stats.set(CANDIDATES, this.right.size());
		} else if (this.depth > 1) {
			//Create all binary configurations of depth up to this.depth
			Collection<DAGChaseConfiguration> last = this.mainLoop();
			//Stop if we cannot create any new configuration
			if (last.isEmpty()) {
				this.forcedTermination = true;
				return;
			}
			//Filter out configurations
			if (this.filter != null) {
				Collection<DAGChaseConfiguration> toDelete;
				toDelete = this.filter.filter(CollectionUtils.union(last,this.right));
				this.right.removeAll(toDelete);
				last.removeAll(toDelete);
			}

			this.left.clear();
			this.left.addAll(last);
			this.selector = new PairSelector<>(this.left, this.right, this.validators, this.orderAware);

			this.stats.set(CONFIGURATIONS, this.right.size());
			this.stats.set(CANDIDATES, this.left.size());
		} else {
			throw new IllegalStateException("Search depth cannot be < 1");
		}
		this.depth++;
	}

	/**
	 * 
	 * @return
	 * @throws PlannerException
	 * @throws LimitReachedException
	 */
	protected Collection<DAGChaseConfiguration> mainLoop() throws PlannerException, LimitReachedException {
		Map<Pair<DAGChaseConfiguration, DAGChaseConfiguration>, DAGChaseConfiguration> last = new HashMap<>();
		Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
		//Get the next pair of configurations to combine
		while ((pair = this.selector.getNext(this.depth)) != null) {
			if(!last.containsKey(pair)) {
				//Create a new binary configuration
				BinaryConfiguration configuration = new BinaryConfiguration(
						pair.getLeft(),
						pair.getRight());
				this.costEstimator.cost(configuration.getPlan());
				configuration.reasonUntilTermination(this.chaser, this.accessibleQuery, this.accessibleSchema.getInferredAccessibilityAxioms());
				//If the newly created binary configuration has the potential to lead to the optimal plan
				if (this.bestPlan == null || !this.successDominance.isDominated(configuration.getPlan(), this.bestPlan)) {
					//If it is closed and has a match, update the best configuration
					if (configuration.isClosed() && configuration.isSuccessful(this.accessibleQuery)) {
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


	/**
	 * Returns pairs of configurations to combine
	 *
	 * @author Efthymia Tsamoura
	 */
	protected static class PairSelector<S extends AccessibleChaseState> {
		/** Configurations to consider on the left*/
		private List<DAGChaseConfiguration> left;
		/** Configurations to consider on the right*/
		private List<DAGChaseConfiguration> right;
		/** Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions. */
		private final List<Validator> validators;
		private final boolean orderAware;
		private final Set<Set<Integer>> cache = Sets.newLinkedHashSet();
		private Pair<DAGChaseConfiguration, DAGChaseConfiguration> reverse = null;
		private int i = 0;
		private int j = 0;
		private boolean sendReverse = false;

		/**
		 * 
		 * @param left
		 * 		Configurations to consider on the left
		 * @param right
		 * 		Configurations to consider on the right
		 * @param validators
		 * 		Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions
		 */
		public PairSelector(List<DAGChaseConfiguration> left,
				List<DAGChaseConfiguration> right, List<Validator> validators) {
			this(left, right, validators, true);
		}

		/**
		 * 
		 * @param left
		 * 		Configurations to consider on the left
		 * @param right
		 * 		Configurations to consider on the right
		 * @param validators
		 * 		Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions
		 * @param orderAware
		 */
		public PairSelector(
				List<DAGChaseConfiguration> left,
				List<DAGChaseConfiguration> right,
				List<Validator> validators,
				boolean orderAware) {
			Preconditions.checkNotNull(left);
			Preconditions.checkNotNull(right);
			Preconditions.checkNotNull(validators);
			this.left = left;
			this.right = right;
			this.validators = validators;
			this.orderAware = orderAware;
		}


		/**
		 * @param depth
		 * @return the next pair of configurations of the given combined depth
		 */
		public Pair<DAGChaseConfiguration, DAGChaseConfiguration> getNext(int depth) {
			if(!this.sendReverse) {
				if(this.i >= this.left.size() || this.j >= this.right.size()) {
					return null;
				}
				DAGChaseConfiguration l = null;
				DAGChaseConfiguration r = null;
				boolean validLR = false;
				boolean validRL = false;
				do {
					l = this.left.get(this.i);
					r = this.right.get(this.j);
					if (this.orderAware || !this.cache.contains(this.makeCacheKey(l, r))) {
						validLR = ConfigurationUtility.validate(l, r, this.validators, depth);
						validRL = ConfigurationUtility.validate(r, l, this.validators, depth);
						if (validLR || validRL) {
							break;
						}
					}
					this.forward();
					if (this.i >= this.left.size()) {
						return null;
					}
				} while(this.j < this.right.size());

				if (validLR) {
					this.sendReverse = false;
					if (this.orderAware && validRL) {
						this.reverse = Pair.of(r, l);
						this.sendReverse = true;
					}
				} else if (validRL) {
					this.sendReverse = false;
				}
				if (!this.orderAware) {
					this.cache.add(this.makeCacheKey(l, r));
				}
				this.forward();
				return Pair.of(l, r);
			}
			this.sendReverse = false;
			return this.reverse;
		}

		private void forward() {
			this.j++;
			if (this.j >= this.right.size()) {
				this.i++;
				this.j = 0;
			}
		}

		private Set<Integer> makeCacheKey(DAGChaseConfiguration... configs) {
			Set<Integer> result = new HashSet<>();
			for (DAGChaseConfiguration config: configs) {
				for (ApplyRule applyRule: config.getApplyRules()) {
					result.add(applyRule.getId());
				}
			}
			return result;
		}
	}

	/**
	 * 
	 * @return
	 */
	public List<DAGChaseConfiguration> getRight() {
		return this.right;
	}

}
