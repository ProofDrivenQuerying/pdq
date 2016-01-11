package uk.ac.ox.cs.pdq.planner.dag.explorer.parallel;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGAnnotatedPlanClasses;

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
	 * @param best
	 * 	 	The minimum cost closed and successful configuration found so far. The plans that correspond to the
	 * 		returned configurations have cost < the bestConfiguration
	 * @param twoWay
	 * @param timeout
	 * @param unit
	 * @return Collection<DAGAnnotatedPlan>
	 * @throws PlannerException
	 */
	public abstract Collection<DAGAnnotatedPlan> reason(
			int depth,
			Queue<DAGAnnotatedPlan> left,
			Collection<DAGAnnotatedPlan> right,
			Query<?> query,
			Schema schema,
			DAGAnnotatedPlan best,
			DAGAnnotatedPlanClasses classes, 
			boolean twoWay,
			long timeout, TimeUnit unit) throws PlannerException, LimitReachedException;

	/**
	 * Iterates over the input collection of configurations to identify the minimum-cost one
	 * @param input
	 * 		The input set of configurations
	 * @param classes
	 * 		Classes of structurally equivalent configurations
	 * @param best
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
	public abstract ExplorationThreadResults explore(
			Query<?> query,
			Queue<DAGAnnotatedPlan> input,
			DAGAnnotatedPlanClasses classes,
			DAGAnnotatedPlan best,
			long timeout, TimeUnit unit) throws PlannerException, LimitReachedException;
}
