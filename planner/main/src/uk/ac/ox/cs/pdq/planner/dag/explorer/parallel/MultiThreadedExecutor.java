package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

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

import uk.ac.ox.cs.pdq.fol.Dependency;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.LimitReachedException;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.SynchronizedEquivalenceClasses;

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
	 * @param dependencies the dependencies
	 * @param bestConfiguration 	 	The minimum cost closed and successful configuration found so far. The plans that correspond to the
	 * 		returned configurations have cost < the bestConfiguration
	 * @param equivalenceClasses the equivalence classes
	 * @param twoWay the two way
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return Collection<DAGChaseConfiguration>
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	public Collection<DAGChaseConfiguration> reason(
			int depth,
			Queue<DAGChaseConfiguration> left,
			Collection<DAGChaseConfiguration> right,
			ConjunctiveQuery query,
			Collection<? extends Dependency> dependencies,
			DAGChaseConfiguration bestConfiguration,
			DAGEquivalenceClasses equivalenceClasses, 
			boolean twoWay,
			long timeout, TimeUnit unit) throws PlannerException, LimitReachedException {
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
		Representative representatives = new Representative();
		//The output configurations
		Map<Pair<DAGChaseConfiguration,DAGChaseConfiguration>,DAGChaseConfiguration> output = new ConcurrentHashMap<>();
		
		Collection<DAGChaseConfiguration> copy = Sets.newLinkedHashSet(left);

		try {
			Queue<DAGChaseConfiguration> leftInput = left;
			Collection<DAGChaseConfiguration> rightInput = right;
			do {
				List<Callable<Boolean>> threads = new ArrayList<>();
				for(int j = 0; j < this.mtcontext.getParallelThreads(); ++j) {
					//Create the threads that will create new binary configurations using the input left, right collections
					threads.add(new ReasoningThread(
							depth,
							leftInput,
							rightInput,
							query,
							dependencies,
							this.mtcontext.getReasoners()[j],
							this.mtcontext.getConnections()[j],
							this.mtcontext.getCostEstimators()[j],
							this.mtcontext.getSuccessDominances()[j],
							bestConfiguration,
							this.mtcontext.getValidators()[j],
							equivalenceClasses,
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
	 * @param equivalenceClasses 		Classes of structurally equivalent configurations
	 * @param bestConfiguration 		The minimum cost closed and successful configuration found so far. The plans that correspond to the
	 * 		returned configurations have cost < the bestConfiguration
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return 		the non-dominated configurations (that could lead to the minimum-cost configuration),
	 * 		the minimum-cost configuration with cost < the cost of the input bestConfiguration and the successful configurations
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	public ExplorationResults explore(
			ConjunctiveQuery query,
			Queue<DAGChaseConfiguration> input,
			DAGEquivalenceClasses equivalenceClasses,
			DAGChaseConfiguration bestConfiguration,
			long timeout, TimeUnit unit) throws PlannerException, LimitReachedException {
		if (timeout <= 0) {
			throw new LimitReachedException(Reasons.TIMEOUT);
		}
		long start = System.currentTimeMillis();

		//Create a pool of threads to run in parallel
		ExecutorService executorService = Executors.newFixedThreadPool(this.mtcontext.getParallelThreads());
		try {

			//The output non-dominated configurations
			Set<DAGChaseConfiguration> output  =
					Collections.newSetFromMap(new ConcurrentHashMap<DAGChaseConfiguration, Boolean>());
			//The output non-dominated and successful (and not closed) configurations
			Set<DAGChaseConfiguration> successfulConfigurations =
					Collections.newSetFromMap(new ConcurrentHashMap<DAGChaseConfiguration, Boolean>());
			List<Callable<DAGChaseConfiguration>> threads = new ArrayList<>();
			for (int j = 0; j < this.mtcontext.getParallelThreads(); ++j) {
				//Create the threads that will iterate over the input configurations to find the non-dominated ones,
				//along with the minimum-cost successful and closed one
				threads.add(new ExplorationThread(
						query,
						input,
						equivalenceClasses,
						bestConfiguration,
						this.mtcontext.getSuccessDominances()[j],
						this.mtcontext.getDominances()[j],
						output,
						successfulConfigurations
						));
			}

			List<Future<DAGChaseConfiguration>> results = executorService.invokeAll(threads, timeout, unit);
			DAGChaseConfiguration configuration = bestConfiguration;
			try {
				for (Future<DAGChaseConfiguration> result: results){
					DAGChaseConfiguration r = result.get();
					if(configuration == null
							|| (r != null
							&& configuration.getPlan().getCost().greaterThan(r.getPlan().getCost())))
					{
						configuration = r;
					}
				}

				if(equivalenceClasses instanceof SynchronizedEquivalenceClasses) {
					((SynchronizedEquivalenceClasses)equivalenceClasses).wakeupSleep(configuration != null ? configuration.getPlan() : null);
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
			return new ExplorationResults(
					Lists.newArrayList(output),
					successfulConfigurations,
					configuration
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
	 * @throws PlannerException the planner exception
	 */
	private static void handleExceptions(Exception e) throws PlannerException {
		Throwable throwable = e.getCause();
		if (throwable != null) {
			if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			}
			if (throwable instanceof Error) {
				throw (Error) throwable;
			}
		}
		throw new PlannerException(e);
	}
}
