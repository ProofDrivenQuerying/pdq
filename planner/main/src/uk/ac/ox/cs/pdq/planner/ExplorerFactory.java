package uk.ac.ox.cs.pdq.planner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGOptimized;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.ExistenceFilter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.FilterFactory;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.IterativeExecutor;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.IterativeExecutorFactory;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ExistenceValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.LinearValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ValidatorFactory;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.dominance.DominanceFactory;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominanceFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearKChase;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearOptimized;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.pruning.PostPruning;
import uk.ac.ox.cs.pdq.planner.linear.explorer.pruning.PostPruningFactory;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;

import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Creates an explorer given the input arguments. The following types of explorers are available:
	
	-The LinearGeneric explores the space of linear proofs exhaustively. 
	-The LinearOptimized employs several heuristics to cut down the search space. 
	The first heuristic prunes the configurations that map to plans with cost >= to the best plan found so far.
	The second heuristic prunes the cost dominated configurations.
	A configuration c and c' is fact dominated by another configuration c' 
	if there exists an homomorphism from the facts of c to the facts of c' and the input constants are preserved.
	A configuration c is cost dominated by c' if it is fact dominated by c and maps to a plan with cost >= the cost of the plan of c'.
	The LinearOptimized class also employs the notion of equivalence in order not to revisit configurations already visited before.
	Both the LinearGeneric and LinearOptimized perform reasoning every time a new node is added to the plan tree. 
	-The LinearKChase class works similarly to the LinearOptimized class.
	However, it does not perform reasoning every time a new node is added to the plan tree but every k steps.  

	-The DAGGeneric explores the space of proofs exhaustively.
	-The DAGOptimized, DAGSimpleDP and DAGChaseFriendlyDP employ two DP-like heuristics to cut down the search space.
	The first heuristic prunes the configurations that map to plans with cost >= to the best plan found so far.
	The second heuristic prunes the cost dominated configurations. A configuration c and c' is fact dominated by another configuration c' 
	if there exists an homomorphism from the facts of c to the facts of c' and the input constants are preserved.
	A configuration c is cost dominated by c' if it is fact dominated by c and maps to a plan with cost >= the cost of the plan of c'.
	-The DAGOptimized employs further techniques to speed up the planning process like reasoning in parallel and re-use of reasoning results.
 * 
 * @author Efthymia Tsamoura
 *
 */
public class ExplorerFactory {

