package uk.ac.ox.cs.pdq.planner;

import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderDependentCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.OrderIndependentCostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGOptimizedMultiThread;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.FilterFactory;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ValidatorFactory;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.DominanceFactory;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominanceFactory;
import uk.ac.ox.cs.pdq.planner.linear.cost.CostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderDependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.cost.OrderIndependentCostPropagator;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearKChase;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearOptimized;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.pruning.PostPruning;
import uk.ac.ox.cs.pdq.planner.linear.explorer.pruning.PostPruningFactory;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

/**
 * Creates an explorer given the input arguments. The following types of
 * explorers are available:
 * 
 * -The LinearGeneric explores the space of linear proofs exhaustively. -The
 * LinearOptimized employs several heuristics to cut down the search space. The
 * first heuristic prunes the configurations that map to plans with cost >= to
 * the best plan found so far. The second heuristic prunes the cost dominated
 * configurations. A configuration c and c' is fact dominated by another
 * configuration c' if there exists an homomorphism from the facts of c to the
 * facts of c' and the input constants are preserved. A configuration c is cost
 * dominated by c' if it is fact dominated by c and maps to a plan with cost >=
 * the cost of the plan of c'. The LinearOptimized class also employs the notion
 * of equivalence in order not to revisit configurations already visited before.
 * Both the LinearGeneric and LinearOptimized perform reasoning every time a new
 * node is added to the plan tree. -The LinearKChase class works similarly to
 * the LinearOptimized class. However, it does not perform reasoning every time
 * a new node is added to the plan tree but every k steps.
 * 
 * -The DAGGeneric explores the space of proofs exhaustively. -The DAGOptimized,
 * DAGSimpleDP and DAGChaseFriendlyDP employ two DP-like heuristics to cut down
 * the search space. The first heuristic prunes the configurations that map to
 * plans with cost >= to the best plan found so far. The second heuristic prunes
 * the cost dominated configurations. A configuration c and c' is fact dominated
 * by another configuration c' if there exists an homomorphism from the facts of
 * c to the facts of c' and the input constants are preserved. A configuration c
 * is cost dominated by c' if it is fact dominated by c and maps to a plan with
 * cost >= the cost of the plan of c'. -The DAGOptimized employs further
 * techniques to speed up the planning process like reasoning in parallel and
 * re-use of reasoning results.
 * 
 * @author Efthymia Tsamoura
 *
 */
public class ExplorerFactory {

	/**
	 * Creates a new Explorer object.
	 *
	 * @param <P>
	 *            the generic type
	 * @param eventBus
	 *            the event bus
	 * @param collectStats
	 *            the collect stats
	 * @param schema
	 *            the schema
	 * @param accessibleSchema
	 *            the accessible schema
	 * @param query
	 *            the query
	 * @param accessibleQuery
	 *            the accessible query
	 * @param chaser
	 *            the chaser
	 * @param detector
	 *            the detector
	 * @param costEstimator
	 *            the cost estimator
	 * @param parameters
	 *            the parameters
	 * @return the explorer< p>
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("rawtypes")
	public static Explorer createExplorer(EventBus eventBus, Schema schema, AccessibleSchema accessibleSchema,
			ConjunctiveQuery query, ConjunctiveQuery accessibleQuery, Chaser chaser, DatabaseManager connection,
			CostEstimator costEstimator, PlannerParameters parameters, ReasoningParameters reasoningParameters)
			throws Exception {

		Dominance[] dominance = new DominanceFactory(parameters.getDominanceType()).getInstance();
		SuccessDominance successDominance = new SuccessDominanceFactory().getInstance();

		NodeFactory nodeFactory = null;
		PostPruning postPruning = null;
		CostPropagator costPropagator = null;
		List<Validator> validators = new ArrayList<>();
		Filter filter = null;

		if (parameters.getPlannerType().equals(PlannerTypes.LINEAR_GENERIC)
				|| parameters.getPlannerType().equals(PlannerTypes.LINEAR_KCHASE)
				|| parameters.getPlannerType().equals(PlannerTypes.LINEAR_OPTIMIZED)) {
			nodeFactory = new NodeFactory(costEstimator);
			postPruning = new PostPruningFactory(parameters.getPostPruningType(), nodeFactory, chaser, query,
					accessibleSchema).getInstance();
			if (costEstimator instanceof OrderDependentCostEstimator)
				costPropagator = new OrderDependentCostPropagator((OrderDependentCostEstimator) costEstimator);
			else if (costEstimator instanceof OrderIndependentCostEstimator)
				costPropagator = new OrderIndependentCostPropagator((OrderIndependentCostEstimator) costEstimator);
			else
				throw new IllegalStateException(
						"Attempting to get a propagator for a cost estimator that is neither blackbox nor simple");
		} else {
			Validator validator = (Validator) new ValidatorFactory(parameters.getValidatorType(),
					parameters.getDepthThreshold()).getInstance();
			validators.add(validator);
			filter = (Filter) new FilterFactory(parameters.getFilterType()).getInstance();
		}

		switch (parameters.getPlannerType()) {
		case LINEAR_GENERIC:
			return new LinearGeneric(eventBus, query, accessibleQuery, accessibleSchema, chaser, connection,
					costEstimator, nodeFactory, parameters.getMaxDepth());
		case LINEAR_KCHASE:
			return new LinearKChase(eventBus, query, accessibleQuery, accessibleSchema, chaser, connection,
					costEstimator, costPropagator, nodeFactory, parameters.getMaxDepth(),
					parameters.getChaseInterval());

		case DAG_GENERIC:
			return new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGGenericLegacy(eventBus, parameters, query, accessibleQuery,
					accessibleSchema, chaser, connection, costEstimator, successDominance, filter, validators,
					parameters.getMaxDepth());
		case DAG_GENERIC_SIMPLE:
			return new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGGenericSimple(eventBus, parameters, query,
					accessibleQuery, accessibleSchema, chaser, connection, costEstimator, successDominance, filter,
					validators, parameters.getMaxDepth());

		case DAG_SIMPLEDP:
			return new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGSimpleDP(eventBus, parameters, query, accessibleQuery,
					accessibleSchema, chaser, connection, costEstimator, successDominance, dominance, filter,
					validators, parameters.getMaxDepth());

		case DAG_CHASEFRIENDLYDP:
			return new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGChaseFriendlyDP(eventBus, parameters, query,
					accessibleQuery, accessibleSchema, chaser, connection, costEstimator, successDominance, dominance,
					filter, validators, parameters.getMaxDepth());

		case DAG_OPTIMIZED:
			return new DAGOptimizedMultiThread(eventBus, parameters, query, accessibleQuery, accessibleSchema, chaser, connection,
					costEstimator, filter, parameters.getMaxDepth());

		case LINEAR_OPTIMIZED:
			return new LinearOptimized(eventBus, query, accessibleQuery, accessibleSchema, chaser, connection,
					costEstimator, costPropagator, nodeFactory, parameters.getMaxDepth(),
					parameters.getQueryMatchInterval(), postPruning, parameters.getZombification());

		default:
			throw new IllegalStateException("Unsupported planner type " + parameters.getPlannerType());
		}
	}

}
