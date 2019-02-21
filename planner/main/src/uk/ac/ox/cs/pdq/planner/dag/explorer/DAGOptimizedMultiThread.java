package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.exceptions.LimitReachedException;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.equivalence.dag.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

/**
 * An explorer for plans using ideas from dynamic programming. It performs
 * parallel chasing and (success-)dominance, equivalence and success checks in
 * parallel
 * 
 * Similar to DAG Optimized, but the code is simplified.
 * 
 * @author Gabor
 *
 */
public class DAGOptimizedMultiThread extends DAGOptimized {

	/** Configurations produced during the previous round. */
	private final Queue<Runnable> createQueue;
	private List<ThreadPoolWorker> threadPool;
	private long TIMEOUT;
	/**
	 * Instantiates a new DAG optimized.
	 * 
	 * @param eventBus
	 * @param parameters
	 * @param query
	 * @param accessibleQuery
	 * @param accessibleSchema
	 * @param chaser
	 * @param connection
	 * @param costEstimator
	 * @param filter
	 *            Filters out configurations at the end of each iteration
	 * @param maxDepth
	 *            The maximum depth to explore
	 * @throws PlannerException
	 * @throws SQLException
	 */
	public DAGOptimizedMultiThread(EventBus eventBus, PlannerParameters parameters, ConjunctiveQuery query,
			AccessibleSchema accessibleSchema, Chaser chaser,
			DatabaseManager connection, CostEstimator costEstimator, Filter filter, int maxDepth)
			throws PlannerException, SQLException {
		super(eventBus, parameters, query, accessibleSchema, chaser, connection, costEstimator, filter, maxDepth);
		
		this.createQueue = new ConcurrentLinkedQueue<>();
		threadPool = new ArrayList<>();
		for (int i = 0; i < parameters.getDagThreads(); i++) {
			threadPool.add(new ThreadPoolWorker(createQueue,"CreatePoolThread"+i));
		}
		TIMEOUT = parameters.getDagThreadTimeout();
	}
	
	/**
	 * Stops the pool.
	 */
	public void shutdownThreads() {
		if (threadPool != null) 
			for (ThreadPoolWorker t: threadPool) t.setShutdown(true);
	}
	/**
	 * _explore.
	 *
	 * @throws PlannerException
	 *             the planner exception
	 * @throws LimitReachedException
	 *             the limit reached exception
	 */
	@Override
	public void performSingleExplorationStep() throws PlannerException, LimitReachedException {
		if (this.depth > this.maxDepth) {
			this.forcedTermination = true;
			return;
		}
		// Check the ApplyRule configurations for success
		if (this.depth == 1) {
			for (DAGChaseConfiguration configuration : this.leftSideConfigurations) {
				Cost cost = this.costEstimator.cost(configuration.getPlan());
				configuration.setCost(cost);
				if (this.bestPlan == null
						|| (configuration.isClosed() && configuration.getCost().lessThan(this.bestCost))) {
					if (configuration.isClosed() && configuration.isSuccessful(this.accessibleQuery)) {
						this.setBestPlan(configuration);
					}
				}
			}
		} else if (this.depth > 1) {
			//generate new configurations by composing old
			this.checkLimitReached();
			List<CreateBinaryConfigurationsTask> currentTasks = new ArrayList<>();
			Queue<DAGChaseConfiguration> leftCopy = new ConcurrentLinkedQueue<>();
			leftCopy.addAll(this.leftSideConfigurations);
			ArrayList<DAGChaseConfiguration> right = new ArrayList<>();
			right.addAll(this.equivalenceClasses.getConfigurations());
			int groupSize = right.size()/threadPool.size()+1;
			// group the right side configurations to create multiple tasks. 
			for (int i = 0; i < right.size(); i += groupSize) {
				Collection<DAGChaseConfiguration> rightGroup = new ArrayList<>();
				for (int j = i; j< i+groupSize; j++) {
					if (j < right.size())
						rightGroup.add(right.get(j));
				}
				// Instead of combining all left and right configurations we combine all left with a small group from the right in one thread. 
				CreateBinaryConfigurationsTask a = new CreateBinaryConfigurationsTask(this,
						new ConcurrentLinkedQueue<>(leftCopy), new ConcurrentLinkedQueue<>(rightGroup),
						this.accessibleSchema.getInferredAccessibilityAxioms(), this.bestConfiguration,
						this.equivalenceClasses);
				CreateBinaryConfigurationsTask b = new CreateBinaryConfigurationsTask(this,
						new ConcurrentLinkedQueue<>(rightGroup), new ConcurrentLinkedQueue<>(leftCopy), 
						this.accessibleSchema.getInferredAccessibilityAxioms(), this.bestConfiguration,
						this.equivalenceClasses);
				currentTasks.add(a);
				currentTasks.add(b);
				createQueue.add(a);
				createQueue.add(b);
			}
			
			Collection<DAGChaseConfiguration> newlyCreatedConfigurations = new ArrayList<>();
			for (CreateBinaryConfigurationsTask t:currentTasks) {
				newlyCreatedConfigurations.addAll(t.getReturnValue());
			}
			// Check for new configurations
			if (newlyCreatedConfigurations == null || newlyCreatedConfigurations.isEmpty()) {
				this.forcedTermination = true;
				shutdownThreads();
				return;
			}

			this.checkLimitReached();
			// Iterate over all newly created configurations and return the best
			// configuration
			Set<DAGChaseConfiguration> nonDominatedConfigurations = null;
			try {
				//nonDominatedConfigurations will contain all new configs that had no equivalence classes before and are not dominated by anything. 
				nonDominatedConfigurations = findBestAndUpdateEquivalences(new ConcurrentLinkedQueue<>(newlyCreatedConfigurations), bestConfiguration);
			} catch (Exception e) {
				e.printStackTrace();
				handleExceptions(e);
			}

			// Stop if no new configuration is being found
			if (nonDominatedConfigurations.isEmpty()) {
				this.forcedTermination = true;
				shutdownThreads();
				return;
			}

			this.leftSideConfigurations.clear();
			this.leftSideConfigurations.addAll(CollectionUtils.intersection(nonDominatedConfigurations,
					this.equivalenceClasses.getConfigurations()));

			// Filter out configurations
			// filters are configured in the case.properties files.
			if (this.filter != null) {
				Collection<DAGChaseConfiguration> toDelete = this.filter
						.filter(this.equivalenceClasses.getConfigurations());
				this.equivalenceClasses.removeAll(toDelete);
				this.leftSideConfigurations.removeAll(toDelete);
			}
		}
		this.depth++;
	}
	/**
	 * @author gabor
	 * This thread will execute runnable tasks from the given queue. In case the queue is empty it will wait for new tasks to appear in it.
	 */
	private class ThreadPoolWorker extends Thread{
		private Queue<Runnable> queue;
		private boolean shutdown = false;