	/**
	 * Creates a new Explorer object.
	 *
	 * @param <P> the generic type
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param schema the schema
	 * @param accessibleSchema the accessible schema
	 * @param query the query
	 * @param accessibleQuery the accessible query
	 * @param chaser the chaser
	 * @param detector the detector
	 * @param costEstimator the cost estimator
	 * @param parameters the parameters
	 * @return the explorer< p>
	 * @throws Exception the exception
	 */
	public static <P extends Plan> Explorer<P> createExplorer(
			EventBus eventBus, 
			boolean collectStats,
			Schema schema,
			AccessibleSchema accessibleSchema,
			ConjunctiveQuery query,
			ConjunctiveQuery accessibleQuery,
			Chaser chaser,
			DatabaseChaseInstance detector,
			CostEstimator<P> costEstimator,
			PlannerParameters parameters) throws Exception {

		Dominance[] dominance = new DominanceFactory(parameters.getDominanceType(), (CostEstimator<Plan>) costEstimator).getInstance();
		SuccessDominance successDominance = new SuccessDominanceFactory<>(costEstimator, parameters.getSuccessDominanceType()).getInstance();
		
		NodeFactory nodeFactory = null;
		PostPruning postPruning = null;
		IterativeExecutor executor0 = null;
		IterativeExecutor executor1 = null;
		List<Validator> validators = new ArrayList<>();
		Filter filter = null;

		if (parameters.getPlannerType().equals(PlannerTypes.LINEAR_GENERIC)
				|| parameters.getPlannerType().equals(PlannerTypes.LINEAR_KCHASE)
				|| parameters.getPlannerType().equals(PlannerTypes.LINEAR_OPTIMIZED)) {
			nodeFactory = new NodeFactory(parameters, (CostEstimator<LeftDeepPlan>) costEstimator);
			postPruning = new PostPruningFactory(parameters.getPostPruningType(), nodeFactory, chaser, query, accessibleSchema).getInstance();
		}
		else {
			Validator validator = (Validator) new ValidatorFactory(parameters.getValidatorType(), parameters.getDepthThreshold()).getInstance();
			if(parameters.getAccessFile() != null) {
				List<Pair<Relation, AccessMethod>> accesses = readAccesses(schema, parameters.getAccessFile());
				filter = new ExistenceFilter<>(accesses);
				validators.add((Validator) new ExistenceValidator(accesses));
				if(validator instanceof LinearValidator || validator instanceof DefaultValidator) {
					validators.add(validator);
				}
			}
			else {
				validators.add(validator);
				filter = (Filter) new FilterFactory(parameters.getFilterType()).getInstance();
			}

			executor0 = IterativeExecutorFactory.createIterativeExecutor(
					parameters.getIterativeExecutorType(),
					parameters.getFirstPhaseThreads(),
					chaser,
					detector,
					(CostEstimator<DAGPlan>) costEstimator,
					successDominance,
					dominance,
					validators);

			executor1 = IterativeExecutorFactory.createIterativeExecutor(
					parameters.getIterativeExecutorType(),
					parameters.getSecondPhaseThreads(),
					chaser,
					detector,
					(CostEstimator<DAGPlan>) costEstimator,
					successDominance,
					dominance,
					validators);
		}

		switch(parameters.getPlannerType()) {
		case LINEAR_GENERIC:
			return (Explorer<P>) new LinearGeneric(
					eventBus, 
					collectStats,
					query, 
					accessibleQuery,
					schema,
					accessibleSchema, 
					chaser, 
					detector, 
					(CostEstimator<LeftDeepPlan>) costEstimator,
					nodeFactory,
					parameters.getMaxDepth());
		case LINEAR_KCHASE:
			return (Explorer<P>) new LinearKChase(
					eventBus, 
					collectStats,
					query, 
					accessibleQuery,
					schema,
					accessibleSchema, 
					chaser, 
					detector, 
					(CostEstimator<LeftDeepPlan>) costEstimator,
					nodeFactory,
					parameters.getMaxDepth(),
					parameters.getChaseInterval());

		case DAG_GENERIC:
			return (Explorer<P>) new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGGeneric(
					eventBus, collectStats,
					parameters,
					query, 
					accessibleQuery,
					schema,
					accessibleSchema, 
					chaser,
					detector,
					(CostEstimator<DAGPlan>) costEstimator,
					successDominance,
					filter,
					validators,
					parameters.getMaxDepth(),
					parameters.getOrderAware());

		case DAG_SIMPLEDP:
			return (Explorer<P>) new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGSimpleDP(
					eventBus, collectStats,
					parameters,
					query, 
					accessibleQuery,
					schema,
					accessibleSchema, 
					chaser,
					detector,
					(CostEstimator<DAGPlan>) costEstimator,
					successDominance,
					dominance,
					filter,
					validators,
					parameters.getMaxDepth(),
					parameters.getOrderAware());

		case DAG_CHASEFRIENDLYDP:
			return (Explorer<P>) new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGChaseFriendlyDP(
					eventBus, collectStats,
					parameters,
					query, 
					accessibleQuery,
					schema,
					accessibleSchema, 
					chaser,
					detector,
					(CostEstimator<DAGPlan>) costEstimator,
					successDominance,
					dominance,
					filter,
					validators,
					parameters.getMaxDepth(),
					parameters.getOrderAware());

		case DAG_OPTIMIZED:
			return (Explorer<P>) new DAGOptimized(
					eventBus, collectStats,
					parameters,
					query, 
					accessibleQuery,
					schema,
					accessibleSchema, 
					chaser,
					detector,
					(CostEstimator<DAGPlan>) costEstimator,
					filter,
					executor0, executor1,
					parameters.getMaxDepth());

		case LINEAR_OPTIMIZED:
			return (Explorer<P>) new LinearOptimized(
					eventBus, 
					collectStats,
					query, 
					accessibleQuery,
					schema,
					accessibleSchema, 
					chaser,
					detector,
					(CostEstimator<LeftDeepPlan>) costEstimator,
					nodeFactory,
					parameters.getMaxDepth(),
					parameters.getQueryMatchInterval(),
					postPruning,
					parameters.getZombification());

		default:
			throw new IllegalStateException("Unsupported planner type " + parameters.getPlannerType());
		}
	}

	/**
	 * Read accesses.
	 *
	 * @param schema the schema
	 * @param fileName the file name
	 * @return the list
	 */
	private static List<Pair<Relation, AccessMethod>> readAccesses(Schema schema, String fileName) {
		String line = null;
		try {
			List<Pair<Relation, AccessMethod>> accesses = new ArrayList<>();
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				accesses.add(readAccess(schema, line));
			}
			bufferedReader.close();        
			return accesses;
		}
		catch(FileNotFoundException ex) {      
			ex.printStackTrace(System.out);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
		return null;
	}

	/**
	 * Read access.
	 *
	 * @param schema the schema
	 * @param line the line
	 * @return the pair
	 */
	private static Pair<Relation, AccessMethod> readAccess(Schema schema, String line) {
		String READ_ERSPI = "^(RE:(\\w+)(\\s+)BI:(\\w+))";
		Pattern p = Pattern.compile(READ_ERSPI);
		Matcher m = p.matcher(line);
		if (m.find()) {
			String relation = m.group(2);
			String binding = m.group(4);
			if(schema.contains(relation)) {
				Relation r = schema.getRelation(relation);
				if(r.getAccessMethod(binding) != null) {
					AccessMethod b = r.getAccessMethod(binding);
					return Pair.of(r,b);
				}
				else {
					throw new java.lang.IllegalArgumentException();
				}
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		throw new java.lang.IllegalArgumentException("CANNOT PARSE " + line);
	} 

}
