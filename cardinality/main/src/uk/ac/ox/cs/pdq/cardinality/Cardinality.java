/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityEstimatorFactory;
import uk.ac.ox.cs.pdq.cardinality.explorer.Explorer;
import uk.ac.ox.cs.pdq.cardinality.explorer.ExplorerFactory;
import uk.ac.ox.cs.pdq.cardinality.logging.performance.ConstantsStatistics;
import uk.ac.ox.cs.pdq.cardinality.logging.performance.EventDrivenExplorerStatistics;
import uk.ac.ox.cs.pdq.cardinality.logging.performance.PlannerStatKeys;
import uk.ac.ox.cs.pdq.cardinality.reasoning.ReasonerFactory;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.CostStatKeys;
import uk.ac.ox.cs.pdq.cost.statistics.Catalog;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.logging.performance.ChainedStatistics;
import uk.ac.ox.cs.pdq.logging.performance.DynamicStatistics;
import uk.ac.ox.cs.pdq.logging.performance.StatKey;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismException;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Provides high level functions for finding an optimal plan for a query with
 * respect to a schema, dependencies and access restrictions.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 *
 */
public class Cardinality {

	/** The log. */
	protected static Logger log = Logger.getLogger(Cardinality.class);

	/**  Input parameters. */
	private CardinalityParameters plannerParams;
	
	/**  Input parameters. */
	private CostParameters costParams;
	
	/** The reasoning params. */
	private ReasoningParameters reasoningParams;

	/**  Event bus. */
	private EventBus eventBus = new EventBus();

	/**  Statistics collector. */
	private ChainedStatistics statsLogger;

	/**  The schema. */
	private Schema schema;

	
	/** The detector. */
	private HomomorphismManager detector;
	
	/**
	 * Instantiates a new planner.
	 *
	 * @param planParams the plan params
	 * @param costParams the cost params
	 * @param reasoningParams the reasoning params
	 * @param schema the schema
	 * @param query the query
	 */
	public Cardinality(CardinalityParameters planParams, CostParameters costParams, ReasoningParameters reasoningParams, Schema schema, Query<?> query) {
		this(planParams, costParams, reasoningParams, schema, query, null);
	}

	/**
	 * Instantiates a new planner.
	 *
	 * @param params the params
	 * @param costParams the cost params
	 * @param reasoningParams the reasoning params
	 * @param schema the schema
	 * @param query the query
	 * @param statsLogger the stats logger
	 */
	public Cardinality(CardinalityParameters params, CostParameters costParams, ReasoningParameters reasoningParams, Schema schema, Query<?> query, ChainedStatistics statsLogger) {
		checkParametersConsistency(params, costParams, reasoningParams);
		this.plannerParams = params;
		this.costParams = costParams;
		this.reasoningParams = reasoningParams;
		this.schema = schema;
		this.statsLogger = statsLogger;
		
		try {
			this.detector = new HomomorphismManagerFactory().getInstance(schema, this.reasoningParams);
		} catch (HomomorphismException e) {
			// TODO what to throw here?
			throw new RuntimeException(e);
		}
	}

