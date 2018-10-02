package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.BinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.SynchronizedEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.MapOfPairsOfConfigurationsToTheEquivalentBinaryConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ValidatorFactory;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.DominanceFactory;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominanceFactory;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

/**
 * An explorer for plans using ideas from dynamic programming. It performs
 * parallel chasing and (success-)dominance, equivalence and success checks in
 * parallel
 * 
 * Similar to DAG Optimized, but the code is simplified.
 * 
 * @author Gabor
 *
 */
public class DAGOptimizedNew extends DAGExplorer {

	/**
	 * The maximum depth we can explore. The exploration ends when there does not
	 * exist any configuration with depth < maxDepth
	 */
	protected final int maxDepth;

	/** The current exploration depth. */
	protected int depth;

	/** Filters out configurations at the end of each iteration. */
	private final Filter filter;

	/** Configurations produced during the previous round. */
	private final Queue<DAGChaseConfiguration> leftSideConfigurations;

	/** Classes of structurally equivalent configurations. */
	private final DAGEquivalenceClasses equivalenceClasses;
	private SuccessDominance successDominance = new SuccessDominanceFactory().getInstance();
	private Dominance[] dominance;
	private Validator validator;

	/**
	 * Instantiates a new DAG optimized.
	 * 
	 * @param eventBus
	 * @param parameters
	 * @param query
	 * @param accessibleQuery
	 * @param accessibleSchema
	 * @param chaser
	 * @param connection
	 * @param costEstimator
	 * @param filter
	 *            Filters out configurations at the end of each iteration
	 * @param maxDepth
	 *            The maximum depth to explore
	 * @throws PlannerException
	 * @throws SQLException
	 */
	public DAGOptimizedNew(EventBus eventBus, PlannerParameters parameters, ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery, AccessibleSchema accessibleSchema, Chaser chaser,
			DatabaseManager connection, CostEstimator costEstimator, Filter filter, int maxDepth)
			throws PlannerException, SQLException {
		super(eventBus, parameters, query, accessibleQuery, accessibleSchema, chaser, connection, costEstimator);
		this.filter = filter;
		this.maxDepth = maxDepth;
		List<DAGChaseConfiguration> initialConfigurations = DAGExplorerUtilities.createInitialApplyRuleConfigurations(
				this.parameters, this.query, this.accessibleQuery, this.accessibleSchema, this.chaser, this.connection);
		if (this.filter != null) {
			Collection<DAGChaseConfiguration> toDelete = this.filter.filter(initialConfigurations);
			initialConfigurations.removeAll(toDelete);
		}
		this.leftSideConfigurations = new ConcurrentLinkedQueue<>();
		this.equivalenceClasses = new SynchronizedEquivalenceClasses();
		this.leftSideConfigurations.addAll(initialConfigurations);
		for (DAGChaseConfiguration initialConfiguration : initialConfigurations) {
			this.equivalenceClasses.addEntry(initialConfiguration);
		}
		this.dominance = new DominanceFactory(parameters.getDominanceType()).getInstance();
		this.validator = (Validator) new ValidatorFactory(parameters.getValidatorType(), parameters.getDepthThreshold())
				.getInstance();
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
		if (this.depth > this.maxDepth) {
			this.forcedTermination = true;
			return;
		}
		// Check the ApplyRule configurations for success
		if (this.depth == 1) {
			for (DAGChaseConfiguration configuration : this.leftSideConfigurations) {
				Cost cost = this.costEstimator.cost(configuration.getPlan());
				configuration.setCost(cost);
				if (this.bestPlan == null
						|| (configuration.isClosed() && configuration.getCost().lessThan(this.bestCost))) {
					if (configuration.isClosed() && configuration.isSuccessful(this.accessibleQuery)) {
						this.setBestPlan(configuration);
					}
				}
			}
		} else if (this.depth > 1) {
			this.checkLimitReached();
			Queue<DAGChaseConfiguration> leftCopy = new ConcurrentLinkedQueue<>();
			leftCopy.addAll(this.leftSideConfigurations);
			// Perform chasing with left to right configurations
			Collection<DAGChaseConfiguration> newlyCreatedConfigurations = new ArrayList<>();			
			newlyCreatedConfigurations.addAll(this.createBinaryConfigurations(
					leftCopy, this.equivalenceClasses.getConfigurations(),
					this.accessibleSchema.getInferredAccessibilityAxioms(), this.bestConfiguration,
					this.equivalenceClasses, true,
					Double.valueOf((this.maxElapsedTime - (this.elapsedTime / 1e6))).longValue(),
					TimeUnit.MILLISECONDS));
			
			// Perform chasing with right to left configurations
			Queue<DAGChaseConfiguration> backwardsRightInput = new ConcurrentLinkedQueue<>();
			backwardsRightInput.addAll(this.leftSideConfigurations);
			ConcurrentLinkedQueue<DAGChaseConfiguration> backwardsLeftInput = new ConcurrentLinkedQueue<>(this.equivalenceClasses.getConfigurations());
			
			newlyCreatedConfigurations.addAll(this.createBinaryConfigurations(
					backwardsLeftInput, backwardsRightInput,
					this.accessibleSchema.getInferredAccessibilityAxioms(), this.bestConfiguration,
					this.equivalenceClasses, true,
					Double.valueOf((this.maxElapsedTime - (this.elapsedTime / 1e6))).longValue(),
					TimeUnit.MILLISECONDS));
			
			// Check for new configurations
			if (newlyCreatedConfigurations == null || newlyCreatedConfigurations.isEmpty()) {
				this.forcedTermination = true;
				return;
			}

			this.checkLimitReached();
			// Iterate over all newly created configurations and return the best
			// configuration
			Set<DAGChaseConfiguration> nonDominatedConfigurations = Collections
					.newSetFromMap(new ConcurrentHashMap<DAGChaseConfiguration, Boolean>());

			DAGChaseConfiguration result = null;
			;
			try {
				result = findBestAndUpdateEquivalences(new ConcurrentLinkedQueue<>(newlyCreatedConfigurations),
						bestConfiguration, nonDominatedConfigurations);
			} catch (Exception e) {
				e.printStackTrace();
				handleExceptions(e);
			}
			if (result != null) {
				this.setBestPlan(result);
			}

			if (equivalenceClasses instanceof SynchronizedEquivalenceClasses) {
				((SynchronizedEquivalenceClasses) equivalenceClasses)
						.wakeupSleep(bestConfiguration != null ? bestConfiguration.getCost() : null);
			}

			// Stop if no new configuration is being found
			if (nonDominatedConfigurations.isEmpty()) {
				this.forcedTermination = true;
				return;
			}

			this.leftSideConfigurations.clear();
			this.leftSideConfigurations.addAll(CollectionUtils.intersection(nonDominatedConfigurations,
					this.equivalenceClasses.getConfigurations()));

			// Filter out configurations
			if (this.filter != null) {
				Collection<DAGChaseConfiguration> toDelete = this.filter
						.filter(this.equivalenceClasses.getConfigurations());
				this.equivalenceClasses.removeAll(toDelete);
				this.leftSideConfigurations.removeAll(toDelete);
			}
		}
		this.depth++;
	}

