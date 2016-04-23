/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag.explorer;

import static uk.ac.ox.cs.pdq.cardinality.logging.performance.PlannerStatKeys.CANDIDATES;
import static uk.ac.ox.cs.pdq.cardinality.logging.performance.PlannerStatKeys.CONFIGURATIONS;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cardinality.CardinalityException;
import uk.ac.ox.cs.pdq.cardinality.CardinalityParameters;
import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.UnaryAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses;
import uk.ac.ox.cs.pdq.cardinality.dag.equivalence.SynchronizedAnnotatedPlanClasses;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.parallel.ExplorationThreadResults;
import uk.ac.ox.cs.pdq.cardinality.dag.explorer.parallel.IterativeExecutor;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityEstimator;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityUtility;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Very chase friendly dynamic programming dag explorer. It performs parallel chasing and
 * (success-)dominance, equivalence and success checks in parallel
 * @author Efthymia Tsamoura
 *
 */
public class DAGOptimized extends DAGExplorer {

	/** The log. */
	protected static Logger log = Logger.getLogger(DAGOptimized.class);

	/**
	 * The maximum depth we can explore. The exploration ends when
	 * there does not exist any configuration with depth < maxDepth
	 */
	protected final int maxDepth;

	/**  The current exploration depth. */
	protected int depth;

	/**  Performs parallel chasing. */
	private final IterativeExecutor firstPhaseExecutor;

	/**  Iterate over all newly created configurations in parallel and returns the best configuration. */
	private final IterativeExecutor secondPhaseExecutor;

	/**  Filters out configurations at the end of each iteration. */
	private final Filter filter;

	/**  Configurations produced during the previous round. */
	private final Queue<DAGAnnotatedPlan> left;

	/**  Classes of structurally equivalent configurations. */
	private final DAGAnnotatedPlanClasses classes;


	/**
	 * Instantiates a new DAG optimized.
	 *
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param parameters the parameters
	 * @param query the query
	 * @param schema the schema
	 * @param chaser the chaser
	 * @param detector the detector
	 * @param cardinalityEstimator the cardinality estimator
	 * @param filter the filter
	 * @param firstPhaseExecutor the first phase executor
	 * @param secondPhaseExecutor the second phase executor
	 * @param maxDepth the max depth
	 * @throws CardinalityException the planner exception
	 */
	public DAGOptimized(
			EventBus eventBus, 
			boolean collectStats, 
			CardinalityParameters parameters,
			ConjunctiveQuery query, 
			Schema schema, 
			Chaser chaser, 
			HomomorphismDetector detector,
			CardinalityEstimator cardinalityEstimator,
			Filter filter,
			IterativeExecutor firstPhaseExecutor,
			IterativeExecutor secondPhaseExecutor,
			int maxDepth) throws CardinalityException {
		super(eventBus, collectStats, parameters, 
				query, schema, chaser, detector, cardinalityEstimator);
		Preconditions.checkNotNull(firstPhaseExecutor);
		Preconditions.checkNotNull(secondPhaseExecutor);
		this.filter = filter;
		this.firstPhaseExecutor = firstPhaseExecutor;
		this.secondPhaseExecutor = secondPhaseExecutor;
		this.maxDepth = maxDepth;
		List<DAGAnnotatedPlan> initialConfigurations = this.createInitialConfigurations();
		if(this.filter != null) {
			Collection<DAGAnnotatedPlan> toDelete = this.filter.filter(initialConfigurations);
			initialConfigurations.removeAll(toDelete);
		}
		this.left = new ConcurrentLinkedQueue<>();
		this.classes = new SynchronizedAnnotatedPlanClasses();
		this.left.addAll(initialConfigurations);
		for(DAGAnnotatedPlan initialConfiguration: initialConfigurations) {
			this.classes.addEntry(initialConfiguration);
		}
	}

