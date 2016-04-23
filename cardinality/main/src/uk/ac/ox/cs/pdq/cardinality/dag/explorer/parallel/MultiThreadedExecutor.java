/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag.explorer.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.cardinality.CardinalityException;
import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * A IterativeExecutor that uses multi-threading on a single machine.
 *
 * @author Efthymia Tsamoura
 */
public class MultiThreadedExecutor extends IterativeExecutor {

	/** The mtcontext. */
	private final MultiThreadedContext mtcontext;

	/**
	 * Constructor for MultiThreadedExecutor.
	 * @param context MultiThreadedContext
	 */
	public MultiThreadedExecutor(MultiThreadedContext context) {
		super(context);
		this.mtcontext = context;
	}

	/**
	 * Creates new binary configurations by combining configurations from the input left and right collections.
	 * If twoWay=TRUE the output configurations are of the form BinaryConfiguration(L,R) and BinaryConfiguration(R,L), where L belongs to the
	 * left input collection and R to the right input collection, respectively.
	 * Otherwise, they are of the form  BinaryConfiguration(L,R)
	 *
	 * @param depth the target depth of the created configurations
	 * @param left 		The configurations to consider on the left
	 * @param right 		The configurations to consider on the right
	 * @param query the query
	 * @param schema the schema
	 * @param best 	 	The minimum cost closed and successful configuration found so far. The plans that correspond to the
	 * 		returned configurations have cost < the bestConfiguration
	 * @param classes the classes
	 * @param twoWay the two way
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return Collection<DAGAnnotatedPlan>
	 * @throws CardinalityException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	public Collection<DAGAnnotatedPlan> reason(
			int depth,
			Queue<DAGAnnotatedPlan> left,
			Collection<DAGAnnotatedPlan> right,
			ConjunctiveQuery query,
			Schema schema,
			DAGAnnotatedPlan best,
			DAGAnnotatedPlanClasses classes, 
			boolean twoWay,
			long timeout, TimeUnit unit) throws CardinalityException, LimitReachedException {
		if (timeout <= 0) {
			throw new LimitReachedException(Reasons.TIMEOUT);
		}
		long start = System.currentTimeMillis();
		//Create a pool of threads to run in parallel
		ExecutorService executorService = Executors.newFixedThreadPool(this.mtcontext.getParallelThreads());
		//Map of representatives. For each configuration c = BinConfiguration(c_1,c_2) we create a map from the
		//equivalence classes of c and c' to c''. This map helps us reducing the chasing time, i.e.,
		//if c'_1 and c'_2 are structurally equivalent to c_1 and c_2, respectively,
		//and c = BinConfiguration(c_1,c_2) has already been fully chased,
		//then we copy the state of c to the state of c' = BinConfiguration(c'_1,c'_2).
		AnnotatedPlanRepresentative representatives = new AnnotatedPlanRepresentative();
		//The output configurations
		Map<Pair<DAGAnnotatedPlan,DAGAnnotatedPlan>,DAGAnnotatedPlan> output = new ConcurrentHashMap<>();
		
		Collection<DAGAnnotatedPlan> copy = Sets.newLinkedHashSet(left);

		try {
			Queue<DAGAnnotatedPlan> leftInput = left;
			Collection<DAGAnnotatedPlan> rightInput = right;
			do {
				List<Callable<Boolean>> threads = new ArrayList<>();
				for(int j = 0; j < this.mtcontext.getParallelThreads(); ++j) {
					//Create the threads that will create new binary configurations using the input left, right collections
					threads.add(new ReasoningThread(
							depth,
							leftInput,
							rightInput,
							query,
							schema,
							this.mtcontext.getReasoners()[j],
							this.mtcontext.getDetectors()[j],
							this.mtcontext.getCardinalityEstimators()[j],
							this.mtcontext.getQualityDominances()[j],
							best,
							this.mtcontext.getValidators()[j],
							classes,
							representatives,
							output
							));
				}
				List<Future<Boolean>> results = executorService.invokeAll(threads, timeout, unit);
				try {
					for(Future<Boolean> result: results){
						result.get();
					}
				} catch(java.util.concurrent.CancellationException e) {
					executorService.shutdownNow();
					if (timeout <= (System.currentTimeMillis() - start)) {
						throw new LimitReachedException(Reasons.TIMEOUT);
					}
					return null;
				}
				executorService.shutdown();

				//If twoWay = TRUE create also configurations BinaryConfiguration(R,L), where R and L belong to the right and left
				//input collections, respectively.
				if(twoWay) {
					executorService = Executors.newFixedThreadPool(this.mtcontext.getParallelThreads());
					leftInput = new ConcurrentLinkedQueue<>(right);
					rightInput = copy;
					twoWay = false;
				}
				else {
					copy.clear();
					break;
				}
			} while(true);
			return output.values();

		} catch (InterruptedException | ExecutionException e) {
			executorService.shutdownNow();
			handleExceptions(e);
			return null;
		}
	}

	/**
	 * Iterates over the input collection of configurations to identify the minimum-cost one.
	 *
	 * @param query the query
	 * @param input 		The input set of configurations
	 * @param classes 		Classes of structurally equivalent configurations
	 * @param bestConfiguration 		The minimum cost closed and successful configuration found so far. The plans that correspond to the
	 * 		returned configurations have cost < the bestConfiguration
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return 		the non-dominated configurations (that could lead to the minimum-cost configuration),
	 * 		the minimum-cost configuration with cost < the cost of the input bestConfiguration and the successful configurations
	 * @throws CardinalityException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	public ExplorationThreadResults explore(
			ConjunctiveQuery query,
			Queue<DAGAnnotatedPlan> input,
			DAGAnnotatedPlanClasses classes,
			DAGAnnotatedPlan best,
			long timeout, TimeUnit unit) throws CardinalityException, LimitReachedException {
		if (timeout <= 0) {
			throw new LimitReachedException(Reasons.TIMEOUT);
		}
		long start = System.currentTimeMillis();

		//Create a pool of threads to run in parallel
		ExecutorService executorService = Executors.newFixedThreadPool(this.mtcontext.getParallelThreads());
		try {

			//The output non-dominated configurations
			Set<DAGAnnotatedPlan> output  =
					Collections.newSetFromMap(new ConcurrentHashMap<DAGAnnotatedPlan, Boolean>());
			//The output non-dominated and successful (and not closed) configurations
			Set<DAGAnnotatedPlan> successfulConfigurations =
					Collections.newSetFromMap(new ConcurrentHashMap<DAGAnnotatedPlan, Boolean>());
			List<Callable<DAGAnnotatedPlan>> threads = new ArrayList<>();
			for (int j = 0; j < this.mtcontext.getParallelThreads(); ++j) {
				//Create the threads that will iterate over the input configurations to find the non-dominated ones,
				//along with the minimum-cost successful and closed one
				threads.add(new ExplorationThread(
						query,
						input,
						classes,
						best,
						this.mtcontext.getDetectors()[j],
						this.mtcontext.getCardinalityEstimators()[j],
						this.mtcontext.getQualityDominances()[j],
						this.mtcontext.getDominances()[j],
						output,
						successfulConfigurations
						));
			}

			List<Future<DAGAnnotatedPlan>> results = executorService.invokeAll(threads, timeout, unit);
			DAGAnnotatedPlan iterationBest = best;
			try {
				for (Future<DAGAnnotatedPlan> result: results){
					DAGAnnotatedPlan configuration = result.get();
					
					//Removed at 04/02/2015
					if(iterationBest == null
					|| (configuration != null
						&& iterationBest.getSize().compareTo(configuration.getSize()) >= 0 
						&& iterationBest.getAdjustedQuality() >= configuration.getAdjustedQuality()
									))
					{
						iterationBest = configuration;
					}
					
//					if(iterationBest == null && configuration != null) {
//						iterationBest = configuration;
//					}
//					else if(iterationBest != null && 
//							configuration != null &&
//							configuration.getCoverage() >= iterationBest.getCoverage() &&
//							configuration.getSize().compareTo(iterationBest.getSize()) < 0) {
//						iterationBest = configuration;
//					}
//					else if(iterationBest != null && 
//							configuration != null &&
//							configuration.getCoverage() == iterationBest.getCoverage() &&
//							configuration.getAdjustedQuality() <= iterationBest.getAdjustedQuality() &&
//							configuration.getSize().compareTo(iterationBest.getSize()) < 0) {
//						iterationBest = configuration;
//					}
				}
			}
			catch(java.util.concurrent.CancellationException e) {
				executorService.shutdownNow();
				if (timeout <= (System.currentTimeMillis() - start)) {
					throw new LimitReachedException(Reasons.TIMEOUT);
				}
				return null;
			}

			executorService.shutdown();
			return new ExplorationThreadResults(
					Lists.newArrayList(output),
					successfulConfigurations,
					iterationBest
					);
		} catch (InterruptedException | ExecutionException e) {
			executorService.shutdownNow();
			handleExceptions(e);
			return null;
		}
	}

	/**
	 * Handle exceptions.
	 *
	 * @param e Exception
	 * @throws CardinalityException the planner exception
	 */
	private static void handleExceptions(Exception e) throws CardinalityException {
		Throwable throwable = e.getCause();
		if (throwable != null) {
			if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			}
			if (throwable instanceof Error) {
				throw (Error) throwable;
			}
		}
		throw new CardinalityException(e);
	}
}