	private Collection<DAGChaseConfiguration> createBinaryConfigurations(
			Queue<DAGChaseConfiguration> leftSideConfigurations,
			Collection<DAGChaseConfiguration> rightSideConfigurations, Dependency[] inferredAccessibilityAxioms,
			DAGChaseConfiguration bestConfiguration, DAGEquivalenceClasses equivalenceClasses2, boolean b,
			long longValue, TimeUnit milliseconds) throws PlannerException {

		// Map of representatives. For each configuration c = BinConfiguration(c_1,c_2)
		// we create a map from the
		// equivalence classes of c and c' to c''. This map helps us reducing the
		// chasing time, i.e.,
		// if c'_1 and c'_2 are structurally equivalent to c_1 and c_2, respectively,
		// and c = BinConfiguration(c_1,c_2) has already been fully chased,
		// then we copy the state of c to the state of c' = BinConfiguration(c'_1,c'_2).
		MapOfPairsOfConfigurationsToTheEquivalentBinaryConfiguration representatives = new MapOfPairsOfConfigurationsToTheEquivalentBinaryConfiguration();
		// The output configurations
		Map<Pair<DAGChaseConfiguration, DAGChaseConfiguration>, DAGChaseConfiguration> output = new HashMap<>();

		try {
			Queue<DAGChaseConfiguration> leftInput = new ConcurrentLinkedQueue<>();
			leftInput.addAll(leftSideConfigurations);
			Collection<DAGChaseConfiguration> rightInput = rightSideConfigurations;
			DAGChaseConfiguration left;
			// Poll the next configuration from the left input
			while ((left = leftSideConfigurations.poll()) != null) {
				Preconditions.checkNotNull(this.equivalenceClasses.getEquivalenceClass(left));
				Preconditions.checkState(!this.equivalenceClasses.getEquivalenceClass(left).isEmpty());
				// If it comes from an equivalence class that is not sleeping
				if (!this.equivalenceClasses.getEquivalenceClass(left).isSleeping()) {
					// Select configuration from the right input to combine with
					Collection<DAGChaseConfiguration> selected = this.selectConfigurationsToCombineOnTheRight(left,
							rightInput, this.equivalenceClasses, this.depth, bestConfiguration);
					for (DAGChaseConfiguration entry : selected) {
						// If the left configuration participates in the creation of at most topk new
						// binary configurations
						if (!output.containsKey(Pair.of(left, entry))) {
							DAGChaseConfiguration configuration = this.createBinaryConfigurationAndReason(left,
									entry, representatives, inferredAccessibilityAxioms);
							// Create a new binary configuration
							output.put(Pair.of(left, entry), configuration);
						}
					}
				}
			}
			return output.values();
		} catch (Throwable e) {
			e.printStackTrace();
			handleExceptions(e);
			return null;
		}
	}

