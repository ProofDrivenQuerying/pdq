package uk.ac.ox.cs.pdq.planner.linear.explorer.pruning;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.util.PlanTree;
import uk.ac.ox.cs.pdq.util.LimitReachedException;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Removes the redundant accesses and the redundant follow-up joins from a successful configuration path.
 *
 * @author Efthymia Tsamoura
 */
public abstract class PostPruning {

	/**  Factory of tree nodes. */
	protected final NodeFactory nodeFactory;
	
	/**  The accessible counterpart of the input schema *. */
	protected final AccessibleSchema accessibleSchema;
	
	/**  True if the input path is pruned. */
	protected Boolean isPruned = false;
	
	/**  The pruned path and its corresponding plan. */
	protected List<SearchNode> path = null;
	
	/** The plan. */
	protected RelationalTerm plan = null;

	/**
	 * Instantiates a new post pruning.
	 *
	 * @param nodeFactory 		Factory of tree nodes
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 */
	public PostPruning(NodeFactory nodeFactory, AccessibleSchema accessibleSchema) {
		Preconditions.checkArgument(nodeFactory != null);
		Preconditions.checkArgument(accessibleSchema != null);
		this.nodeFactory = nodeFactory;
		this.accessibleSchema = accessibleSchema;
	}

	
	/**
	 * Post-prunes the input nodes path.
	 *
	 * @param root 		The root of the linear path tree
	 * @param path 		The path of nodes to be prostpruned 
	 * @param queryFacts 		The facts of the query match
	 * @return true, if successful
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	public boolean prune(SearchNode root, List<SearchNode> path, Atom[] queryFacts) throws PlannerException, LimitReachedException {
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(queryFacts != null);
		this.isPruned = false;
		this.path = null;
		this.plan = null;
		Collection<Atom> qF = new LinkedHashSet<>();
		for(Atom queryFact: queryFacts) {
			if (queryFact.getPredicate().getName().startsWith(AccessibleSchema.inferredAccessiblePrefix)) 
				qF.add(queryFact);
			else 
				Preconditions.checkState(queryFact.getPredicate().equals(AccessibleSchema.accessibleRelation));
		}
		Collection<Atom> factsToExpose = this.findFactsToExpose(path, qF);
		if(this.isPruned) 
			this.createPath(root, path, factsToExpose);
		return this.isPruned;
	}

	/**
	 * Gets the utilised candidates.
	 *
	 * @param candidates the candidates
	 * @param minimalFacts the minimal facts
	 * @return the candidates that produced the input facts
	 */
	protected static Set<Candidate> getUtilisedCandidates(Collection<Candidate> candidates, Collection<Atom> minimalFacts) {
		Set<Candidate> useful = new HashSet<>();
		for(Candidate candidate: candidates) {
			Atom inferredAccessibleFact = candidate.getInferredAccessibleFact();
			if (minimalFacts.contains(inferredAccessibleFact)) {
				useful.add(candidate);
			}
		}
		return useful;
	}

	
	/**
	 * Adds the pruned path to tree.
	 *
	 * @param planTree 		The input tree of paths
	 * @param parentNode 		The node below which we will add the input path
	 * @param path 		The path to the add to the input tree
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
	
	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public List<SearchNode> getPath() {
		return this.path;
	}
	
	/**
	 * Gets the plan.
	 *
	 * @return the plan
	 */
	public RelationalTerm getPlan() {
		return this.plan;
	}

	/**
	 * Find facts to expose.
	 *
	 * @param path 		A successful path 
	 * @param queryFacts 		The facts in the query match 
	 * @return 		the facts that are sufficient to produce the input queryFacts
	 */
	protected abstract Collection<Atom> findFactsToExpose(List<SearchNode> path, Collection<Atom> queryFacts);
	
	/**
	 * Creates a post-pruned query path .
	 *
	 * @param root 		The root of the plan tree
	 * @param path 		The path that will be post-pruned 
	 * @param factsToExpose 		The facts that we will expose 
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	protected abstract void createPath(SearchNode root, List<SearchNode> path, Collection<Atom> factsToExpose) throws PlannerException, LimitReachedException;
}
