package uk.ac.ox.cs.pdq.planner.linear.pruning;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.accessible.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;

import com.google.common.base.Preconditions;

/**
 * Plan post pruning class
 *
 * @author Efthymia Tsamoura
 */
public abstract class PostPruning {

	/** Creates tree nodes */
	protected final NodeFactory nodeFactory;
	protected final AccessibleSchema accessibleSchema;
	/** True if the input path is pruned */
	protected Boolean isPruned = false;
	/** The pruned path and its corresponding plan */
	protected List<SearchNode> path = null;
	protected LeftDeepPlan plan = null;

	/**
	 * @param nodeFactory
	 * @param accessibleSchema
	 */
	public PostPruning(NodeFactory nodeFactory, AccessibleSchema accessibleSchema) {
		Preconditions.checkArgument(nodeFactory != null);
		Preconditions.checkArgument(accessibleSchema != null);
		this.nodeFactory = nodeFactory;
		this.accessibleSchema = accessibleSchema;
	}

	/**
	 * Post-prunes the input path and adds it to the input plan tree
	 * @param planTree
	 * @param path
	 * @param queryFacts facts of a query match
	 * @return true if the input path is postpruned
	 * @throws PlannerException
	 */
	public boolean prune(SearchNode root, List<SearchNode> path, Collection<Predicate> queryFacts) throws PlannerException, LimitReachedException {
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(queryFacts != null);
		this.isPruned = false;
		this.path = null;
		this.plan = null;
		Collection<Predicate> qF = new LinkedHashSet<>();
		for(Predicate queryFact: queryFacts) {
			if (queryFact.getSignature() instanceof InferredAccessibleRelation) {
				qF.add(queryFact);
			} else {
				Preconditions.checkState(queryFact.getSignature() instanceof AccessibleRelation);
			}
		}
		Collection<Predicate> factsToExpose = this.findFactsToExpose(path, qF);
		if(this.isPruned) {
			this.createPath(root, path, factsToExpose);
		}
		return this.isPruned;
	}

	/**
	 * @param candidates Exposed candidates
	 * @param minimalFacts
	 * @return the candidates that correspond to the input facts
	 */
	protected static Set<Candidate> getUtilisedCandidates(Collection<Candidate> candidates, Collection<Predicate> minimalFacts) {
		Set<Candidate> useful = new HashSet<>();
		for(Candidate candidate: candidates) {
			Predicate inferredAccessibleFact = candidate.getInferredAccessibleFact();
			if (minimalFacts.contains(inferredAccessibleFact)) {
				useful.add(candidate);
			}
		}
		return useful;
	}

	/**
	 * @param path
	 * 		A path that must be added to the plan tree
	 * 		If the root of this path is not the root of the plan tree, then an exception is thrown
	 * @param planTree DirectedGraph<N,DefaultEdge>
	 * @param parentNode N
	 */
	public void addPrunedPathToTree(PlanTree<SearchNode> planTree, SearchNode parentNode, List<SearchNode> path) {
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(!path.isEmpty());
		
		for(int j = 0; j < path.size(); ++j) {
			try {
				planTree.addVertex(path.get(j));
				if(j == 0) {
					planTree.addEdge(parentNode, path.get(j), new DefaultEdge());
				}
				else {
					planTree.addEdge(path.get(j-1), path.get(j), new DefaultEdge());
				}
			} catch(Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
	}
	
	public List<SearchNode> getPath() {
		return this.path;
	}
	
	public LeftDeepPlan getPlan() {
		return this.plan;
	}

	/**
	 * Method findFactsToExpose.
	 * @param path List<N>
	 * @param queryFacts Collection<PredicateFormula>
	 * @return Collection<PredicateFormula>
	 */
	protected abstract Collection<Predicate> findFactsToExpose(List<SearchNode> path, Collection<Predicate> queryFacts);
	
	/**
	 * Creates a post-pruned path given the input exposed facts
	 * @param path Input path
	 * @param factsToExpose The facts that will be exposed
	 * @throws PlannerException
	 */
	protected abstract void createPath(SearchNode root, List<SearchNode> path, Collection<Predicate> factsToExpose) throws PlannerException, LimitReachedException;
}
