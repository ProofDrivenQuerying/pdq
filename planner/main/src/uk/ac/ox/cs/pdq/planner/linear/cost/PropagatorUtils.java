package uk.ac.ox.cs.pdq.planner.linear.cost;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.algebra.Operators;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.cost.estimators.BlackBoxCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.linear.LinearPlanGenerator;
import uk.ac.ox.cs.pdq.planner.linear.LinearUtility;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.planner.reasoning.Proof;
import uk.ac.ox.cs.pdq.util.IndexedDirectedGraph;

import com.google.common.base.Preconditions;

/**
 */
public class PropagatorUtils {
	
	public static CostPropagator getPropagator(CostEstimator<LeftDeepPlan> e) {
		if (e instanceof BlackBoxCostEstimator) {
			return new BlackBoxPropagator((BlackBoxCostEstimator) e);
		}
		if (e instanceof SimpleCostEstimator) {
			return new SimplePropagator((SimpleCostEstimator) e);
		}
		throw new IllegalStateException("Attempting to get a propagator for a cost estimator that is neither blackbox nor simple");
	}
	

	/**
	 * @param nodesSet
	 *            The nodes of the plan tree
	 * @param path
	 *            A successful path (sequence of nodes). The corresponding nodes must
	 *            correspond to a successful path (a path from the root to a
	 *            success node)
	 * @param costEstimator CostEstimator<LeftDeepPlan>
	 * @return a linear plan that corresponds to the input path to success
	 */
	public static <T extends SearchNode> LeftDeepPlan createLinearPlan(IndexedDirectedGraph<T> nodesSet, List<Integer> path, CostEstimator<LeftDeepPlan> costEstimator, boolean projection) {
		Preconditions.checkArgument(path != null && !path.isEmpty());
		List<T> nodes = LinearUtility.createPath(nodesSet, path);
		LeftDeepPlan plan = LinearPlanGenerator.createLinearPlan(nodes);
		T snode = nodes.get(nodes.size()-1);
		if(snode.getStatus().equals(NodeStatus.SUCCESSFUL) && projection) {
			Projection project = Operators.createFinalProjection(snode.getConfiguration().getQuery(), plan.getOperator());
			plan = plan.projectLast(project);
		}
		costEstimator.cost(plan);

		for (T next:nodes) {
			costEstimator.cost(next.getConfiguration().getPlan());
		}
		return plan;
	}

	/**
	 *
	 * @param nodesSet
	 *            The nodes of the plan tree
	 * @param path
	 *            A successful path (sequence of nodes). The corresponding nodes must
	 *            correspond to a successful path (a path from the root to a
	 *            success node)
	 *
	 * @return a proof. The proof is created using the input nodes and the
	 *         uk.ac.ox.cs.pdq.builder.Builder class. Exceptions are thrown in
	 *         the following cases: -an input node cannot be found in the
	 *         nodesSet argument -the final node in the sequence has an empty
	 *         query match (@see uk.ac.ox.cs.pdq.explorer.SearchNode)
	 */
	public static <T extends SearchNode> Proof createProof(IndexedDirectedGraph<T> nodesSet, List<Integer> path) {
		Preconditions.checkArgument(path != null && !path.isEmpty());
		List<T> nodes = LinearUtility.createPath(nodesSet, path);
		Proof.Builder builder = Proof.builder();
		for (SearchNode node:nodes) {
			Collection<Candidate> exposedCandidates = node.getConfiguration().getExposedCandidates();
			if (exposedCandidates != null) {
				Candidate first = exposedCandidates.iterator().next();
				Preconditions.checkState(first.getMatch().getQuery() instanceof AccessibilityAxiom);
				builder.addAxiom((AccessibilityAxiom) first.getMatch().getQuery());
				for (Candidate candidate: exposedCandidates) {
					builder.addMatch(candidate.getMatch().getMapping());
				}
			}
		}
		T snode = nodes.get(nodes.size() - 1);
		Preconditions.checkNotNull(snode.getConfiguration().getProof().getQueryMatch());
		builder.setQueryMatch(snode.getConfiguration().getProof().getQueryMatch());
		return builder.build();
	}
}