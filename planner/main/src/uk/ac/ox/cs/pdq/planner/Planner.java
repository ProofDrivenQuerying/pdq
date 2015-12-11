package uk.ac.ox.cs.pdq.planner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.CostStatKeys;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.logging.performance.ChainedStatistics;
import uk.ac.ox.cs.pdq.logging.performance.DynamicStatistics;
import uk.ac.ox.cs.pdq.logging.performance.StatKey;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.explorer.ConfigurationFactory;
import uk.ac.ox.cs.pdq.planner.explorer.CostEstimatorFactory;
import uk.ac.ox.cs.pdq.planner.explorer.Explorer;
import uk.ac.ox.cs.pdq.planner.explorer.ExplorerFactory;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.logging.performance.ConstantsStatistics;
import uk.ac.ox.cs.pdq.planner.logging.performance.EventDrivenExplorerStatistics;
import uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys;
import uk.ac.ox.cs.pdq.planner.reasoning.ReasonerFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.ExtendedBagFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.DominanceFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominanceFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.DatabaseTreeState;
import uk.ac.ox.cs.pdq.reasoning.HomomorphismException;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.ReasoningTypes;
import uk.ac.ox.cs.pdq.reasoning.chase.BagsTree;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

import com.google.common.eventbus.EventBus;

/**
 * Provides high level functions for finding an optimal plan for a query with
 * respect to a schema, dependencies and access restrictions.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * @author George Konstantinidis
 *
 */
public class Planner {

	protected static Logger log = Logger.getLogger(Planner.class);

	/** Input parameters */
	private PlannerParameters plannerParams;
	
	/** Input parameters */
	private CostParameters costParams;
	
	private ReasoningParameters reasoningParams;

	/** Event bus */
	private EventBus eventBus = new EventBus();

	/** Statistics collector */
	private ChainedStatistics statsLogger;

	/** The schema */
	private Schema schema;


	private CostEstimator<?> externalCostEstimator = null;

	private AccessibleSchema accessibleSchema;
	private HomomorphismManager detector;
	
	/**
	 * 
	 * @param planParams
	 * @param costParams
	 * @param schema
	 * @param query
	 */
	public Planner(PlannerParameters planParams, CostParameters costParams, ReasoningParameters reasoningParams, Schema schema) {
		this(planParams, costParams, reasoningParams, schema, null);
	}

