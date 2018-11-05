package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

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
public class DAGOptimized extends DAGExplorer {

	/**
	 * The maximum depth we can explore. The exploration ends when there does not
	 * exist any configuration with depth < maxDepth
	 */
	protected final int maxDepth;

	/** The current exploration depth. */
	protected int depth;

	/** Filters out configurations at the end of each iteration. */
	protected final Filter filter;

	/** Configurations produced during the previous round. */
	protected final Queue<DAGChaseConfiguration> leftSideConfigurations;

	/** Classes of structurally equivalent configurations. */
	protected final DAGEquivalenceClasses equivalenceClasses;
	protected SuccessDominance successDominance = new SuccessDominanceFactory().getInstance();
	protected Dominance[] dominance;
	protected Validator validator;

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
	public DAGOptimized(EventBus eventBus, PlannerParameters parameters, ConjunctiveQuery query,
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
			
			Collection<DAGChaseConfiguration> newlyCreatedConfigurations = new ArrayList<>();	
			// Creating configurations and chasing when necessary.
			newlyCreatedConfigurations.addAll(this.selectAndCreateBinaryConfigurationsToCreateAndReason(
					leftCopy, this.equivalenceClasses.getConfigurations(),
					this.accessibleSchema.getInferredAccessibilityAxioms(), this.bestConfiguration,
					this.equivalenceClasses));
			
			// creating configurations right to left.
			newlyCreatedConfigurations.addAll(this.selectAndCreateBinaryConfigurationsToCreateAndReason(
					new ConcurrentLinkedQueue<>(this.equivalenceClasses.getConfigurations()), new ConcurrentLinkedQueue<>(this.leftSideConfigurations),
					this.accessibleSchema.getInferredAccessibilityAxioms(), this.bestConfiguration,
					this.equivalenceClasses));
			
			// Check for new configurations
			if (newlyCreatedConfigurations == null || newlyCreatedConfigurations.isEmpty()) {
				this.forcedTermination = true;
				return;
			}

			this.checkLimitReached();
			// Iterate over all newly created configurations and return the best
			// configuration
			Set<DAGChaseConfiguration> nonDominatedConfigurations = null;
			try {
				//nonDominatedConfigurations will contain all new configs that had no equivalence classes before and are not dominated by anything. 
				nonDominatedConfigurations = findBestAndUpdateEquivalences(new ConcurrentLinkedQueue<>(newlyCreatedConfigurations), bestConfiguration);
			} catch (Exception e) {
				e.printStackTrace();
				handleExceptions(e);
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

	protected Collection<DAGChaseConfiguration> selectAndCreateBinaryConfigurationsToCreateAndReason(
			Queue<DAGChaseConfiguration> leftSideConfigurations,
			Collection<DAGChaseConfiguration> rightSideConfigurations, Dependency[] inferredAccessibilityAxioms,
			DAGChaseConfiguration bestConfiguration, DAGEquivalenceClasses equivalenceClasses2) throws PlannerException {

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
				this.checkLimitReached();
				Preconditions.checkNotNull(this.equivalenceClasses.getEquivalenceClass(left));
				Preconditions.checkState(!this.equivalenceClasses.getEquivalenceClass(left).isEmpty());
				// Select configuration from the right input to combine with
				Collection<DAGChaseConfiguration>  selected = Sets.newLinkedHashSet();
				for (DAGChaseConfiguration configuration : rightInput) {
					Preconditions.checkNotNull(equivalenceClasses.getEquivalenceClass(configuration));
					Preconditions.checkState(!equivalenceClasses.getEquivalenceClass(configuration).isEmpty());
					if (ConfigurationUtility.validate(left, configuration,
									Arrays.asList(new Validator[] { this.validator }), depth)
							&& ConfigurationUtility.getPotential(left, configuration,
									bestConfiguration == null ? null : bestConfiguration.getPlan(),
									bestConfiguration == null ? null : bestConfiguration.getCost(), this.costEstimator,
									this.successDominance))
						selected.add(configuration);
				}
				
				for (DAGChaseConfiguration entry : selected) {
					// If the new configuration is not already in the output
					if (!output.containsKey(Pair.of(left, entry))) {
						DAGChaseConfiguration configuration = null;
						// A configuration BinConfiguration(c,c'), where c and c' belong to the
						// equivalence classes of
						// the left and right input configuration, respectively.
						DAGChaseConfiguration representative = representatives.getRepresentative(this.equivalenceClasses, left, entry);
						if (representative == null) {
							representative = representatives.getRepresentative(this.equivalenceClasses, entry, left);
						}
						// If the representative of composition is null, then create a binary configuration
						// from scratch by fully chasing its state
						if (representative == null) {
							configuration = new BinaryConfiguration(left, entry);
							this.chaser.reasonUntilTermination(configuration.getState(), inferredAccessibilityAxioms);
							representatives.put(this.equivalenceClasses, left, entry, configuration);
						}
						// otherwise, re-use the state of the representative
						else if (representative != null) {
							configuration = new BinaryConfiguration(left, entry, representative.getState().clone());
						}
						Cost cost = this.costEstimator.cost(configuration.getPlan());
						configuration.setCost(cost);							
						
						// Create a new binary configuration
						output.put(Pair.of(left, entry), configuration);
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

	/**
	 * Loops through the new input configs and updates the best configuration if
	 * there is a better one, and updates the equavalence classes. Returns the new
	 * configurations that did not exists in the equavalence classes before
	 * 
	 * @param input
	 *            - new configurations to update the equavalence classes with.
	 * @param bestConfiguration
	 *            - previous best config
	 * @return the new configurations that did not exists in the equavalence classes before and are not dominated by other classes.
	 * @throws Exception
	 */
	protected Set<DAGChaseConfiguration> findBestAndUpdateEquivalences(Queue<DAGChaseConfiguration> input,
			DAGChaseConfiguration bestConfiguration) throws Exception {
		
		Set<DAGChaseConfiguration> output = new HashSet<DAGChaseConfiguration>();
		DAGChaseConfiguration configuration;
		// Poll the next configuration
		while ((configuration = input.poll()) != null) {
			this.checkLimitReached();

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
		if (bestConfiguration != null) {
			this.setBestPlan(bestConfiguration);
		}
		return output;
	}

	/**
	 * Handle exceptions.
	 *
	 * @param e
	 *            Exception
	 * @throws PlannerException
	 *             the planner exception
	 */
	protected static void handleExceptions(Throwable e) throws PlannerException {
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
