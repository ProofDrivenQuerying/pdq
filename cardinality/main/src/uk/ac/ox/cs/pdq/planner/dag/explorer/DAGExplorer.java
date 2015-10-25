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
import uk.ac.ox.cs.pdq.planner.cardinality.CardinalityEstimator;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.explorer.Explorer;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState;
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

	protected final Schema schema;

	protected final Chaser chaser;

	protected final HomomorphismDetector detector;

	protected final CardinalityEstimator cardinalityEstimator;

	/** The minimum cost configuration */
	protected DAGAnnotatedPlan bestConfiguration = null;

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
	 * @param cardinalityEstimator
	 */
	public DAGExplorer(EventBus eventBus, 
			boolean collectStats, 
			PlannerParameters parameters,
			Query<?> query, 
			Schema accessibleSchema, 
			Chaser chaser, 
			HomomorphismDetector detector,
			CardinalityEstimator cardinalityEstimator) {
		super(eventBus, collectStats);
		Preconditions.checkArgument(parameters != null);
		Preconditions.checkArgument(query != null);
		Preconditions.checkArgument(accessibleSchema != null);
		Preconditions.checkArgument(chaser != null);
		Preconditions.checkArgument(detector != null);
		Preconditions.checkArgument(cardinalityEstimator != null);
		this.parameters = parameters;
		this.query = query;
		this.schema = accessibleSchema;
		this.chaser = chaser;
		this.detector = detector;
		this.cardinalityEstimator = cardinalityEstimator;
	}

	/**
	 * Updates the minimum cost configuration/plan
	 * @param configuration
	 * @return true if the best configuration/plan is updated
	 */
	public boolean setBestPlan(DAGAnnotatedPlan configuration) {
		if(this.bestConfiguration != null && configuration != null &&
				this.bestConfiguration.getPlan().getCost().lessOrEquals(configuration.getPlan().getCost())) {
			return false;
		}
		this.bestConfiguration = configuration;
		//Add the final projection to the best plan
		RelationalOperator project = Operators.createFinalProjection(
				this.query,
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

	public DAGAnnotatedPlan getBestConfiguration() {
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
	protected List<DAGAnnotatedPlan> createInitialConfigurations() throws PlannerException {
		ChaseState state = null;
		state = new DatabaseListState(this.query, (DBHomomorphismManager) this.detector);
		this.chaser.reasonUntilTermination(state, this.query, this.schema.getDependencies());

		List<DAGAnnotatedPlan> collection = new ArrayList<>();
		for(Predicate fact:state.getFacts()) {
			ChaseState newState = new DatabaseListState((DBHomomorphismManager) this.detector, Sets.newHashSet(fact));
			UnaryAnnotatedPlan unary =  new UnaryAnnotatedPlan(newState,fact);
			collection.add(unary);
		}
		return collection;
	}

}
