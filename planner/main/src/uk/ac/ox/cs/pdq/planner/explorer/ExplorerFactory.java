package uk.ac.ox.cs.pdq.planner.explorer;

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
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.LinearPlan;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
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
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearKChase;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearOptimized;
import uk.ac.ox.cs.pdq.planner.linear.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.pruning.PostPruning;
import uk.ac.ox.cs.pdq.planner.linear.pruning.PostPruningFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.DominanceFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominanceFactory;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.eventbus.EventBus;

/**
 * Creates an explorer given the input arguments
 * @author Efthymia Tsamoura
 *
 */
public class ExplorerFactory {

	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param schema
	 * @param accessibleSchema
	 * @param query
	 * @param chaser
	 * @param detector
	 * @param costEstimator
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public static <P extends Plan> Explorer<P> createExplorer(
			EventBus eventBus, 
			boolean collectStats,
			Schema schema,
			AccessibleSchema accessibleSchema,
			Query<?> query,
			Query<?> accessibleQuery,
			Chaser chaser,
			HomomorphismDetector detector,
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
			nodeFactory = new NodeFactory(parameters, (CostEstimator<LinearPlan>) costEstimator);
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
					(CostEstimator<LinearPlan>) costEstimator,
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
					(CostEstimator<LinearPlan>) costEstimator,
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
					(CostEstimator<LinearPlan>) costEstimator,
					nodeFactory,
					parameters.getMaxDepth(),
					parameters.getQueryMatchInterval(),
					postPruning,
					parameters.getZombification());

		default:
			throw new IllegalStateException("Unsupported planner type " + parameters.getPlannerType());
		}
	}

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
