package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.List;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.planner.linear.LeftDeepPlanGenerator;
import uk.ac.ox.cs.pdq.planner.linear.LinearUtility;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.util.IndexedDirectedGraph;


// TODO: Auto-generated Javadoc
/**
 * The Class PropagatorUtils.
 *
 * @author Efthymia Tsamoura
 */
public class CostPropagatorUtility {
	/**
	 * Creates the left deep plan.
	 *
	 * @param <T> the generic type
	 * @param nodesSet            The nodes of the plan tree
	 * @param path            A successful path (sequence of nodes). The corresponding nodes must
	 *            correspond to a successful path (a path from the root to a
	 *            success node)
	 * @param costEstimator CostEstimator<LeftDeepPlan>
	 * @return a linear plan that corresponds to the input path to success
	 */
	public static <T extends SearchNode> RelationalTerm createLeftDeepPlan(IndexedDirectedGraph<T> nodesSet, List<Integer> path) {
		Preconditions.checkArgument(path != null && !path.isEmpty());
		List<T> nodes = LinearUtility.createPath(nodesSet, path);
		RelationalTerm plan = LeftDeepPlanGenerator.createLeftDeepPlan(nodes);
		return plan;
	}
}