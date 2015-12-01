package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.planner.equivalence.FactEquivalence;
import uk.ac.ox.cs.pdq.planner.equivalence.FastFactEquivalence;
import uk.ac.ox.cs.pdq.planner.linear.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode.NodeStatus;

import com.google.common.base.Preconditions;


/**
 * Utility class
 * @author Efthymia Tsamoura
 *
 */

public class ExplorerUtils {

	/**
	 * @param parentsNodes
	 * @param childNode
	 * @return a parent node that is globally equivalent to the input node
	 */
	public static <N extends SearchNode> N isEquivalent(Collection<N> parentsNodes, N childNode) {
		FactEquivalence factEquivalence = new FastFactEquivalence();
		N equivalent = null;
		Preconditions.checkArgument(childNode.isFullyGenerated());
		for (N parentNode: parentsNodes) {
			if (!parentNode.equals(childNode)
					&& parentNode.isFullyGenerated()
					&& parentNode.getPointer() == null
					&& !parentNode.getStatus().equals(NodeStatus.TERMINAL)
					&& !parentNode.getStatus().equals(NodeStatus.FAKE_TERMINAL)
					&& factEquivalence.isEquivalent(childNode.getConfiguration(), parentNode.getConfiguration())) {
				if (equivalent == null) {
					equivalent = parentNode;
				} else {
					if (parentNode.getId() < equivalent.getId()) {
						equivalent = parentNode;
					}
				}
			}
		}
		return equivalent;
	}

	/**
	 * @param parentsNodes
	 * @param childNode
	 * @return a parent node that dominates the child
	 */
	public static <N extends SearchNode> N isDominated(Collection<N> parentsNodes, N childNode) {
		for (N parentNode: parentsNodes) {
			if (!parentNode.equals(childNode)
					&& !parentNode.getStatus().equals(NodeStatus.FAKE_TERMINAL)
					&& childNode.isDominatedBy(parentNode)) {
				return parentNode;
			}
		}
		return null;
	}

	/**
	 * @param planTree
	 * @return the fully generate nodes
	 */
	public static <N extends SearchNode> Collection<N> getFullyGeneratedNodes(DirectedGraph<N, DefaultEdge> planTree) {
		Collection<N> fullyGenerated = new LinkedHashSet<>();
		for (N node: planTree.vertexSet()) {
			if (node.isFullyGenerated()) {
				fullyGenerated.add(node);
			}
		}
		return fullyGenerated;
	}

	/**
	 * @param planTree
	 * @return the partially generated leaves
	 */
	public static <N extends SearchNode> Collection<N> getPartiallyGeneratedLeaves(DirectedGraph<N, DefaultEdge> planTree) {
		Collection<N> partiallyGenerated = new LinkedHashSet<>();
		for (N node:planTree.vertexSet()) {
			if (planTree.outDegreeOf(node) == 0 && !node.isFullyGenerated()) {
				partiallyGenerated.add(node);
			}
		}
		return partiallyGenerated;
	}

	/**
	 * Julien: Quick fix to avoid cycles in propagation.
	 * @param planTree PlanTree<N>
	 * @param node N
	 * @return a collection of all node in the search space after removing the
	 * ancestors of snode
	 */
	public static <N extends SearchNode> Collection<N> allButAncestorsOf(PlanTree<N> planTree, N node) {
		Collection<N> result = new LinkedHashSet<>(planTree.vertexSet());
		result.removeAll(ancestorsOf(planTree, node));
		return result;
	}

	/**
	 * Julien: Quick fix to avoid cycles in propagation.
	 * @return a collection of the ancestors of snode
	 */
	public static <N extends SearchNode> Collection<N> ancestorsOf(PlanTree<N> planTree, N node) {
		Collection<N> result = new LinkedHashSet<>();
		for (DefaultEdge n:planTree.incomingEdgesOf(node)) {
			N sourceNode = planTree.getEdgeSource(n);
			if (!result.contains(sourceNode)) {
				result.addAll(ancestorsOf(planTree, sourceNode));
			}
			result.add(sourceNode);
		}
		return result;
	}


}
