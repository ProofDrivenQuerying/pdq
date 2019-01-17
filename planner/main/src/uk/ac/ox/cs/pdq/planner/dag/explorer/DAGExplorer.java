package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.Explorer;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleQuery;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.plancreation.PlanCreationUtility;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

/**
 * Explores the space of DAG proofs.
 * Exploration proceeds roughly as follows.
 * First, create all unary configurations. Unary configuration correspond to single access plans.
 * Then in every exploration step, create a new binary configuration by combining two other configurations. 
 * Saturate the new configuration using the constraints of the accessible schema. 
 * Finally, check if the newly configuration matches the accessible query and update the best configuration appropriately.   
 * @author Efthymia Tsamoura
 *
 */
public abstract class DAGExplorer extends Explorer {

	/**  The input user query *. */
	protected final ConjunctiveQuery query;
	
	/**  The accessible counterpart of the user query *. */
	protected final ConjunctiveQuery accessibleQuery;
	
	/**  The accessible counterpart of the input schema *. */
	protected final AccessibleSchema accessibleSchema;

	/**  Runs the chase algorithm *. */
	protected final Chaser chaser;

	/**  Detects homomorphisms during chasing*. */
	DatabaseManager connection;

	/**  Estimates the cost of a plan *. */
	protected final CostEstimator costEstimator;

	/**  The minimum cost configuration. */
	protected DAGChaseConfiguration bestConfiguration = null;

	/** The parameters. */
	protected final PlannerParameters parameters;

	/**
	 * Instantiates a new DAG explorer.
	 *
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param parameters the parameters
	 * @param query 		The input user query
	 * @param accessibleQuery 		The accessible counterpart of the user query
	 * @param schema 		The input schema
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 * @param chaser 		Saturates configurations using the chase algorithm
	 * @param detector 		Detects homomorphisms during chasing
	 * @param costEstimator 		Estimates the cost of a plan
	 * @param reasoningParameters 
	 */
	public DAGExplorer(EventBus eventBus, 
			PlannerParameters parameters,
			ConjunctiveQuery query, 
			AccessibleSchema accessibleSchema, 
			Chaser chaser, 
			DatabaseManager connection,
			CostEstimator costEstimator) {
		super(eventBus);
		Preconditions.checkArgument(parameters != null);
		Preconditions.checkArgument(query != null);
		Preconditions.checkArgument(accessibleSchema != null);
		Preconditions.checkArgument(chaser != null);
		Preconditions.checkArgument(connection != null);
		Preconditions.checkArgument(costEstimator != null);
		
		this.parameters = parameters;
		this.query = query;
		this.accessibleQuery = generateAccessibleQuery(query);
		this.accessibleSchema = accessibleSchema;
		this.chaser = chaser;
		this.connection = connection;
		this.costEstimator = costEstimator;
		
				
	}
	
	private static ConjunctiveQuery generateAccessibleQuery(ConjunctiveQuery query) {
		Map<Variable, Constant> canonicalMapping = AccessibleQuery.generateCanonicalMappingForQuery(query);
		Map<Variable, Constant> substitutionFiltered = new HashMap<>();
		substitutionFiltered.putAll(canonicalMapping);
		for (Variable variable : query.getBoundVariables())
			substitutionFiltered.remove(variable);
		return new AccessibleQuery(query);
	}

	/**
	 * Updates the minimum cost configuration/plan.
	 *
	 * @param configuration the configuration
	 * @return true if the best configuration/plan is updated
	 */
	public synchronized boolean setBestPlan(DAGChaseConfiguration configuration) {
		if(this.bestConfiguration != null && configuration != null &&
				this.bestConfiguration.getCost().lessOrEquals(configuration.getCost())) {
			return false;
		}
		this.bestConfiguration = configuration;
		//Add the final projection to the best plan
		this.bestPlan = PlanCreationUtility.createFinalProjection(this.accessibleQuery, this.bestConfiguration.getPlan(),this.connection.getSchema());
		this.bestCost = configuration.getCost();
		this.eventBus.post(this);
		this.eventBus.post(this.getBestPlan());
		log.trace("\t+ BEST CONFIGURATION	" + configuration + "\t" + configuration.getCost());
		return true;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.Explorer#getBestPlan()
	 */
	@Override
	public RelationalTerm getBestPlan() {
		if (this.bestConfiguration == null) 
			return null;
		return this.bestPlan;
	}

	/**
	 * Gets the best configuration.
	 *
	 * @return the best configuration
	 */
	public DAGChaseConfiguration getBestConfiguration() {
		return this.bestConfiguration;
	}

	/**
	 * Terminates.
	 *
	 * @return true if the planner terminates
	 */
	@Override
	protected boolean terminates() {
		return false;
	}
}
