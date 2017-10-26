package uk.ac.ox.cs.pdq.planner;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.CostEstimatorFactory;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.logging.CostStatKeys;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.ChainedStatistics;
import uk.ac.ox.cs.pdq.logging.DynamicStatistics;
import uk.ac.ox.cs.pdq.logging.StatKey;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.logging.performance.ConstantsStatistics;
import uk.ac.ox.cs.pdq.planner.logging.performance.EventDrivenExplorerStatistics;
import uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.util.PlannerUtility;
import uk.ac.ox.cs.pdq.reasoning.ReasonerFactory;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.EventHandler;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Provides high level functions for finding an optimal plan for a query with
 * respect to a schema, dependencies and access restrictions.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 * @author George Konstantinidis
 *
 */
public class ExplorationSetUp {

	/** The log. */
	protected static Logger log = Logger.getLogger(ExplorationSetUp.class);

	/**  Input parameters. */
	private PlannerParameters plannerParams;
	
	/**  Input parameters. */
	private CostParameters costParams;
	
	/**  */
	private ReasoningParameters reasoningParams;
	
	/**  */
	private DatabaseParameters databaseParams;


	/**   */
	private EventBus eventBus = new EventBus();

	/**  Statistics collector. */
	private ChainedStatistics statsLogger;

	/**   */
	private Schema schema;


	/** The external cost estimator. */
	private CostEstimator externalCostEstimator = null;

	/** The auxiliary schema, including axioms capturing access methods  */
	private AccessibleSchema accessibleSchema;

	
	/**
	 * Instantiates a new exploration set up.
	 *
	 * @param planParams the plan params
	 * @param costParams the cost params
	 * @param reasoningParams the reasoning params
	 * @param schema the schema
	 */
	public ExplorationSetUp(PlannerParameters planParams, CostParameters costParams, ReasoningParameters reasoningParams, DatabaseParameters dbParams, Schema schema) {
		this(planParams, costParams, reasoningParams, dbParams, schema, null);
	}

	/**
	 * Instantiates a new exploration set up.
	 *
	 * @param params the params
	 * @param costParams the cost params
	 * @param reasoningParams the reasoning params
	 * @param schema the schema
	 * @param statsLogger the stats logger
	 */
	public ExplorationSetUp(PlannerParameters params, CostParameters costParams, ReasoningParameters reasoningParams, DatabaseParameters databaseParams, Schema schema, ChainedStatistics statsLogger) {
		this.plannerParams = params;
		this.costParams = costParams;
		this.reasoningParams = reasoningParams;
		this.databaseParams = databaseParams;
		final Attribute Fact = Attribute.create(Integer.class, "InstanceID");
		this.schema = addAdditionalAttributeToSchema(schema, Fact);
		this.statsLogger = statsLogger;
		this.accessibleSchema = new AccessibleSchema(this.schema);
	}

	//add an extra attribute
	private Schema addAdditionalAttributeToSchema(Schema schema, Attribute atribute) {
		Relation[] relations = schema.getRelations();
		for(int index = 0; index < relations.length; ++index) {
			if (relations[index].getAttribute("InstanceID") == null) {
				relations[index] = Relation.appendAttribute(relations[index],atribute);
			}
		}
		List<Dependency> deps = new ArrayList<>();
		deps.addAll(Arrays.asList(schema.getDependencies()));
		deps.addAll(Arrays.asList(schema.getKeyDependencies()));
		return new Schema(relations,deps.toArray(new Dependency[deps.size()]));
	}

	/**
	 * Register event handler.
	 *
	 * @param handler EventHandler
	 */
	public void registerEventHandler(EventHandler handler) {
		this.eventBus.register(handler);
	}

	/**
	 * Register the given event homoChecker.
	 *
	 * @param handler EventHandler
	 */
	public void unregisterEventHandler(EventHandler handler) {
		this.eventBus.unregister(handler);
	}

	/**
	 * Sets the cost estimator.
	 *
	 * @param estimator CostEstimator<?>
	 */
	public void setCostEstimator(CostEstimator estimator) {
		this.externalCostEstimator = estimator;
	}