	/**
	 * 
	 * @param params
	 * @param costParams
	 * @param schema
	 * @param query
	 * @param statsLogger
	 */
	public Planner(PlannerParameters params, CostParameters costParams, ReasoningParameters reasoningParams, Schema schema, ChainedStatistics statsLogger) {
		checkParametersConsistency(params, costParams, reasoningParams);
		this.plannerParams = params;
		this.costParams = costParams;
		this.reasoningParams = reasoningParams;
		this.schema = schema;
		this.statsLogger = statsLogger;
		this.schema = schema;
		
		accessibleSchema = new AccessibleSchema(schema);
		try {
			this.detector = new HomomorphismManagerFactory().getInstance(accessibleSchema, this.reasoningParams);
		} catch (HomomorphismException e) {
			//TODO what to do here?
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param params PlannerParameters
	 * @return boolean
	 */
	private static void checkParametersConsistency(PlannerParameters params, CostParameters costParams, ReasoningParameters reasoningParams) {
		new PlannerConsistencyChecker().check(params, costParams, reasoningParams);
	}

	/**
	 *
	 * @param handler EventHandler
	 */
	public void registerEventHandler(EventHandler handler) {
		this.eventBus.register(handler);
	}

	/**
	 * Register the given event homoChecker
	 *
	 * @param handler EventHandler
	 */
	public void unregisterEventHandler(EventHandler handler) {
		this.eventBus.unregister(handler);
	}

	/**
	 * @param estimator CostEstimator<?>
	 */
	public void setCostEstimator(CostEstimator<?> estimator) {
		this.externalCostEstimator = estimator;
	}

	/**
	 * Search a best plan for the given schema and query.
	 *
	 * @return a pair whose first element is the best plan found if any, null
	 *         otherwise, and the second is a mapping from the variables of the
	 *         input query to the constant generated in the initial grounded
	 *         operation.
	 * @throws PlannerException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 * @throws ProofEvent
	 */
	public <P extends Plan> P search(Query<?> query) throws PlannerException {
		return this.search(query,false);
	}
	
	/**
	 * Search a best plan for the given schema and query.
	 *
	 * @param noDep if true, dependencies in the schema are disabled and
	 *         planning occur taking only into account access-based axioms.
	 * @return a pair whose first element is the best plan found if any, null
	 *         otherwise, and the second is a mapping from the variables of the
	 *         input query to the constant generated in the initial grounded
	 *         operation.
	 * @throws PlannerException
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 * @throws ProofEvent
	 */
	public <S extends AccessibleChaseState, P extends Plan> P search(Query<?> query, boolean noDep) throws PlannerException {
		
		boolean collectStats = this.statsLogger != null;
		
		if (noDep) 
		{
			schema = Schema.builder(schema).disableDependencies().build();
			schema.updateConstants(query.getSchemaConstants());
			accessibleSchema = new AccessibleSchema(schema);
		}
		else
		{
			schema.updateConstants(query.getSchemaConstants());
			accessibleSchema.updateConstants(query.getSchemaConstants());
		}

		detector.addQuery(query);
		Query<?> accessibleQuery = accessibleSchema.accessible(query, query.getVariables2Canonical());
		
		Explorer<P> explorer = null;
		try{
			// Top-level initialisations
			CostEstimator<P> costEstimator = (CostEstimator<P>) this.externalCostEstimator;
			if (costEstimator == null) {
				costEstimator = CostEstimatorFactory.getEstimator(this.plannerParams, this.costParams, schema);
			}
			ReasonerFactory reasonerFactory = new ReasonerFactory(
					this.eventBus, 
					collectStats,
					this.reasoningParams);
			Chaser reasoner = reasonerFactory.getInstance();
			if(this.reasoningParams.getReasoningType().equals(ReasoningTypes.BLOCKING_CHASE)) {
				BagsTree.setBagFactory(new ExtendedBagFactory(accessibleSchema, query));
			}
			AccessibleChaseState state = this.reasoningParams.getReasoningType().equals(ReasoningTypes.BLOCKING_CHASE) == true ?
			(uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState) new DatabaseTreeState(query, this.schema, (DBHomomorphismManager) detector) : 
			(uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState) new DatabaseListState(query, this.schema, (DBHomomorphismManager) detector);
			reasoner.reasonUntilTermination(state, accessibleQuery, this.schema.getDependencies());
			detector.clearQuery();
			
			DominanceFactory dominanceFactory = new DominanceFactory(
					this.plannerParams.getDominanceType());

			SuccessDominanceFactory<P> successDominanceFactory =
					new SuccessDominanceFactory<>(costEstimator, this.plannerParams.getSuccessDominanceType());

			ConfigurationFactory<P> configFactory = new ConfigurationFactory<>(
					this.eventBus, collectStats,
					accessibleSchema,
					accessibleQuery, 
					state,
					reasoner,
					costEstimator,
					dominanceFactory,
					successDominanceFactory,
					this.plannerParams);
			
			explorer = ExplorerFactory.createExplorer(
					this.eventBus, 
					collectStats,
					this.schema,
					accessibleSchema,
					query,
					state,
					reasonerFactory,
					detector,
					costEstimator,
					configFactory,
					successDominanceFactory,
					this.plannerParams);

			// Chain all statistics collectors
			if (collectStats) {
				// Explorer statistics
				EventDrivenExplorerStatistics es = new EventDrivenExplorerStatistics();
				this.registerEventHandler(es);
				this.statsLogger.addStatistics(es);

				// Constant statistics
				ConstantsStatistics cs = new ConstantsStatistics();
				this.registerEventHandler(cs);
				this.statsLogger.addStatistics(cs);

				// Append dynamic statistics to logs
				DynamicStatistics ds = new DynamicStatistics(Planner.displayColumns());
				this.registerEventHandler(ds);
				this.statsLogger.addStatistics(ds);
			}

			explorer.setExceptionOnLimit(this.plannerParams.getExceptionOnLimit());
			explorer.setMaxRounds(this.plannerParams.getMaxIterations().doubleValue());
			explorer.setMaxElapsedTime(this.plannerParams.getTimeout());
			explorer.explore();
			return explorer.getBestPlan();
			
		} catch (PlannerException e) {
			this.handleEarlyTermination(explorer);
			throw e;
		} catch (Exception e) {
			this.handleEarlyTermination(explorer);
			e.printStackTrace();
			throw new PlannerException(e);
		} catch (Throwable e) {
			this.handleEarlyTermination(explorer);
			throw e;
		}
	}

	/**
	 * @return StatKeys[]
	 */
	private static StatKey[] displayColumns() {
		return new StatKey[] {
				CostStatKeys.COST_ESTIMATION_COUNT, CostStatKeys.COST_ESTIMATION_TIME,
				PlannerStatKeys.GENERATED_FACTS, PlannerStatKeys.CANDIDATES,
				PlannerStatKeys.CUMULATED_CANDIDATES, PlannerStatKeys.EQUIVALENCE_CLASSES,
				PlannerStatKeys.AVG_EQUIVALENCE_CLASSES, PlannerStatKeys.MED_EQUIVALENCE_CLASSES,
				PlannerStatKeys.FILTERED, PlannerStatKeys.MILLI_REASONING, PlannerStatKeys.MILLI_UPDATE,
				PlannerStatKeys.MILLI_UPDATE_QUERY_DEPENDENCIES,
				PlannerStatKeys.MILLI_BLOCKING_CHECK, PlannerStatKeys.MILLI_SELECT_IC,
				PlannerStatKeys.MILLI_DETECT_CANDIDATES, PlannerStatKeys.MILLI_CLOSE,
				PlannerStatKeys.MILLI_QUERY_MATCH, PlannerStatKeys.MILLI_DOMINANCE,
				PlannerStatKeys.DOMINANCE_PRUNING, PlannerStatKeys.MILLI_EQUIVALENCE,
				PlannerStatKeys.EQUIVALENCE_PRUNING, PlannerStatKeys.HIGHER_COST_PRUNING
		};
	}

	/**
	 * @param ex Explorer<?>
	 */
	private void handleEarlyTermination(Explorer<?> ex) {
		if (ex != null) {
			ex.updateClock();
			if (this.eventBus != null) {
				this.eventBus.post(ex);
			}
		}
	}

	/**
	 * @return the planner's underlying schema
	 */
	public Schema getSchema() {
		return this.schema;
	}

	/**
	 * Performs a search as in search, and returns not only the best plan found
	 * but also the search node in which it is was found. The returned search
	 * node may be used later as parameters to the resumeSearch method.
	 *
	 * @return the best plan found and the search node in which it was found.
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 * @throws SQLException
	 */
	public Pair<Plan, SearchNode> dynamicSearch() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Resumes the search from the given node and return the new best plan
	 * found.
	 *
	 * @param node
	 * @return the best plan found from the given search node, after discarding
	 *         the plan that was previously found at that node, and the search
	 *         node where it was found.
	 */
	public Pair<Plan, SearchNode> resumeSearch(SearchNode node) {
		throw new UnsupportedOperationException();
	}
}
