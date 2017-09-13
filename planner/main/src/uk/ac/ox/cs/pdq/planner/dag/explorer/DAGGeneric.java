package uk.ac.ox.cs.pdq.planner.dag.explorer;

import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.CANDIDATES;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.CONFIGURATIONS;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

// TODO: Auto-generated Javadoc
/**
 * Simple dag explorer. It searches the space of binary configurations exhaustively
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class DAGGeneric extends DAGExplorer {

	/**
	 * The maximum depth we can explore. The exploration ends when
	 * there does not exist any configuration with depth < maxDepth
	 */
	protected final int maxDepth;

	/**  Filters out configurations at the end of each iteration. */
	private final Filter filter;
	/** Check whether the binary configuration composed from a given configuration pair satisfies given shape restrictions.*/
	private final List<Validator> validators;

	/** The left. */
	private final List<DAGChaseConfiguration> leftSideConfigurations;

	/** The right. */
	private final List<DAGChaseConfiguration> rightSideConfigurations;

	/**  The current exploration depth. */
	protected int depth = 1;

	/**  Returns pairs of configurations to combine. */
	@SuppressWarnings("rawtypes")
	protected SelectorOfPairsOfConfigurationsToCombine selector;

	/**  Removes success dominated configurations *. */
	protected final SuccessDominance successDominance;
	
	protected List<Entry<RelationalTerm, Cost>> exploredPlans = new ArrayList<>();

	/**
	 * Instantiates a new DAG generic.
	 *
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param parameters the parameters
	 * @param query 		The input user query
	 * @param accessibleQuery 		The accessible counterpart of the user query
	 * @param schema 		The input schema
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 * @param chaser 		Saturates the newly created configurations
	 * @param detector 		Detects homomorphisms during chasing
	 * @param costEstimator 		Estimates the cost of a plan
	 * @param successDominance 		Removes success dominated configurations
	 * @param filter 		Filters out configurations at the end of each iteration
	 * @param validators 		Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions.
	 * @param maxDepth 		The maximum depth to explore
	 * @param orderAware the order aware
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	public DAGGeneric(
			EventBus eventBus, 
			boolean collectStats,
			PlannerParameters parameters,
			ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery,
			//			Schema schema,
			AccessibleSchema accessibleSchema, 
			Chaser chaser,
			DatabaseConnection connection,
			CostEstimator costEstimator,
			SuccessDominance successDominance,
			Filter filter,
			List<Validator> validators,
			int maxDepth) throws PlannerException, SQLException {
		super(eventBus, collectStats, parameters, query, accessibleQuery, accessibleSchema, chaser, connection, costEstimator);
		Preconditions.checkNotNull(successDominance);
		Preconditions.checkArgument(validators != null);
		Preconditions.checkArgument(!validators.isEmpty());
		this.successDominance = successDominance;
		this.filter = filter;
		this.validators = validators;
		this.maxDepth = maxDepth;
		List<DAGChaseConfiguration> initialConfigurations = 
				DAGExplorerUtilities.createInitialApplyRuleConfigurations(this.parameters, this.query, this.accessibleQuery, this.accessibleSchema, this.chaser, this.connection);
		if(this.filter != null) {
			Collection<DAGChaseConfiguration> toDelete = this.filter.filter(initialConfigurations);
			initialConfigurations.removeAll(toDelete);
		}
		this.leftSideConfigurations = new ArrayList<>();
		this.rightSideConfigurations = new ArrayList<>();
		this.leftSideConfigurations.addAll(initialConfigurations);
		this.rightSideConfigurations.addAll(initialConfigurations);
		this.selector = new SelectorOfPairsOfConfigurationsToCombine<>(this.leftSideConfigurations, this.rightSideConfigurations, this.validators);
	}

	/**
	 * _explore.
	 *
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	public void performSingleExplorationStep() throws PlannerException, LimitReachedException {
		//if the current depth exceeds the threshold return
		if (this.depth > this.maxDepth) {
			this.forcedTermination = true;
			return;
		}
		//Check the ApplyRule configurations for success
		if (this.depth == 1) {
			for (DAGChaseConfiguration configuration:this.rightSideConfigurations) {
				Cost cost = this.costEstimator.cost(configuration.getPlan());
				configuration.setCost(cost);
				if (configuration.isClosed()
						&& (this.bestPlan == null
						|| configuration.getCost().lessThan(this.bestCost))
						&& configuration.isSuccessful(this.accessibleQuery)) {
					this.setBestPlan(configuration);
				}
			}
			this.stats.set(CONFIGURATIONS, this.rightSideConfigurations.size());
			this.stats.set(CANDIDATES, this.rightSideConfigurations.size());
		} else if (this.depth > 1) {
			//Create all binary configurations of depth up to this.depth
			Collection<DAGChaseConfiguration> last = this.exploreAllConfigurationsUpToCurrentDepth();
			//Stop if we cannot create any new configuration
			if (last.isEmpty()) {
				this.forcedTermination = true;
				return;
			}
			//Filter out configurations
			if (this.filter != null) {
				Collection<DAGChaseConfiguration> toDelete;
				toDelete = this.filter.filter(CollectionUtils.union(last,this.rightSideConfigurations));
				this.rightSideConfigurations.removeAll(toDelete);
				last.removeAll(toDelete);
			}

			this.leftSideConfigurations.addAll(last);
			if (this.depth+1 > 3) {
				// in cases when depth is 4 or more we have multiple options to generate a plan with such a depth.
				// for example depth = 4 can be achieved by adding 3+1 or 2+2 or 1+3. 
				// The left side already contains plans with 1 and 2 and 3 depth, we have to make sure the right side also contains combinations up to half-depth.
				for (DAGChaseConfiguration config:leftSideConfigurations) {
					if (config.getHeight() <= (depth+1)/2.0 && !rightSideConfigurations.contains(config)) {
						rightSideConfigurations.add(config);
					}
				}
			}
			this.selector = new SelectorOfPairsOfConfigurationsToCombine<>(this.leftSideConfigurations, this.rightSideConfigurations, this.validators);

			this.stats.set(CONFIGURATIONS, this.rightSideConfigurations.size());
			this.stats.set(CANDIDATES, this.leftSideConfigurations.size());
		} else {
			throw new IllegalStateException("Search depth cannot be < 1");
		}
		this.depth++;
	}

	/**
	 * Main loop.
	 *
	 * @return the collection
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@SuppressWarnings("unchecked")
	public Collection<DAGChaseConfiguration> exploreAllConfigurationsUpToCurrentDepth() throws PlannerException, LimitReachedException {
		Map<Pair<DAGChaseConfiguration, DAGChaseConfiguration>, DAGChaseConfiguration> last = new HashMap<>();
		Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
		//Get the next pair of configurations to combine
		while ((pair = this.selector.getNextPairOfConfigurationsToCompose(this.depth)) != null) {
			if(!last.containsKey(pair)) {
				//Create a new binary configuration
				BinaryConfiguration configuration = new BinaryConfiguration(pair.getLeft(), pair.getRight());
				Cost cost = this.costEstimator.cost(configuration.getPlan());
				configuration.setCost(cost);
				configuration.reasonUntilTermination(this.chaser, this.accessibleQuery, this.accessibleSchema.getInferredAccessibilityAxioms());
				
				//If the newly created binary configuration has the potential to lead to the optimal plan
				if (this.bestPlan == null || !this.successDominance.isDominated(configuration.getPlan(), configuration.getCost(), this.bestPlan, this.bestCost)) {
					//If it is closed and has a match, update the best configuration
					if (configuration.isClosed() && configuration.isSuccessful(this.accessibleQuery)) {
						this.exploredPlans.add(new AbstractMap.SimpleEntry<RelationalTerm, Cost>(configuration.getPlan(), configuration.getCost()));
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

	public List<DAGChaseConfiguration> getRight() {
		return this.rightSideConfigurations;
	}
	
	public List<Entry<RelationalTerm, Cost>> getExploredPlans() {
		return this.exploredPlans;
	}
}
