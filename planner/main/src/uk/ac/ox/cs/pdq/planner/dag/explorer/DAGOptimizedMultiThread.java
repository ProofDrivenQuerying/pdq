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
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

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
	private final Queue<Runnable> postprocessQueue;
	private List<ThreadPoolWorker> createPool;
	private List<ThreadPoolWorker> postProcessQueue;
	
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
			ConjunctiveQuery accessibleQuery, AccessibleSchema accessibleSchema, Chaser chaser,
			DatabaseManager connection, CostEstimator costEstimator, Filter filter, int maxDepth)
			throws PlannerException, SQLException {
		super(eventBus, parameters, query, accessibleQuery, accessibleSchema, chaser, connection, costEstimator, filter, maxDepth);
		this.createQueue = new ConcurrentLinkedQueue<>();
		this.postprocessQueue = new ConcurrentLinkedQueue<>();
		createPool = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			createPool.add(new ThreadPoolWorker(createQueue,"CreatePoolThread"+i));
		}
		postProcessQueue = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			postProcessQueue.add(new ThreadPoolWorker(postprocessQueue,"PostProcessPoolThread"+i));
		}
	}
	public void shutdownThreads() {
		if (createPool != null) for (ThreadPoolWorker t: createPool) t.setShutdown(true);
		if (postProcessQueue != null) for (ThreadPoolWorker t: postProcessQueue) t.setShutdown(true);
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
			this.checkLimitReached();
			List<CreateBinaryConfigurationsTask> currentTasks = new ArrayList<>();
			Queue<DAGChaseConfiguration> leftCopy = new ConcurrentLinkedQueue<>();
			leftCopy.addAll(this.leftSideConfigurations);
			final int STEP = 5;
			ArrayList<DAGChaseConfiguration> right = new ArrayList<>();
			right.addAll(this.equivalenceClasses.getConfigurations());
			for (int i = 0; i < right.size(); i += STEP) {
				Collection<DAGChaseConfiguration> rightSTEP = new ArrayList<>();
				for (int j = i; j< i+STEP; j++) {
					if (j < right.size())
						rightSTEP.add(right.get(j));
				}
				CreateBinaryConfigurationsTask a = new CreateBinaryConfigurationsTask(this,
						new ConcurrentLinkedQueue<>(leftCopy), new ConcurrentLinkedQueue<>(rightSTEP),
						this.accessibleSchema.getInferredAccessibilityAxioms(), this.bestConfiguration,
						this.equivalenceClasses);
				CreateBinaryConfigurationsTask b = new CreateBinaryConfigurationsTask(this,
						new ConcurrentLinkedQueue<>(rightSTEP), new ConcurrentLinkedQueue<>(leftCopy), 
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
						synchronized (queue) {
							try {
								queue.wait(100);
							} catch (InterruptedException e) {
							}
						}
					} else {
						task.run();
					}
						
				}
			} catch(Throwable t) {
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
		private Queue<DAGChaseConfiguration> leftSideConfigurations2;
		private Collection<DAGChaseConfiguration> rightSideConfigurations;
		private Dependency[] inferredAccessibilityAxioms;
		private DAGChaseConfiguration bestConfiguration2;
		private DAGEquivalenceClasses equivalenceClasses2;
		private Collection<DAGChaseConfiguration> returnValue;
		private Throwable t;
		volatile private boolean finished = false;
		public CreateBinaryConfigurationsTask(DAGOptimizedMultiThread executor,
				Queue<DAGChaseConfiguration> leftSideConfigurations,
				Collection<DAGChaseConfiguration> rightSideConfigurations, Dependency[] inferredAccessibilityAxioms,
				DAGChaseConfiguration bestConfiguration, DAGEquivalenceClasses equivalenceClasses2) {
					this.executor = executor;
					leftSideConfigurations2 = leftSideConfigurations;
					this.rightSideConfigurations = rightSideConfigurations;
					this.inferredAccessibilityAxioms = inferredAccessibilityAxioms;
					bestConfiguration2 = bestConfiguration;
					this.equivalenceClasses2 = equivalenceClasses2;
					t = null;
		}
		public void run() {
			try {
				returnValue = executor.selectAndCreateBinaryConfigurationsToCreateAndReason(leftSideConfigurations2, rightSideConfigurations, inferredAccessibilityAxioms, bestConfiguration2, equivalenceClasses2);
			} catch(Throwable t) {
				this.t = t;
			}
			synchronized (this) {
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
			while (!finished && System.currentTimeMillis() - start < 1000*60*2 ) { // 1 min
				synchronized (this) {
					try {
						this.wait(100);
					} catch (InterruptedException e) {
					}
				}
			}
			if (!finished) {
				throw new PlannerException("Worker thread read error at createBinaryConfigurations. ");
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
