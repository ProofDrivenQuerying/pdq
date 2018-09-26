package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;
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

	/** The left. */
	private List<DAGChaseConfiguration> leftSideConfigurations = new ArrayList<>();;
	private int leftIndex = 0;

	/** The right. */
	private List<DAGChaseConfiguration> rightSideConfigurations = new ArrayList<>();;
	private int rightIndex = 0;

	/** Removes success dominated configurations *. */
	protected final SuccessDominance successDominance;

	/**
	 * List of closed and successful plans.
	 */
	protected List<Entry<RelationalTerm, Cost>> exploredPlans = new ArrayList<>();
	/**
	 * different configurations can result the same plan. We cache the generated
	 * plans to make sure we skip the configuration that results in a known plan.
	 */
	private Set<RelationalTerm> allPlanCache = new HashSet<>();

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
	 * @param connection
	 *            handle to database manager used to store facts during chasing and
	 *            exploration
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
	 * @throws PlannerException
	 *             the planner exception
	 * @throws SQLException
	 */
	public DAGGenericSimple(EventBus eventBus, PlannerParameters parameters, ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery, AccessibleSchema accessibleSchema, Chaser chaser,
			DatabaseManager connection, CostEstimator costEstimator, SuccessDominance successDominance, Filter filter,
			List<Validator> validators, int maxDepth) throws PlannerException, SQLException {
		super(eventBus, parameters, query, accessibleQuery, accessibleSchema, chaser, connection, costEstimator);
		Preconditions.checkNotNull(successDominance);
		Preconditions.checkArgument(validators != null);
		this.successDominance = successDominance;
		List<DAGChaseConfiguration> initialConfigurations = createInitialApplyRuleConfigurations(this.parameters,
				this.query, this.accessibleQuery, this.accessibleSchema, this.chaser, this.connection);
		checkConfigurationsForSuccess(initialConfigurations);
		this.leftSideConfigurations.addAll(initialConfigurations);
		this.rightSideConfigurations.addAll(initialConfigurations);
	}

	/**
	 * Checks every configuration for success and sets them as best plan.
	 * 
	 * @param configurations
	 */
	private void checkConfigurationsForSuccess(List<DAGChaseConfiguration> configurations) {
		// check initial configurations for success
		for (DAGChaseConfiguration configuration : configurations) {
			Cost cost = this.costEstimator.cost(configuration.getPlan());
			configuration.setCost(cost);
			if (configuration.isClosed() && (this.bestPlan == null || configuration.getCost().lessThan(this.bestCost))
					&& configuration.isSuccessful(this.accessibleQuery)) {
				this.setBestPlan(configuration);
			}
		}
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
		List<DAGChaseConfiguration> newLeftSideConfigurations = new ArrayList<>();
		leftIndex = 0;
		rightIndex = 0;
		// Get the next pair of configurations to combine
		while (leftIndex < leftSideConfigurations.size()) {
			DAGChaseConfiguration l = this.leftSideConfigurations.get(this.leftIndex);
			DAGChaseConfiguration r = this.rightSideConfigurations.get(this.rightIndex);
			RelationalTerm leftToRightPlan = PlanCreationUtility.createJoinPlan(l.getPlan(), r.getPlan());
			if (!allPlanCache.contains(leftToRightPlan)) {
				allPlanCache.add(leftToRightPlan);
				newLeftSideConfigurations = reasonAndUpdateLeftSideConfigurations(newLeftSideConfigurations, l, r);
			}
			RelationalTerm rightToLeftPlan = PlanCreationUtility.createJoinPlan(r.getPlan(), l.getPlan());
			if (!allPlanCache.contains(rightToLeftPlan)) {
				allPlanCache.add(rightToLeftPlan);
				newLeftSideConfigurations = reasonAndUpdateLeftSideConfigurations(newLeftSideConfigurations, r, l);
			}
			if (this.checkLimitReached()) {
				this.forcedTermination = true;
				break;
			}
			rightIndex++;
			if (rightIndex >= rightSideConfigurations.size()) {
				rightIndex = 0;
				leftIndex++;
			}
		}
		// Stop if we cannot create any new configuration
		if (newLeftSideConfigurations.isEmpty()) {
			this.forcedTermination = true;
			return;
		}
		leftSideConfigurations = newLeftSideConfigurations;
	}

	// private RelationalTerm expected_plan = null;
	private List<DAGChaseConfiguration> reasonAndUpdateLeftSideConfigurations(List<DAGChaseConfiguration> newLeftSideConfigurations, DAGChaseConfiguration l,
			DAGChaseConfiguration r) {

		BinaryConfiguration configuration = new BinaryConfiguration(l, r);
		if (ConfigurationUtility.isNonTrivial(l, r)) {
			// Create a new binary configuration
			Cost cost = this.costEstimator.cost(configuration.getPlan());
			configuration.setCost(cost);
			if (this.bestPlan == null || !this.successDominance.isDominated(configuration.getPlan(),
					configuration.getCost(), this.bestPlan, this.bestCost)) {
				configuration.reasonUntilTermination(this.chaser, this.accessibleQuery,
						this.accessibleSchema.getInferredAccessibilityAxioms());
				// If it is closed and has a match, update the best configuration
				if (configuration.isClosed() && configuration.isSuccessful(this.accessibleQuery)) {
					this.exploredPlans.add(new AbstractMap.SimpleEntry<RelationalTerm, Cost>(configuration.getPlan(),
							configuration.getCost()));
					this.setBestPlan(configuration);
				}
				newLeftSideConfigurations.add(configuration);
			}
		}
		return newLeftSideConfigurations;
	}

	/** Checks if the left side's output appears as input on the right. 
	 * @param l
	 * @param r
	 * @return
	 */
	protected static boolean containsAny(DAGChaseConfiguration l, DAGChaseConfiguration r) {
		boolean containsAny = false;
		for (Constant rightInput : r.getInput()) {
			if (l.getOutput().contains(rightInput)) {
				containsAny = true;
				break;
			}
		}
		return containsAny;
	}

	/** Used for debugging only.checks if the two given plan has the same child hierarchy. 
	 * @param plan1
	 * @param plan2
	 * @return
	 */
	protected boolean checkPlanSimilarity(RelationalTerm plan1, RelationalTerm plan2) {
		if (!plan1.getClass().equals(plan2.getClass()))
			return false;
		if (plan1.getChildren().length != plan2.getChildren().length)
			return false;
		if (plan1.getChildren().length == 0)
			return true;

		if (plan1.getChildren().length == 1)
			return checkPlanSimilarity(plan1.getChild(0), plan2.getChild(0));
		// 2 children case
		if (!checkPlanSimilarity(plan1.getChild(0), plan2.getChild(0)))
			return false;
		return checkPlanSimilarity(plan1.getChild(1), plan2.getChild(1));
	}

	public List<DAGChaseConfiguration> getRight() {
		return this.rightSideConfigurations;
	}

	public List<Entry<RelationalTerm, Cost>> getExploredPlans() {
		return this.exploredPlans;
	}

	/**
	 * Creates the initial configurations.
	 *
	 * @return a list of ApplyRule configurations based on the facts derived after
	 *         chasing the input schema with the canonical database of the query
	 * @throws SQLException
	 */
	public static List<DAGChaseConfiguration> createInitialApplyRuleConfigurations(PlannerParameters parameters,
			ConjunctiveQuery query, ConjunctiveQuery accessibleQuery, AccessibleSchema accessibleSchema, Chaser chaser,
			DatabaseManager connection) throws SQLException {
		// reason with dependencies
		AccessibleDatabaseChaseInstance state = new AccessibleDatabaseChaseInstance(query, accessibleSchema, connection, false);
		chaser.reasonUntilTermination(state, accessibleSchema.getOriginalDependencies());

		List<DAGChaseConfiguration> collection = new ArrayList<>();
		Collection<Pair<AccessibilityAxiom, Collection<Atom>>> groupsOfFacts = state
				.groupFactsByAccessMethods(accessibleSchema.getAccessibilityAxioms());
		
		// go over the groups of facts and create applyRule configurations for each.
		for (Pair<AccessibilityAxiom, Collection<Atom>> groupOfFacts : groupsOfFacts) {
			ApplyRule applyRule = null;
			Collection<Collection<Atom>> groupForGivenAccessMethod = new LinkedHashSet<>();
			switch (parameters.getFollowUpHandling()) {
			case MINIMAL:
				for (Atom p : groupOfFacts.getRight()) {
					groupForGivenAccessMethod.add(Sets.newHashSet(p));
				}
				break;
			default:
				groupForGivenAccessMethod.add(groupOfFacts.getRight());
				break;
			}
			// creating apply rule configurations
			for (Collection<Atom> atoms : groupForGivenAccessMethod) {
				AccessibleChaseInstance newState = (uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance) new AccessibleDatabaseChaseInstance(
						atoms, connection, false);
				applyRule = new ApplyRule(newState, groupOfFacts.getLeft(), Sets.newHashSet(atoms));
				applyRule.generate(chaser, query, accessibleSchema.getInferredAccessibilityAxioms());
				collection.add(applyRule);
			}
		}
		return collection;
	}

}
