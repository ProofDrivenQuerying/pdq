package uk.ac.ox.cs.pdq.planner.dag.explorer;

import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.CANDIDATES;
import static uk.ac.ox.cs.pdq.planner.logging.performance.PlannerStatKeys.CONFIGURATIONS;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.cardinality.CardinalityEstimator;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGAnnotatedPlanClasses;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.SynchronizedAnnotatedPlanClasses;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.FinalIterationThreadResults;
import uk.ac.ox.cs.pdq.planner.dag.explorer.parallel.IterativeExecutor;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * Very chase friendly dynamic programming dag explorer. It performs parallel chasing and
 * (success-)dominance, equivalence and success checks in parallel
 * @author Efthymia Tsamoura
 *
 */
public class DAGOptimized extends DAGExplorer {

	/**
	 * The maximum depth we can explore. The exploration ends when
	 * there does not exist any configuration with depth < maxDepth
	 */
	protected final int maxDepth;

	/** The current exploration depth */
	protected int depth;

	/** Performs parallel chasing */
	private final IterativeExecutor firstPhaseExecutor;

	/** Iterate over all newly created configurations in parallel and returns the best configuration*/
	private final IterativeExecutor secondPhaseExecutor;

	/** Filters out configurations at the end of each iteration*/
	private final Filter filter;

	/** Configurations produced during the previous round*/
	private final Queue<DAGAnnotatedPlan> left;

	/** Classes of structurally equivalent configurations*/
	private final DAGAnnotatedPlanClasses classes;

	
	/**
	 * 
	 * @param eventBus
	 * @param collectStats
	 * @param initialConfigurations
	 * 		ApplyRule configurations to initilise the explorer
	 * @param filter
	 * 		Filters out configurations at the end of each iteration
	 * @param priority Prioritises configurations
	 * 
	 * @param firstPhaseExecutor
	 * 		Performs parallel chasing
	 * @param secondPhaseExecutor
	 * 		Iterates over all newly created configurations in parallel and returns the best configuration
	 * @param maxDepth
	 * 		The maximum depth to explore
	 * @throws PlannerException
	 */
	public DAGOptimized(
			EventBus eventBus, 
			boolean collectStats, 
			PlannerParameters parameters,
			Query<?> query, 
			Schema accessibleSchema, 
			Chaser chaser, 
			HomomorphismDetector detector,
			CardinalityEstimator cardinalityEstimator,
			Filter filter,
			IterativeExecutor firstPhaseExecutor,
			IterativeExecutor secondPhaseExecutor,
			int maxDepth) throws PlannerException {
		super(eventBus, collectStats, parameters, 
				query, accessibleSchema, chaser, detector, cardinalityEstimator);
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
	 * @throws PlannerException
	 */
	@Override
	protected void _explore() throws PlannerException, LimitReachedException {
		if (this.depth > this.maxDepth) {
			this.forcedTermination = true;
			return;
		}
		//Check the ApplyRule configurations for success
		if (this.depth == 1) {
			for (DAGAnnotatedPlan configuration: this.left) {
				if (this.bestPlan == null
						|| (configuration.getPlan().getCost().lessThan(this.bestPlan.getCost()))) {
					if (configuration.isSuccessful(this.query)) {
						this.setBestPlan(configuration);
					}
				}
			}
			this.stats.set(CONFIGURATIONS, this.left.size());
		} else if (this.depth > 1) {
			this.checkLimitReached();
			//Perform parallel chasing
			Collection<DAGAnnotatedPlan> configurations =
					this.firstPhaseExecutor.chase(this.depth,
							this.left,
							this.classes.getConfigurations(),
							this.query,
							this.schema.getDependencies(),
							this.bestConfiguration,
							this.classes,
							true,
							Double.valueOf((this.maxElapsedTime - (this.elapsedTime/1e6))).longValue(),
							TimeUnit.MILLISECONDS);
			if(configurations == null || configurations.isEmpty()) {
				this.forcedTermination = true;
				return;
			}

			this.checkLimitReached();
			//Iterate over all newly created configurations in parallel and return the best configuration
			FinalIterationThreadResults results = this.secondPhaseExecutor.finalIteration(
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
			if (bestResult !=  null && !bestResult.getPlan().getCost().isUpperBound()) {
				this.setBestPlan(bestResult);
			}

			if (output.isEmpty()) {
				this.forcedTermination = true;
				return;
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
