package uk.ac.ox.cs.pdq.planner.linear.explorer.pruning;

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
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.PlanTree;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;

import com.google.common.base.Preconditions;

/**
 * Removes the redundant accesses and the redundant follow-up joins from a successful configuration path
 *
 * @author Efthymia Tsamoura
 */
public abstract class PostPruning {

	/** Factory of tree nodes */
	protected final NodeFactory nodeFactory;
	/** The accessible counterpart of the input schema **/
	protected final AccessibleSchema accessibleSchema;
	/** True if the input path is pruned */
	protected Boolean isPruned = false;
	/** The pruned path and its corresponding plan */
	protected List<SearchNode> path = null;
	protected LeftDeepPlan plan = null;

	/**
	 * @param nodeFactory
	 * 		Factory of tree nodes
	 * @param accessibleSchema
	 * 		The accessible counterpart of the input schema
	 */
	public PostPruning(NodeFactory nodeFactory, AccessibleSchema accessibleSchema) {
		Preconditions.checkArgument(nodeFactory != null);
		Preconditions.checkArgument(accessibleSchema != null);
		this.nodeFactory = nodeFactory;
		this.accessibleSchema = accessibleSchema;
	}

	
	/**
	 * Post-prunes the input nodes path
	 * @param root
	 * 		The root of the linear path tree
	 * @param path
	 * 		The path of nodes to be prostpruned 
	 * @param queryFacts
	 * 		The facts of the query match
	 * @return
	 * @throws PlannerException
	 * @throws LimitReachedException
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
	 * @param candidates 
	 * @param minimalFacts
	 * @return the candidates that produced the input facts
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
	 * 
	 * @param planTree
	 * 		The input tree of paths
	 * @param parentNode
	 * 		The node below which we will add the input path
	 * @param path
	 * 		The path to the add to the input tree
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
	 * 
	 * @param path
	 * 		A successful path 
	 * @param queryFacts
	 * 		The facts in the query match 
	 * @return
	 * 		the facts that are sufficient to produce the input queryFacts
	 */
	protected abstract Collection<Predicate> findFactsToExpose(List<SearchNode> path, Collection<Predicate> queryFacts);
	
	/**
	 * Creates a post-pruned query path 
	 * @param root
	 * 		The root of the plan tree
	 * @param path
	 * 		The path that will be post-pruned 
	 * @param factsToExpose
	 * 		The facts that we will expose 
	 * @throws PlannerException
	 * @throws LimitReachedException
	 */
	protected abstract void createPath(SearchNode root, List<SearchNode> path, Collection<Predicate> factsToExpose) throws PlannerException, LimitReachedException;
}
