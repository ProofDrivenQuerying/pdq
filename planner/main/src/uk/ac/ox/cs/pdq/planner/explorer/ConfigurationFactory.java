package uk.ac.ox.cs.pdq.planner.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.LinearPlan;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.FollowUpHandling;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.LinearConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.DominanceFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominanceFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;


/**
 * Creates configurations based on the input arguments
 *
 * @author Efthymia Tsamoura
 */
public final class ConfigurationFactory<P extends Plan> {

	private final AccessibleSchema accessibleSchema;
	private final Query<?> query;

	/** Performs reasoning*/
	private final Chaser reasoner;

	/** The state of a configuration */
	private final AccessibleChaseState state; 

	/** Factory of dominance detectors*/
	private final DominanceFactory df;

	/** Factory of success dominance detectors*/
	private final SuccessDominanceFactory<P> sdf;

	/** Estimates a plan's cost*/
	private final CostEstimator<P> costEstimator;

	private final EventBus eventBus;
	private final boolean collectStats;	
	private final Random random;

	/** Defines whether or not the ApplyRule configurations will be minimal or not*/
	private final FollowUpHandling followUps;
	private final List<DAGChaseConfiguration> dagConfigurations;

	/**
	 * @param eventBus
	 * @param collectStats
	 * @param schema
	 * @param accessibleSchema
	 * @param state
	 * 		The state of a configuration
	 * @param reasoner
	 * 		Performs reasoning
	 * @param costEstimator
	 * 		Estimates a plan's cost
	 * @param dominanceFactory
	 * 		Factory of dominance detectors
	 * @param successDominanceFactory
	 * 		Factory of success dominance detectors
	 * @param params
	 * @throws PlannerException
	 */
	public ConfigurationFactory(
			EventBus eventBus,
			boolean collectStats,
			AccessibleSchema accessibleSchema,
			Query<?> query,
			AccessibleChaseState state,
			Chaser reasoner,
			CostEstimator<P> costEstimator,
			DominanceFactory dominanceFactory,
			SuccessDominanceFactory<P> successDominanceFactory,
			PlannerParameters params) throws PlannerException {
		this(eventBus, collectStats,
				params.getPlannerType(),
				accessibleSchema, 
				query,
				state,
				reasoner,
				costEstimator,
				dominanceFactory,
				successDominanceFactory,
				new Random(params.getSeed()),
				params.getFollowUpHandling(),
				params.getPlannerType());
	}

	/**
	 *
	 * @param eventBus
	 * @param collectStats
	 * @param type
	 * @param schema
	 * @param accessibleSchema
	 * @param state
	 * 		The state of a configuration
	 * @param reasoner
	 * 		Performs reasoning
	 * @param costEstimator
	 * 		Estimates a plan's cost
	 * @param dominanceFactory
	 * 		Factory of dominance detectors
	 * @param successDominanceFactory
	 * 		Factory of success dominance detectors
	 * @param random
	 * 		Random generator
	 * @param cf
	 * 		Defines a plan's control flow
	 * @param fu
	 * 		Defines whether or not the ApplyRule configurations will be minimal or not
	 * @param plannerType
	 * 		The planner's type
	 * @throws PlannerException
	 */
	protected ConfigurationFactory(
			EventBus eventBus,
			boolean collectStats,
			PlannerTypes type,
			AccessibleSchema accessibleSchema,
			Query<?> query,
			AccessibleChaseState state,
			Chaser reasoner,
			CostEstimator<P> costEstimator,
			DominanceFactory dominanceFactory,
			SuccessDominanceFactory<P> successDominanceFactory,
			Random random,
			FollowUpHandling fu,
			PlannerTypes plannerType) throws PlannerException {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(accessibleSchema);
		Preconditions.checkNotNull(state);
		Preconditions.checkNotNull(reasoner);
		Preconditions.checkNotNull(costEstimator);
		Preconditions.checkNotNull(random );
		Preconditions.checkNotNull(costEstimator);
		Preconditions.checkNotNull(dominanceFactory);
		Preconditions.checkNotNull(successDominanceFactory);
		Preconditions.checkNotNull(query);
		this.eventBus = eventBus;
		this.collectStats = collectStats;
		this.accessibleSchema = accessibleSchema;
		this.state = state;
		this.reasoner = reasoner;
		this.random = random;
		this.df = dominanceFactory;
		this.sdf = successDominanceFactory;
		this.followUps = fu;
		this.costEstimator = costEstimator;
		this.query = query;
		if(plannerType.equals(PlannerTypes.LINEAR_GENERIC) || 
				plannerType.equals(PlannerTypes.LINEAR_OPTIMIZED) ||
				plannerType.equals(PlannerTypes.LINEAR_KCHASE)) {
			this.dagConfigurations = null;
		}
		else {
			this.dagConfigurations = this.createDAGInstances();
		}
	}

