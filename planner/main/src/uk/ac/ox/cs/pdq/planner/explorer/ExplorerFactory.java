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
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ExistenceValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.LinearValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ReachabilityValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ValidatorFactory;
import uk.ac.ox.cs.pdq.planner.dag.potential.DefaultPotentialAssessor;
import uk.ac.ox.cs.pdq.planner.dag.priority.PriorityAssessor;
import uk.ac.ox.cs.pdq.planner.dag.priority.PriorityAssessorFactory;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearGeneric;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearKChase;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearOptimized;
import uk.ac.ox.cs.pdq.planner.linear.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.pruning.PostPruningFactory;
import uk.ac.ox.cs.pdq.planner.parallel.IterativeExecutor;
import uk.ac.ox.cs.pdq.planner.parallel.IterativeExecutorFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominanceFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.ReasonerFactory;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;
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
	 * @param accessibleQuery
	 * @param state
	 * 		The state of a configuration
	 * @param reasonerFactory
	 * 		Creates reasoners
	 * @param detector
	 * 		Detects homomorphisms
	 * @param costEstimator
	 * 		Estimates a plan's cost
	 * @param configurationFactory
	 * 		Creates configurations
	 * @param successDominanceFactory
	 * 		Creates success dominance detectors
	 * @param parameters
	 * @return Explorer<P>
	 * @throws Exception
	 */
	public static <P extends Plan> Explorer<P> createExplorer(
			EventBus eventBus, boolean collectStats,
			Schema schema,
			AccessibleSchema accessibleSchema,
			Query<?> query,
			AccessibleChaseState state,
			ReasonerFactory reasonerFactory,
			HomomorphismDetector detector,
			CostEstimator<P> costEstimator,
			ConfigurationFactory<P> configurationFactory,
			SuccessDominanceFactory<P> successDominanceFactory,
			PlannerParameters parameters) throws Exception {

		NodeFactory nf = null;
		PostPruningFactory ppf = null;
		IterativeExecutor executor0 = null;
		IterativeExecutor executor1 = null;
		PriorityAssessor pra = null;
		List<Validator> validators = new ArrayList<>();
		Filter filter = null;

		if (parameters.getPlannerType().equals(PlannerTypes.LINEAR_GENERIC)
				|| parameters.getPlannerType().equals(PlannerTypes.LINEAR_KCHASE)
				|| parameters.getPlannerType().equals(PlannerTypes.LINEAR_OPTIMIZED)) {
			nf = new NodeFactory((ConfigurationFactory<LinearPlan>) configurationFactory);
			ppf = new PostPruningFactory(parameters.getPostPruningType(), nf, accessibleSchema);
		}
		else {

			Validator validator = (Validator) new ValidatorFactory<>(parameters.getValidatorType(), parameters.getDepthThreshold()).getInstance();
			
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
				if(parameters.getReachabilityFiltering()) {
					ReachabilityValidator rv = new ReachabilityValidator(query,
							configurationFactory.getDAGInstances(),
							(FiringGraph) state.getFiringGraph());
					validators.add(rv);
				}
				filter = (Filter) new FilterFactory<>(parameters.getFilterType()).getInstance();
			}

			DefaultPotentialAssessor dpa = new DefaultPotentialAssessor(null, (CostEstimator<DAGPlan>) costEstimator, successDominanceFactory.getInstance());
			pra = PriorityAssessorFactory.createPriorityAssessor(parameters,
					validators,
					dpa,
					query,
					state,
					parameters.getSeed());

			executor0 = IterativeExecutorFactory.createIterativeExecutor(
					parameters.getIterativeExecutorType(),
					parameters.getFirstPhaseThreads(),
					reasonerFactory,
					detector,
					(CostEstimator<DAGPlan>) costEstimator,
					successDominanceFactory.getInstance(),
					eventBus, collectStats);

			executor1 = IterativeExecutorFactory.createIterativeExecutor(
					parameters.getIterativeExecutorType(),
					parameters.getSecondPhaseThreads(),
					reasonerFactory,
					detector,
					(CostEstimator<DAGPlan>) costEstimator,
					successDominanceFactory.getInstance(),
					eventBus, collectStats);
		}

		switch(parameters.getPlannerType()) {
		case LINEAR_GENERIC:
			return (Explorer<P>) new LinearGeneric(
					eventBus, collectStats,
					(CostEstimator<LinearPlan>) costEstimator,
					configurationFactory.getLinearInstance(),
					nf,
					parameters.getMaxDepth());
		case LINEAR_KCHASE:
			return (Explorer<P>) new LinearKChase(
					eventBus, collectStats,
					(CostEstimator<LinearPlan>) costEstimator,
					configurationFactory.getLinearInstance(),
					nf,
					parameters.getMaxDepth(),
					parameters.getChaseInterval());

		case DAG_GENERIC:
			return (Explorer<P>) new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGGeneric(
					eventBus, collectStats,
					configurationFactory.getDAGInstances(),
					filter,
					validators,
					parameters.getMaxDepth(),
					parameters.getOrderAware());

		case DAG_SIMPLEDP:
			return (Explorer<P>) new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGSimpleDP(
					eventBus, collectStats,
					configurationFactory.getDAGInstances(),
					filter,
					validators,
					parameters.getMaxDepth(),
					parameters.getOrderAware());

		case DAG_CHASEFRIENDLYDP:
			return (Explorer<P>) new uk.ac.ox.cs.pdq.planner.dag.explorer.DAGChaseFriendlyDP(
					eventBus, collectStats,
					configurationFactory.getDAGInstances(),
					filter, 
					validators,
					parameters.getMaxDepth(),
					parameters.getOrderAware());

		case DAG_OPTIMIZED:
			return (Explorer<P>) new DAGOptimized(
					eventBus, collectStats,
					configurationFactory.getDAGInstances(),
					filter,
					pra, executor0, executor1,
					parameters.getMaxDepth());

		case LINEAR_OPTIMIZED:
			return (Explorer<P>) new LinearOptimized(
					eventBus, collectStats,
					(CostEstimator<LinearPlan>) costEstimator,
					configurationFactory.getLinearInstance(),
					nf,
					parameters.getMaxDepth(),
					parameters.getQueryMatchInterval(),
					ppf.getInstance(),
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