	/**
	 * _explore.
	 *
	 * @throws CardinalityException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	protected void _explore() throws CardinalityException, LimitReachedException {
		if (this.depth > this.maxDepth) {
			this.forcedTermination = true;
			return;
		}
		//Check the ApplyRule configurations for success
		if (this.depth == 1) {
			for (DAGAnnotatedPlan configuration: this.left) {
				Pair<BigInteger, Double> sizeQuality = this.cardinalityEstimator.sizeQualityOf((UnaryAnnotatedPlan) configuration);
				configuration.setSizeOf(sizeQuality.getLeft());
				configuration.setQuality(sizeQuality.getRight());				

				//Check for query match
				boolean matchesQuery = configuration.isSuccessful(this.query);
				//Estimate the configuration's adjusted quality 
				double adjustedQuality = this.cardinalityEstimator.adjustedQualityOf(configuration, this.query, matchesQuery);
				configuration.setAdjustedQuality(adjustedQuality);

				//Estimate the configuration's cardinality (size after considering any projections)
				BigInteger sizeOfProjection = this.cardinalityEstimator.sizeOfProjection(configuration, this.query);
				configuration.setSizeOfProjection(sizeOfProjection);	

				if (matchesQuery || configuration.getOutput().containsAll(this.query.getGroundingsProjectionOnFreeVars().values())) {
					//Estimate the coverage of the input annotated plan
					int coverage = CardinalityUtility.coverage(configuration, this.query, this.detector);
					configuration.setCoverage(coverage);
					this.setBestPlan(configuration);
				}
			}
			this.stats.set(CONFIGURATIONS, this.left.size());
		} else if (this.depth > 1) {
			this.checkLimitReached();
			//Perform parallel chasing
			Collection<DAGAnnotatedPlan> configurations =
					this.firstPhaseExecutor.reason(this.depth,
							this.left,
							this.classes.getConfigurations(),
							this.query,
							this.schema,
							this.bestConfiguration,
							this.classes,
							false,
							Double.valueOf((this.maxElapsedTime - (this.elapsedTime/1e6))).longValue(),
							TimeUnit.MILLISECONDS);
			if(configurations == null || configurations.isEmpty()) {
				this.forcedTermination = true;
				return;
			}

			this.checkLimitReached();
			//Iterate over all newly created configurations in parallel and return the best configuration
			ExplorationThreadResults results = this.secondPhaseExecutor.explore(
					this.query,
					new ConcurrentLinkedQueue<>(configurations),
					this.classes,
					this.bestConfiguration,
					Double.valueOf((this.maxElapsedTime - (this.elapsedTime/1e6))).longValue(),
					TimeUnit.MILLISECONDS);


			//Stop if no new configuration is being found
			if (results == null) {
				this.forcedTermination = true;
				return;
			}
			//Update the best configuration
			List<DAGAnnotatedPlan> output = results.getOutput();
			DAGAnnotatedPlan bestResult = results.getBest();
			if (bestResult !=  null) {
				this.setBestPlan(bestResult);
			}

			if (output.isEmpty()) {
				this.forcedTermination = true;
				return;
			}

			for(DAGAnnotatedPlan o:output) {
				log.trace(o + "\t\t" +  
						"Size: " + o.getSize() + "\t" +
						"SizeOf: " + o.getSizeOf() + "\t" + 
						"SizeOfProjection: " + o.getSizeOfProjection() + "\t" +  
						"Quality: " + o.getQuality() + "Adjusted quality: " + o.getAdjustedQuality());
			}

			this.left.clear();
			this.left.addAll(CollectionUtils.intersection(output, this.classes.getConfigurations()));

			//Filter out configurations
			if(this.filter != null) {
				Collection<DAGAnnotatedPlan> toDelete = this.filter.filter(this.classes.getConfigurations());
				this.classes.removeAll(toDelete);
				this.left.removeAll(toDelete);
			}

			this.stats.set(CONFIGURATIONS, this.classes.size());
			this.stats.set(CANDIDATES, this.left.size());
		}
		this.depth++;
	}

}