	/**
	 * Search a best plan for the given schema and query.
	 *
	 * @param <P> the generic type
	 * @param query the query
	 * @return a pair whose first element is the best plan found if any, null
	 *         otherwise, and the second is a mapping from the variables of the
	 *         input query to the constant generated in the initial grounded
	 *         operation.
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	public Entry<RelationalTerm, Cost> search(ConjunctiveQuery query) throws PlannerException, SQLException {
		return this.search(query,false);
	}
	
	/**
	 * Search for a best plan for the given schema and query.
	 *
	 * @param <S> the generic type
	 * @param <P> the generic type
	 * @param query the query
	 * @param noDep if true, dependencies in the schema are disabled and
	 *         planning occur taking only into account access-based axioms.
	 * @return a pair whose first element is the best plan found if any, null
	 *         otherwise, and the second is a mapping from the variables of the
	 *         input query to the constant generated in the initial grounded
	 *         operation.
	 * @throws PlannerException the planner exception
	 * @throws SQLException 
	 */
	public Entry<RelationalTerm, Cost> search(ConjunctiveQuery query, boolean noDep) throws PlannerException, SQLException {
		boolean collectStats = this.statsLogger != null;
		if (noDep) {
			this.schema = new Schema(this.schema.getRelations());
			this.schema.addConstants(Utility.getTypedConstants(query));
			this.accessibleSchema = new AccessibleSchema(this.schema);
		}
		else {
			this.schema.addConstants(Utility.getTypedConstants(query));
			this.accessibleSchema.addConstants(Utility.getTypedConstants(query));
		}
		Map<Variable, Constant> substitution = ConjunctiveQuery.generateSubstitutionToCanonicalVariables(query.getChild(0));
		Map<Variable, Constant> substitutionFiltered = new HashMap<>(); 
		substitutionFiltered.putAll(substitution);
		for(Variable variable:query.getBoundVariables()) 
			substitutionFiltered.remove(variable);
		ChaseConfiguration.getSubstitutions().put(query,substitution);
		ChaseConfiguration.getFilteredSubstitutions().put(query,substitutionFiltered);
		ConjunctiveQuery accessibleQuery = PlannerUtility.createAccessibleQuery(query);
		ChaseConfiguration.getSubstitutions().put(accessibleQuery,substitution);
		ChaseConfiguration.getFilteredSubstitutions().put(accessibleQuery,substitutionFiltered);
		
		Explorer explorer = null;
		DatabaseConnection databaseConnection = new DatabaseConnection(this.databaseParams,this.accessibleSchema);

		try{
			// Top-level initialisations
			CostEstimator costEstimator = this.externalCostEstimator;
			if (costEstimator == null) 
				costEstimator = CostEstimatorFactory.getEstimator(this.costParams, this.schema);

			Chaser reasoner = new ReasonerFactory(this.eventBus, collectStats, this.reasoningParams).getInstance();
			
			explorer = ExplorerFactory.createExplorer(
					this.eventBus, 
					collectStats,
					this.schema,
					this.accessibleSchema,
					query,
					accessibleQuery,
					reasoner,
					databaseConnection,
					costEstimator,
					this.plannerParams,
					this.reasoningParams, 
					this.databaseParams);

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
				DynamicStatistics ds = new DynamicStatistics(ExplorationSetUp.displayColumns());
				this.registerEventHandler(ds);
				this.statsLogger.addStatistics(ds);
			}
			explorer.setExceptionOnLimit(this.plannerParams.getExceptionOnLimit());
			explorer.setMaxRounds(this.plannerParams.getMaxIterations().doubleValue());
			explorer.setMaxElapsedTime(this.plannerParams.getTimeout());
			explorer.explore();
			if(explorer.getBestPlan() != null && explorer.getBestCost() != null)
				return new AbstractMap.SimpleEntry<RelationalTerm, Cost>(explorer.getBestPlan(), explorer.getBestCost());
			else
				return null;
		} catch (PlannerException e) {
			this.handleEarlyTermination(explorer);
			throw e;
		} catch (Exception e) {
			this.handleEarlyTermination(explorer);
			log.error(e.getMessage(),e);
			throw new PlannerException(e);
		} catch (Throwable e) {
			this.handleEarlyTermination(explorer);
			throw e;
		} finally {
			try {
				new DatabaseInstance(databaseConnection).close();
			} catch (Exception e) {
				this.handleEarlyTermination(explorer);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Display columns.
	 *
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
	 * Handle early termination.
	 *
	 * @param ex Explorer<?>
	 */
	private void handleEarlyTermination(Explorer ex) {
		if (ex != null) {
			ex.updateClock();
			if (this.eventBus != null) {
				this.eventBus.post(ex);
			}
		}
	}
}
