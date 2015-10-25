package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.algebra.Operators;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.explorer.Explorer;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

/**
 * Abstract DAG explorer
 * @author Efthymia Tsamoura
 *
 */
public abstract class DAGExplorer extends Explorer<DAGPlan> {

	protected final Query<?> query;
	
	protected final Query<?> accessibleQuery;

	protected final Schema schema;
	
	protected final AccessibleSchema accessibleSchema;

	protected final Chaser chaser;

	protected final HomomorphismDetector detector;

	protected final CostEstimator<DAGPlan> costEstimator;

	/** The minimum cost configuration */
	protected DAGChaseConfiguration bestConfiguration = null;

	protected final PlannerParameters parameters; 

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
	 */
	public DAGExplorer(EventBus eventBus, 
			boolean collectStats, 
			PlannerParameters parameters,
			Query<?> query, 
			Query<?> accessibleQuery,
			Schema schema,
			AccessibleSchema accessibleSchema, 
			Chaser chaser, 
			HomomorphismDetector detector,
			CostEstimator<DAGPlan> costEstimator) {
		super(eventBus, collectStats);
		Preconditions.checkArgument(parameters != null);
		Preconditions.checkArgument(query != null);
		Preconditions.checkArgument(accessibleQuery != null);
		Preconditions.checkArgument(schema != null);
		Preconditions.checkArgument(accessibleSchema != null);
		Preconditions.checkArgument(chaser != null);
		Preconditions.checkArgument(detector != null);
		Preconditions.checkArgument(costEstimator != null);
		
		this.parameters = parameters;
		this.query = query;
		this.accessibleQuery = accessibleQuery;
		this.schema = schema;
		this.accessibleSchema = accessibleSchema;
		this.chaser = chaser;
		this.detector = detector;
		this.costEstimator = costEstimator;
	}

	/**
	 * Updates the minimum cost configuration/plan
	 * @param configuration
	 * @return true if the best configuration/plan is updated
	 */
	public boolean setBestPlan(DAGChaseConfiguration configuration) {
		if(this.bestConfiguration != null && configuration != null &&
				this.bestConfiguration.getPlan().getCost().lessOrEquals(configuration.getPlan().getCost())) {
			return false;
		}
		this.bestConfiguration = configuration;
		//Add the final projection to the best plan
		RelationalOperator project = Operators.createFinalProjection(
				this.accessibleQuery,
				this.bestConfiguration.getPlan().getOperator());
		this.bestPlan = new DAGPlan(project);
		this.bestPlan.addChild(this.bestConfiguration.getPlan());
		this.bestPlan.setCost(this.bestConfiguration.getPlan().getCost());
		this.eventBus.post(this);
		this.eventBus.post(this.getBestPlan());
		log.trace("\t+ BEST CONFIGURATION	" + configuration + "\t" + configuration.getPlan().getCost());
		return true;
	}

	@Override
	public DAGPlan getBestPlan() {
		if (this.bestConfiguration == null) {
			return null;
		}
		return this.bestPlan;
	}

	public DAGChaseConfiguration getBestConfiguration() {
		return this.bestConfiguration;
	}

	/**
	 * @return true if the planner terminates
	 */
	@Override
	protected boolean terminates() {
		return false;
	}

	/**
	 * @return a list of ApplyRule configurations based on the facts derived after chasing the input schema with the canonical database of the query
	 * @throws PlannerException
	 */
	protected List<DAGChaseConfiguration> createInitialConfigurations() throws PlannerException {
		AccessibleChaseState state = null;
		state = (uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState) 
				new DatabaseListState(this.query, this.schema, (DBHomomorphismManager) this.detector);
		this.chaser.reasonUntilTermination(state, this.query, this.schema.getDependencies());


		List<DAGChaseConfiguration> collection = new ArrayList<>();
		Collection<Pair<AccessibilityAxiom,Collection<Predicate>>> pairs = state.groupByBinding(this.accessibleSchema.getAccessibilityAxioms());
		for (Pair<AccessibilityAxiom, Collection<Predicate>> pair: pairs) {
			ApplyRule applyRule = null;
			Collection<Collection<Predicate>> bindings = new LinkedHashSet<>();
			switch (this.parameters.getFollowUpHandling()) {
			case MINIMAL:
				for (Predicate p: pair.getRight()) {
					bindings.add(Sets.newHashSet(p));
				}
				break;
			default:
				bindings.add(pair.getRight());
				break;
			}
			for (Collection<Predicate> binding:bindings) {
				AccessibleChaseState newState = state.clone();
				applyRule = new ApplyRule(
						newState,
						pair.getLeft(),
						Sets.newHashSet(binding)
						);
				applyRule.generate(this.chaser, this.query, this.accessibleSchema);
				collection.add(applyRule);
			}
		}
		return collection;
	}

}
