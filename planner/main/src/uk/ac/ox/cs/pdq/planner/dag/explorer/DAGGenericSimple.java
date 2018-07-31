package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ApplyRuleValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ClosedValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DepthValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.LinearValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.RightDepthValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * Simple dag explorer. It searches the space of binary configurations
 * exhaustively
 *
 * The main difference from DAGGeneric is that when generating configuration
 * pairs to combine it will not limit the new configurations to a certain depth,
 * therefore it will provide more combinations in each round, however when there
 * is no new configuration we do not need to run the system further.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class DAGGenericSimple extends DAGExplorer {

	/**
	 * Check whether the binary configuration composed from a given configuration
	 * pair satisfies given shape restrictions.
	 */
	private final List<Validator> validators;

	/** The left. */
	private final List<DAGChaseConfiguration> leftSideConfigurations;

	/** The right. */
	private final List<DAGChaseConfiguration> rightSideConfigurations;

	/** Returns pairs of configurations to combine. */
	protected SelectorOfPairsOfConfigurationsToCombine<AccessibleChaseInstance> selector;

	/** Removes success dominated configurations *. */
	protected final SuccessDominance successDominance;

	protected List<Entry<RelationalTerm, Cost>> exploredPlans = new ArrayList<>();

	/**
	 * Instantiates a new DAG generic.
	 *
	 * @param eventBus
	 *            the event bus
	 * @param collectStats
	 *            the collect stats
	 * @param parameters
	 *            the parameters
	 * @param query
	 *            The input user query
	 * @param accessibleQuery
	 *            The accessible counterpart of the user query
	 * @param schema
	 *            The input schema
	 * @param accessibleSchema
	 *            The accessible counterpart of the input schema
	 * @param chaser
	 *            Saturates the newly created configurations
	 * @param detector
	 *            Detects homomorphisms during chasing
	 * @param costEstimator
	 *            Estimates the cost of a plan
	 * @param successDominance
	 *            Removes success dominated configurations
	 * @param filter
	 *            Filters out configurations at the end of each iteration
	 * @param validators
	 *            Checks whether the binary configuration composed from a given
	 *            configuration pair satisfies given shape restrictions.
	 * @param maxDepth
	 *            The maximum depth to explore
	 * @param orderAware
	 *            the order aware
	 * @throws PlannerException
	 *             the planner exception
	 * @throws SQLException
	 */
	public DAGGenericSimple(EventBus eventBus, PlannerParameters parameters, ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery, AccessibleSchema accessibleSchema, Chaser chaser,
			DatabaseManager connection, CostEstimator costEstimator, SuccessDominance successDominance, Filter filter,
			List<Validator> validators, int maxDepth) throws PlannerException, SQLException {
		super(eventBus, parameters, query, accessibleQuery, accessibleSchema, chaser, connection, costEstimator);
		this.validators = validators;
		for (Validator validator : validators) {
			if (validator instanceof LinearValidator)
				((LinearValidator) validator).setIgnoreDepth(true);
			if (validator instanceof DefaultValidator)
				((DefaultValidator) validator).setIgnoreDepth(true);
			if (validator instanceof ApplyRuleValidator)
				((ApplyRuleValidator) validator).setIgnoreDepth(true);

			if (validator instanceof ClosedValidator)
				((ClosedValidator) validator).setIgnoreDepth(true);
			if (validator instanceof DepthValidator)
				((DepthValidator) validator).setIgnoreDepth(true);
			if (validator instanceof RightDepthValidator)
				((RightDepthValidator) validator).setIgnoreDepth(true);
			if (validator instanceof ApplyRuleValidator)
				((ApplyRuleValidator) validator).setIgnoreDepth(true);
			if (validator instanceof ApplyRuleValidator)
				((ApplyRuleValidator) validator).setIgnoreDepth(true);
		}
		Preconditions.checkNotNull(successDominance);
		Preconditions.checkArgument(validators != null);
		// Preconditions.checkArgument(!validators.isEmpty());
		this.successDominance = successDominance;
		List<DAGChaseConfiguration> initialConfigurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(
				this.parameters, this.query, this.accessibleQuery, this.accessibleSchema, this.chaser, this.connection);
		this.leftSideConfigurations = new ArrayList<>();
		this.rightSideConfigurations = new ArrayList<>();
		this.leftSideConfigurations.addAll(initialConfigurations);
		this.rightSideConfigurations.addAll(initialConfigurations);
		this.selector = new SelectorOfPairsOfConfigurationsToCombine<>(this.leftSideConfigurations,
				this.rightSideConfigurations, validators);
	}

	/**
	 * _explore.
	 *
	 * @throws PlannerException
	 *             the planner exception
	 * @throws LimitReachedException
	 *             the limit reached exception
	 */
	@Override
	public void performSingleExplorationStep() throws PlannerException, LimitReachedException {
		boolean changed = false;
		Pair<DAGChaseConfiguration, DAGChaseConfiguration> pair = null;
		// Get the next pair of configurations to combine
		while ((pair = this.selector.getNextPairOfConfigurationsToCompose(rightSideConfigurations.size())) != null) {
			BinaryConfiguration configuration = new BinaryConfiguration(pair.getLeft(), pair.getRight());
			if (!leftSideConfigurations.contains(configuration)) {
				// Create a new binary configuration
				Cost cost = this.costEstimator.cost(configuration.getPlan());
				configuration.setCost(cost);
				configuration.reasonUntilTermination(this.chaser, this.accessibleQuery,
						this.accessibleSchema.getInferredAccessibilityAxioms());
				// If the newly created binary configuration has the potential to lead to the
				// optimal plan
				if (this.bestPlan == null || !this.successDominance.isDominated(configuration.getPlan(),
						configuration.getCost(), this.bestPlan, this.bestCost)) {
					// If it is closed and has a match, update the best configuration
					if (configuration.isClosed() && configuration.isSuccessful(this.accessibleQuery)) {
						this.exploredPlans.add(new AbstractMap.SimpleEntry<RelationalTerm, Cost>(
								configuration.getPlan(), configuration.getCost()));
						this.setBestPlan(configuration);
					}
					leftSideConfigurations.add(configuration);
					// rightSideConfigurations.add(configuration);
					changed = true;
				}
			}
			if (this.checkLimitReached()) {
				this.forcedTermination = true;
				break;
			}
		}
		// Stop if we cannot create any new configuration
		if (!changed) {
			this.forcedTermination = true;
			return;
		}
		this.selector = new SelectorOfPairsOfConfigurationsToCombine<>(this.leftSideConfigurations,
				this.rightSideConfigurations, this.validators);
	}

	public List<DAGChaseConfiguration> getRight() {
		return this.rightSideConfigurations;
	}

	public List<Entry<RelationalTerm, Cost>> getExploredPlans() {
		return this.exploredPlans;
	}
}