	/**
	 * @return a list of ApplyRule configurations based on the facts derived after chasing the input schema with the canonical database of the query
	 * @throws PlannerException
	 */
	private List<DAGChaseConfiguration> createDAGInstances() throws PlannerException {
		List<DAGChaseConfiguration> collection = new ArrayList();
		Collection<Pair<AccessibilityAxiom,Collection<Predicate>>> pairs = this.state.groupByBinding(this.accessibleSchema.getAccessibilityAxioms());
		for (Pair<AccessibilityAxiom, Collection<Predicate>> pair: pairs) {
			ApplyRule applyRule = null;
			Collection<Collection<Predicate>> bindings = new LinkedHashSet();
			switch (this.followUps) {
			case MINIMAL:
				for (Predicate p: pair.getRight()) {
					bindings.add(Sets.newHashSet(p));
				}
				break;
			default:
				bindings.add(pair.getRight());
				break;
			}
			for (Collection<Predicate> binding: bindings) {
//				AccessibleChaseState newState = null;
//				if (this.state instanceof DatabaseTreeState) {
//					newState = ((DatabaseTreeState) this.state).replicate();
//				} else {
//					newState = this.state.clone();
//				}
				AccessibleChaseState newState = this.state.clone();
				applyRule = new ApplyRule(
						this.accessibleSchema,
						this.query,
						this.reasoner,
						newState,
						this.df.getInstance(),
						this.sdf.getInstance(),
						(CostEstimator<DAGPlan>) this.costEstimator,
						pair.getLeft(),
						Sets.newHashSet(binding)
						);
				applyRule.generate();
				collection.add(applyRule);
			}
		}
		return collection;
	}

	/**
	 * @return List<DAGConfiguration>
	 * @throws PlannerException
	 */
	public List<DAGChaseConfiguration> getDAGInstances() throws PlannerException {
		return this.dagConfigurations;
	}


	/**
	 * @param parent
	 * @param exposedCandidates
	 * @return a linear configuration given the input parent configuration and the exposed set of facts
	 * @throws PlannerException
	 */
	public LinearChaseConfiguration getLinearInstance(LinearConfiguration parent, Set<Candidate> exposedCandidates) throws PlannerException {
		return new LinearChaseConfiguration(
				this.eventBus,
				this.collectStats,
				this.accessibleSchema,
				this.query,
				this.reasoner,
				this.df.getInstance(),
				this.sdf.getInstance(),
				(CostEstimator<LinearPlan>) this.costEstimator,
				(LinearChaseConfiguration) parent,
				exposedCandidates,
				this.random);
	}

	/**
	 * @return a linear configurations based on the facts derived after chasing the input schema with the canonical database of the query
	 * @throws PlannerException
	 */
	public LinearChaseConfiguration getLinearInstance() throws PlannerException {
		return new LinearChaseConfiguration(
				this.eventBus,
				this.collectStats,
				this.accessibleSchema,
				this.query,
				this.reasoner,
				this.state,
				this.df.getInstance(),
				this.sdf.getInstance(),
				(CostEstimator<LinearPlan>) this.costEstimator,
				this.random);
	}

	/**
	 * @return Reasoner
	 */
	public Chaser getReasoner() {
		return this.reasoner;
	}

	public CostEstimator getCostEstimator() {
		return this.costEstimator;
	}
}
