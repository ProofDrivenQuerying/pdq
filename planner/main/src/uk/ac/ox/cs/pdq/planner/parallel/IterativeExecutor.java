package uk.ac.ox.cs.pdq.planner.parallel;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.dag.explorer.Template;
import uk.ac.ox.cs.pdq.planner.dag.priority.PriorityAssessor;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.SuccessDominance;

/**
 * Provides functions to create binary configurations or to
 * identify the minimum-cost configuration among a given set of configurations
 *
 * @author Efthymia Tsamoura
 */
public abstract class IterativeExecutor {

	private final Context context;

	/**
	 * Constructor for IterativeExecutor.
	 * @param context Context
	 */
	public IterativeExecutor(Context context) {
		this.context = context;
	}

	/**
	 * @return Context
	 */
	public Context getContext() {
		return this.context;
	}
	
	/**
	 * Creates new binary configurations by combining configurations from the input left and right collections.
	 * If twoWay=TRUE the output configurations are of the form BinaryConfiguration(L,R) and BinaryConfiguration(R,L), where L belongs to the
	 * left input collection and R to the right input collection, respectively.
	 * Otherwise, they are of the form  BinaryConfiguration(L,R)
	 * @param depth
	 * 		The depth of the output configurations
	 * @param left
	 * 		The configurations to consider on the left
	 * @param right
	 * 		The configurations to consider on the right
	 * @param priority
	 * 		Prioritises pairs of configurations
	 * @param templates
	 * 		Maps each configuration to its constituting ApplyRule configurations. Used to speed up chasing, i.e.,
	 * 		when we are about to create a new binary configuration c''= BinaryConfiguration(c,c')
	 * 		from c and c' and there exists another configuration c^(3) with ApplyRules
	 * 		the ApplyRules of c and c' and c^(3) is already chased then we use c^(3)'s state as the state of c''
	 * @param bestConfiguration
	 * 	 	The minimum cost closed and successful configuration found so far. The plans that correspond to the
	 * 		returned configurations have cost < the bestConfiguration
	 * @param twoWay
	 * @param timeout
	 * @param unit
	 * @return Collection<DAGChaseConfiguration>
	 * @throws PlannerException
	 */
	public abstract Collection<DAGChaseConfiguration> chaseOrPropagate(
			int depth,
			Queue<DAGChaseConfiguration> left,
			Collection<DAGChaseConfiguration> right,
			PriorityAssessor priority,
			Template templates,
			DAGChaseConfiguration bestConfiguration,
			boolean twoWay,
			long timeout, TimeUnit unit) throws PlannerException, LimitReachedException;

	/**
	 * Iterates over the input collection of configurations to identify the minimum-cost one
	 * @param configurations
	 * 		The input set of configurations
	 * @param equivalenceClasses
	 * 		Classes of structurally equivalent configurations
	 * @param bestConfiguration
	 * 		The minimum cost closed and successful configuration found so far. The plans that correspond to the
	 * 		returned configurations have cost < the bestConfiguration
	 * @param successDominance
	 * 		Performs success dominance checks
	 * @param timeout
	 * @param unit
	 * @return
	 * 		the non-dominated configurations (that could lead to the minimum-cost configuration),
	 * 		the minimum-cost configuration with cost < the cost of the input bestConfiguration and the successful configurations
	 * @throws PlannerException
	 */
	public abstract FinalIterationThreadResults finalIteration(
			Queue<DAGChaseConfiguration> configurations,
			DAGEquivalenceClasses equivalenceClasses,
			DAGChaseConfiguration bestConfiguration,
			SuccessDominance successDominance,
			long timeout, TimeUnit unit) throws PlannerException, LimitReachedException;
}
