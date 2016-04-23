/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.explorer;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.pdq.cardinality.CardinalityParameters;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.DAGOptimized;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.filters.FilterFactory;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.parallel.IterativeExecutor;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.parallel.IterativeExecutorFactory;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.validators.JoinValidator;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.validators.ValidatorFactory;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cardinality.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.cardinality.reasoning.chase.dominance.DominanceFactory;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Creates an explorer given the input arguments.
 *
 * @author Efthymia Tsamoura
 */
public class ExplorerFactory {

	/**
	 * Creates a new Explorer object.
	 *
	 * @param <P> the generic type
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param schema the schema
	 * @param query the query
	 * @param chaser the chaser
	 * @param detector the detector
	 * @param cardinalityEstimator the cardinality estimator
	 * @param parameters the parameters
	 * @return the explorer< p>
	 * @throws Exception the exception
	 */
	public static <P extends Plan> Explorer<P> createExplorer(
			EventBus eventBus, 
			boolean collectStats,
			Schema schema,
			ConjunctiveQuery query,
			Chaser chaser,
			HomomorphismDetector detector,
			CardinalityEstimator cardinalityEstimator,
			CardinalityParameters parameters) throws Exception {

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
				validators.add(new JoinValidator());
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
