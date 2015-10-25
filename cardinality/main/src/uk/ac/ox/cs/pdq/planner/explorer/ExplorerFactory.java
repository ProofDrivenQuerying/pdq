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
import uk.ac.ox.cs.pdq.planner.cardinality.CardinalityEstimator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.DAGOptimized;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.FilterFactory;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.IterativeExecutor;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.IterativeExecutorFactory;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.LinearValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ValidatorFactory;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.DominanceFactory;
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
	 * @param query
	 * @param chaser
	 * @param detector
	 * @param cardinalityEstimator
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public static <P extends Plan> Explorer<P> createExplorer(
			EventBus eventBus, 
			boolean collectStats,
			Schema schema,
			Query<?> query,
			Chaser chaser,
			HomomorphismDetector detector,
			CardinalityEstimator cardinalityEstimator,
			PlannerParameters parameters) throws Exception {

		Dominance[] dominance = DominanceFactory.getInstance(parameters.getDominanceType(), cardinalityEstimator);
		Dominance qualityDominance = DominanceFactory.getInstance(parameters.getSuccessDominanceType(), cardinalityEstimator);
		
		IterativeExecutor executor0 = null;
		IterativeExecutor executor1 = null;
		List<Validator> validators = new ArrayList<>();
		Filter filter = null;

		{
			Validator validator = (Validator) new ValidatorFactory(parameters.getValidatorType(), parameters.getDepthThreshold()).getInstance();

			{
				validators.add(validator);
				filter = (Filter) new FilterFactory(parameters.getFilterType()).getInstance();
			}

			executor0 = IterativeExecutorFactory.createIterativeExecutor(
					parameters.getIterativeExecutorType(),
					parameters.getFirstPhaseThreads(),
					chaser,
					detector,
					cardinalityEstimator,
					qualityDominance,
					dominance,
					validators);

			executor1 = IterativeExecutorFactory.createIterativeExecutor(
					parameters.getIterativeExecutorType(),
					parameters.getSecondPhaseThreads(),
					chaser,
					detector,
					cardinalityEstimator,
					qualityDominance,
					dominance,
					validators);
		}

		switch(parameters.getPlannerType()) {

		case DAG_OPTIMIZED:
			return (Explorer<P>) new DAGOptimized(
					eventBus, collectStats,
					parameters,
					query,
					schema,
					chaser,
					detector,
					cardinalityEstimator,
					filter,
					executor0, executor1,
					parameters.getMaxDepth());


		default:
			throw new IllegalStateException("Unsupported planner type " + parameters.getPlannerType());
		}
	}

}