	private Collection<DAGChaseConfiguration> selectConfigurationsToCombineOnTheRight(
			DAGChaseConfiguration left, Collection<DAGChaseConfiguration> right,
			DAGEquivalenceClasses equivalenceClasses, int depth, DAGChaseConfiguration bestConfiguration) {
		Set<DAGChaseConfiguration> selected = Sets.newLinkedHashSet();
		for (DAGChaseConfiguration configuration : right) {
			Preconditions.checkNotNull(equivalenceClasses.getEquivalenceClass(configuration));
			Preconditions.checkState(!equivalenceClasses.getEquivalenceClass(configuration).isEmpty());
			if (!equivalenceClasses.getEquivalenceClass(configuration).isSleeping()
					&& ConfigurationUtility.validate(left, configuration,
							Arrays.asList(new Validator[] { this.validator }), depth)
					&& ConfigurationUtility.getPotential(left, configuration,
							bestConfiguration == null ? null : bestConfiguration.getPlan(),
							bestConfiguration == null ? null : bestConfiguration.getCost(), this.costEstimator,
							this.successDominance))
				selected.add(configuration);
		}
		return selected;
	}

	private DAGChaseConfiguration createBinaryConfigurationAndReason(DAGChaseConfiguration left,
			DAGChaseConfiguration right, MapOfPairsOfConfigurationsToTheEquivalentBinaryConfiguration representatives,
			Dependency[] inferredAccessibilityAxioms) throws SQLException {
		DAGChaseConfiguration configuration = null;
		// A configuration BinConfiguration(c,c'), where c and c' belong to the
		// equivalence classes of
		// the left and right input configuration, respectively.
		DAGChaseConfiguration representative = representatives.getRepresentative(this.equivalenceClasses, left, right);

		if (representative == null) {
			representative = representatives.getRepresentative(this.equivalenceClasses, right, left);
		}

		// If the representative is null, then create a binary configuration
		// from scratch by fully chasing its state
		if (representative == null) {
			configuration = new BinaryConfiguration(left, right);
			this.chaser.reasonUntilTermination(configuration.getState(), inferredAccessibilityAxioms);
			representatives.put(this.equivalenceClasses, left, right, configuration);
		}
		// otherwise, re-use the state of the representative
		else if (representative != null) {
			configuration = new BinaryConfiguration(left, right, representative.getState().clone());
		}
		Cost cost = this.costEstimator.cost(configuration.getPlan());
		configuration.setCost(cost);
		return configuration;
	}

	private DAGChaseConfiguration findBestAndUpdateEquivalences(Queue<DAGChaseConfiguration> input,
			DAGChaseConfiguration bestConfiguration, Set<DAGChaseConfiguration> output) throws Exception {
		DAGChaseConfiguration configuration;
		// Poll the next configuration
		while ((configuration = input.poll()) != null) {
			// This dominance related stuff needs to be checked, unit tested and then added
			// again.
			// If the configuration is not dominated
			DAGChaseConfiguration dominator = this.equivalenceClasses.dominate(this.dominance, configuration);
			if (dominator == null) {
				// Assess its potential
				if (ConfigurationUtility.getPotential(configuration,
						bestConfiguration == null ? null : bestConfiguration.getPlan(),
						bestConfiguration == null ? null : bestConfiguration.getCost(), this.successDominance)) {
					// Find the configurations dominated by the current one and remove them
					Collection<DAGChaseConfiguration> dominated = this.equivalenceClasses.dominatedBy(this.dominance,
							configuration);
					if (!dominated.isEmpty()) {
						output.removeAll(dominated);
						this.equivalenceClasses.removeAll(dominated);
					}
					// Update the best configuration
					if (configuration.isClosed() && configuration.isSuccessful(this.accessibleQuery) == true) {
						if (bestConfiguration != null && bestConfiguration.getCost() == null && configuration != null
								&& configuration.getCost() != null) {
							// in case the old best had no cost the new one is better, no matter what the
							// cost value is.
							bestConfiguration = configuration;
						}
						if (bestConfiguration == null || (bestConfiguration != null && configuration != null
								&& bestConfiguration.getCost().greaterThan(configuration.getCost()))) {
							bestConfiguration = configuration;
						}
					} else {
						this.equivalenceClasses.addEntry(configuration);
						output.add(configuration);
					}
				}
			} else { 
				// when dominator is present do nothing.
			}
		}
		return bestConfiguration;
	}

	/**
	 * Handle exceptions.
	 *
	 * @param e
	 *            Exception
	 * @throws PlannerException
	 *             the planner exception
	 */
	private static void handleExceptions(Throwable e) throws PlannerException {
		Throwable throwable = e.getCause();
		if (throwable != null) {
			if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			}
			if (throwable instanceof Error) {
				throw (Error) throwable;
			}
		}
		throw new PlannerException(e);
	}

}