		public ThreadPoolWorker(Queue<Runnable> queue,String name) {
			super(name);
			this.queue = queue;
			this.start();
		}
		public void run() {
			try {
				while (!shutdown) {
					Runnable task = queue.poll();
					if (task == null) {
						// wait until task arrives
						synchronized (queue) {
							try {
								queue.wait(100);
							} catch (InterruptedException e) {
							}
						}
					} else {
						// run the task, it shouldn't have a return value nor should it throw anything.
						task.run();
					}
						
				}
			} catch(Throwable t) {
				// since it will be executed as a thread we need to make sure we log any events...
				t.printStackTrace();
			}
		}
		public void setShutdown(boolean shutdown) {
			this.shutdown = shutdown;
		}
	}
	
	/**
	 * This class represents one part of the exploration. Multiple instances will be executed at the same time by executor threads.
	 *
	 */
	private class CreateBinaryConfigurationsTask implements Runnable {
		private DAGOptimizedMultiThread executor;
		private Queue<DAGChaseConfiguration> leftSideConfigurations;
		private Collection<DAGChaseConfiguration> rightSideConfigurations;
		private Dependency[] inferredAccessibilityAxioms;
		private DAGChaseConfiguration bestConfiguration;
		private DAGEquivalenceClasses equivalenceClasses;
		private Collection<DAGChaseConfiguration> returnValue;
		private Throwable t;
		volatile private boolean finished = false;
		
		/**
		 * Constructor to store all parameters needed for the selectAndCreateBinaryConfigurationsToCreateAndReason call.
		 */
		public CreateBinaryConfigurationsTask(DAGOptimizedMultiThread executor,
				Queue<DAGChaseConfiguration> leftSideConfigurations,
				Collection<DAGChaseConfiguration> rightSideConfigurations, Dependency[] inferredAccessibilityAxioms,
				DAGChaseConfiguration bestConfiguration, DAGEquivalenceClasses equivalenceClasses) {
					this.executor = executor;
					this.leftSideConfigurations = leftSideConfigurations;
					this.rightSideConfigurations = rightSideConfigurations;
					this.inferredAccessibilityAxioms = inferredAccessibilityAxioms;
					this.bestConfiguration = bestConfiguration;
					this.equivalenceClasses = equivalenceClasses;
					t = null;
		}
		public void run() {
			try {
				returnValue = executor.selectAndCreateBinaryConfigurationsToCreateAndReason(
						leftSideConfigurations, rightSideConfigurations, inferredAccessibilityAxioms, bestConfiguration, equivalenceClasses);
			} catch(Throwable t) {
				// store any exception to re throw it later when the thread is joined.
				this.t = t;
			}
			synchronized (this) {
				// mark it done, and notify anyone waiting for the results.
				finished = true;
				this.notifyAll();
			}
		}
		/**
		 * Waits until the results are generated. In case exception was thrown while executing the task this function will re-throw it.
		 * @return
		 * @throws PlannerException
		 */
		public Collection<DAGChaseConfiguration> getReturnValue() throws PlannerException {
			long start = System.currentTimeMillis();
			try {
				while (!finished && System.currentTimeMillis() - start < TIMEOUT ) { 
					synchronized (this) {
							this.wait(100);
					}
				}
			} catch (InterruptedException e) {
				// in case the thread is interrupted we ignore it, and return.
			}
			if (!finished) {
				throw new PlannerException("Worker thread read error (probably timeout) at createBinaryConfigurations. ");
			}
			if (t != null) {
				if (t instanceof PlannerException)
					throw (PlannerException)t;
				throw new PlannerException(t);
			}
			return returnValue;
		}
	}
}