	/**
	 * Check parameters consistency.
	 *
	 * @param params PlannerParameters
	 * @param costParams the cost params
	 * @param reasoningParams the reasoning params
	 * @return boolean
	 */
	private static void checkParametersConsistency(CardinalityParameters params, CostParameters costParams, ReasoningParameters reasoningParams) {
		new CardinalityConsistencyChecker().check(params, costParams, reasoningParams);
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
	 * Search a best plan for the given schema and query.
	 *
	 * @param <P> the generic type
	 * @param query the query
	 * @return a pair whose first element is the best plan found if any, null
	 *         otherwise, and the second is a mapping from the variables of the
	 *         input query to the constant generated in the initial grounded
	 *         operation.
	 * @throws CardinalityException the planner exception
	 */
	public <P extends Plan> P search(ConjunctiveQuery query) throws CardinalityException {
		return this.search(query,false);
	}
	
	/**
	 * Search a best plan for the given schema and query.
	 *
	 * @param <P> the generic type
	 * @param query the query
	 * @param noDep if true, dependencies in the schema are disabled and
	 *         planning occur taking only into account access-based axioms.
	 * @return a pair whose first element is the best plan found if any, null
	 *         otherwise, and the second is a mapping from the variables of the
	 *         input query to the constant generated in the initial grounded
	 *         operation.
	 * @throws CardinalityException the planner exception
	 */
	public <P extends Plan> P search(ConjunctiveQuery query, boolean noDep) throws CardinalityException {
		
		boolean collectStats = this.statsLogger != null;
		
		if (noDep) 
		{
			this.schema = Schema.builder(this.schema).disableDependencies().build();
			this.schema.updateConstants(query.getSchemaConstants());
		}
		else
		{
			this.schema.updateConstants(query.getSchemaConstants());
		}
		this.addKeys(schema);
		
		this.detector.addQuery(query);
	
		Explorer<P> explorer = null;
		try{
			// Top-level initialisations
			Catalog catalog = new SimpleCatalog(schema, this.costParams.getDatabaseCatalog());
			CardinalityEstimator cardinalityEstimator = CardinalityEstimatorFactory.getInstance(this.plannerParams.getCardinalityEstimatorType(), catalog);

			Chaser reasoner = new ReasonerFactory(
					this.eventBus, 
					collectStats,
					this.reasoningParams).getInstance();
			
			explorer = ExplorerFactory.createExplorer(
					this.eventBus, 
					collectStats,
					this.schema,
					query,
					reasoner,
					this.detector,
					cardinalityEstimator,
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
				DynamicStatistics ds = new DynamicStatistics(Cardinality.displayColumns());
				this.registerEventHandler(ds);
				this.statsLogger.addStatistics(ds);
			}

			explorer.setExceptionOnLimit(this.plannerParams.getExceptionOnLimit());
			explorer.setMaxRounds(this.plannerParams.getMaxIterations().doubleValue());
			explorer.setMaxElapsedTime(this.plannerParams.getTimeout());
			explorer.explore();
			this.detector.clearQuery();
			return explorer.getBestPlan();
			
		} catch (CardinalityException e) {
			this.handleEarlyTermination(explorer);
			throw e;
		} catch (Exception e) {
			this.handleEarlyTermination(explorer);
			log.error(e.getMessage(),e);
			throw new CardinalityException(e);
		} catch (Throwable e) {
			this.handleEarlyTermination(explorer);
			throw e;
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
	private void handleEarlyTermination(Explorer<?> ex) {
		if (ex != null) {
			ex.updateClock();
			if (this.eventBus != null) {
				this.eventBus.post(ex);
			}
		}
	}

	/**
	 * Gets the schema.
	 *
	 * @return the planner's underlying schema
	 */
	public Schema getSchema() {
		return this.schema;
	}


	
	/**
	 * Adds the keys.
	 *
	 * @param schema the schema
	 */
	public void addKeys(Schema schema) {
		Relation region = schema.getRelation("region");
		Attribute r_regionkey = region.getAttribute(0);
		region.setKey(Lists.newArrayList(r_regionkey));
		
		Relation nation = schema.getRelation("nation");
		Attribute n_nationkey = nation.getAttribute(0);
		nation.setKey(Lists.newArrayList(n_nationkey));
		
		Relation supplier = schema.getRelation("supplier");
		Attribute s_suppkey = supplier.getAttribute(0);
		supplier.setKey(Lists.newArrayList(s_suppkey));
		
		Relation part = schema.getRelation("part");
		Attribute p_partkey = part.getAttribute(0);
		part.setKey(Lists.newArrayList(p_partkey));
		
		Relation partsupp = schema.getRelation("partsupp");
		Attribute ps_partkey = partsupp.getAttribute(0);
		Attribute ps_suppkey = partsupp.getAttribute(1);
		partsupp.setKey(Lists.newArrayList(ps_partkey, ps_suppkey));
		
		Relation lineitem = schema.getRelation("lineitem");
		Attribute l_orderkey = lineitem.getAttribute(0);
		Attribute l_linenumber = lineitem.getAttribute(3);
		lineitem.setKey(Lists.newArrayList(l_orderkey, l_linenumber));
		
		Relation customer = schema.getRelation("customer");
		Attribute c_custkey = customer.getAttribute(0);
		customer.setKey(Lists.newArrayList(c_custkey));
		schema.consolidateKeys();
		
		Relation orders = schema.getRelation("orders");
		Attribute o_orderkey = orders.getAttribute(0);
		orders.setKey(Lists.newArrayList(o_orderkey));
		schema.consolidateKeys();
	}
	
	
}
