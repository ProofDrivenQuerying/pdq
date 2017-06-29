package uk.ac.ox.cs.pdq.planner.linear.explorer.pruning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.planner.LimitReachedException;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.accessibleschema.InferredAccessibleAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.NodeFactory;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode.NodeStatus;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Removes the redundant accesses and the redundant follow-up joins from a successful configuration path.
 *
 * @author Efthymia Tsamoura
 */
public final class PostPruningRemoveFollowUps extends PostPruning {
	
	/** Runs the chase. Finds the consequences of newly created nodes **/
	private final Chaser chaser;
	
	/**  The input query*. */
	private final ConjunctiveQuery query;

	/**
	 * Instantiates a new post pruning remove follow ups.
	 *
	 * @param nodeFactory 		Factory of tree nodes
	 * @param accessibleSchema 		The accessible counterpart of the input schema
	 * @param chaser the chaser
	 * @param query the query
	 */
	public PostPruningRemoveFollowUps(NodeFactory nodeFactory, AccessibleSchema accessibleSchema, Chaser chaser, ConjunctiveQuery query) {
		super(nodeFactory, accessibleSchema);
		Preconditions.checkNotNull(chaser);
		Preconditions.checkNotNull(query);
		this.chaser = chaser;
		this.query = query;
	}

	/**
	 * Creates a post-pruned query path .
	 *
	 * @param root 		The root of the plan tree
	 * @param path 		The path that will be post-pruned 
	 * @param factsToExpose 		The facts that we will expose 
	 * @throws PlannerException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	@Override
	protected void createPath(SearchNode root, List<SearchNode> path, Collection<Atom> factsToExpose) throws PlannerException, LimitReachedException {
		if(this.isPruned) {
			this.path = new ArrayList<>();
			List<Integer> nodeIds = new ArrayList<>();

			for(int i = 0; i < path.size(); ++i) {
				SearchNode node = path.get(i);
				Preconditions.checkNotNull(node.getConfiguration().getExposedCandidates());
				Collection<Candidate> alreadyExposed = node.getConfiguration().getExposedCandidates();
				/*
				 * Get the candidate facts C of the current node that are part of the minimalFacts collection.
				 * The list C corresponds to the minimal set of candidate facts that must be exposed by this node in order to answer the query.
				 * If C is empty, then the current node has to be eliminated from the path
				 */
				Set<Candidate> mustBeExposed = getUtilisedCandidates(alreadyExposed, factsToExpose);
				if(!mustBeExposed.isEmpty()) {
					SearchNode freshNode;
					if(i > 0) {
						SearchNode parentNode = path.get(i-1);
						freshNode = this.nodeFactory.getInstance(parentNode, Sets.newHashSet(alreadyExposed));
					}
					else {
						freshNode = this.nodeFactory.getInstance(root, Sets.newHashSet(alreadyExposed));
					}
					freshNode.setStatus(NodeStatus.FAKE_TERMINAL);
					//Find the consequences of the newly created node
					freshNode.close(this.chaser, this.query, this.accessibleSchema.getAccessibilityAxioms());
					this.path.add(freshNode);
					nodeIds.add(freshNode.getId());
				}
			}

			int n;
			for(n = 0; n < this.path.size() - 1; ++n) {
				this.path.get(n).setPathToSuccess(Lists.<Integer>newArrayList(nodeIds.subList(n+1, nodeIds.size())));
			}
			this.path.get(n).setPathToSuccess(Lists.<Integer>newArrayList());
			this.path.get(n).setStatus(NodeStatus.SUCCESSFUL);
			List<Match> matches = this.path.get(n).matchesQuery(this.query);
			Preconditions.checkArgument(!matches.isEmpty());
			this.plan = this.path.get(n).getConfiguration().getPlan();
		}
	}

	/**
	 * Find facts to expose.
	 *
	 * @param path 		A successful path 
	 * @param queryFacts 		The facts in the query match 
	 * @return 		the facts that are sufficient to produce the input queryFacts
	 */
	@Override
	protected Collection<Atom> findFactsToExpose(List<SearchNode> path, Collection<Atom> queryFacts) {
		SearchNode successNode = path.get(path.size() - 1);
		// Find the minimal set of facts that have to be exposed in order to make each fact in the query match accessible
		Collection<Atom> minimalFacts = this.getMinimalFacts(queryFacts, successNode);

		// For each node in the path
		int i = 0;
		while(i < path.size()) {
			SearchNode node = path.get(i);
			Collection<Candidate> alreadyExposed = node.getConfiguration().getExposedCandidates();
			if(alreadyExposed != null) {
				/*
				 * Get the list of candidates C of the current node that are part of the minimalFacts collection.
				 * The list C corresponds to the minimal set of candidate facts that must be exposed in this node.
				 * If C is empty, then the current node has to be eliminated from the path
				 */
				Collection<Candidate> mustBeExposed = getUtilisedCandidates(alreadyExposed, minimalFacts);
				if (mustBeExposed.isEmpty() || !Sets.newHashSet(alreadyExposed).equals(Sets.newHashSet(mustBeExposed))) {
					this.isPruned = true;
					i = path.size();
					break;
				}
			}
			++i;
		}
		return minimalFacts;
	}

	/**
	 * Gets the minimal facts.
	 *
	 * @param queryMatch 		The facts of a query match
	 * @param successNode 		A node with configuration that matches the input query
	 * @return 		a minimal set of facts that have to be exposed in order to make each input fact accessible
	 */
	private Collection<Atom> getMinimalFacts(Collection<Atom> queryMatch, SearchNode successNode) {
		Preconditions.checkArgument(queryMatch != null);
		Preconditions.checkArgument(successNode != null);
		Preconditions.checkArgument(successNode.getStatus() == NodeStatus.SUCCESSFUL);
		Map<Atom, Pair<Dependency, Collection<Atom>>> factProvenance = successNode.getConfiguration().getState().getProvenance();
		Collection<Atom> minimalFacts = this.getMinimalFactsRecursive(queryMatch, Utility.getUntypedConstants(queryMatch), new LinkedHashSet<Constant>(), factProvenance);
		return minimalFacts;
	}

	/**
	 * Minimises the set of facts that have to be exposed in order to make each input fact accessible.
	 *
	 * @param facts 		A list of accessible, inferred accessible and accessed atoms
	 * @param inputTerms 		The input constants of the input atoms (i.e., the constants that are required to expose the input atoms)
	 * @param outputTerms 		The output constants of the input atoms
	 * @param factProvenance 		Mapping of atoms F to pairs of dependencies and atoms.
	 * 		Each pair corresponds to the dependency and the facts that were used to fire the former dependency and derive a fact in F.
	 * @return a (minimal) list of inferred accessible facts.
	 */
	private Collection<Atom> getMinimalFactsRecursive(
			Collection<Atom> facts,
			Collection<Constant> inputTerms,
			Collection<Constant> outputTerms,
			Map<Atom, Pair<Dependency, Collection<Atom>>> factProvenance) {
		// The minimal set of atoms
		Collection<Atom> parentFacts = new LinkedHashSet<>();
		// For each input fact
		for(Atom fact:facts) {
			// Get its provenance
			Pair<Dependency, Collection<Atom>> icToFacts = factProvenance.get(fact);
			if(icToFacts == null) {
				return new LinkedHashSet<>();
			}
			Dependency ic = icToFacts.getLeft();
			/*
			 * If the dependency (query) that leads to the derivation of the current atom is the inferred accessible version of a schema dependency
			 * then add the current atom in the output list of facts and continue the recursion with the facts that were used to fire the considered dependency.
			 * The recursion continues in order to reduce to the case where all of the returned inferred accessible facts come from accessibility
			 */
			if(ic instanceof InferredAccessibleAxiom) {
				parentFacts.addAll(this.getMinimalFactsRecursive(icToFacts.getRight(), inputTerms, outputTerms, factProvenance));
			}
			// If the dependency is an accessibility axiom
			else if(ic instanceof AccessibilityAxiom) {
				if(!outputTerms.containsAll(inputTerms)) {
					/*
					 * Find the accessible facts I that were used to fire the considered dependency (query),
					 * the accessible facts O that were derived from this firing and
					 * the inferred accessible fact that corresponds to the dependency's accessed fact.
					 * Update the list of input and output constants and continue the recursion for each accessible fact in I.
					 * The recursion continues in order to find the accesses that must be performed in order to expose the current fact
					 */
					Element elements = this.getElement(icToFacts.getRight());
					inputTerms.addAll(elements.getInputTerms());
					outputTerms.addAll(elements.getOutputTerms());
					outputTerms.removeAll(elements.getInputTerms());  // Julien-bugfix: if a terms previously thought to be accessible turns out to be a required input earlier in the plan, remove it from the output.
					parentFacts.add(elements.getInferredAccessibleFact());
					for(Atom accessibleFact:elements.getInputAccessibleFacts()) {
						parentFacts.addAll(this.getMinimalFactsRecursive(Sets.newLinkedHashSet(Sets.newHashSet(accessibleFact)), inputTerms, outputTerms, factProvenance));
					}
				}
			}
		}
		return parentFacts;
	}

	/**
	 * Gets the element.
	 *
	 * @param facts Facts that were used to fire an accessibility axiom
	 * @return A structure of
	 * 				the accessible facts that were used to fire an accessibility axiom
	 * 				the accessible facts O that were derived from this firing and
	 * 				the inferred accessible fact that corresponds to the axiom's accessed fact.
	 */
	private Element getElement(Collection<Atom> facts) {
		Collection<Constant> inputTerms = new LinkedHashSet<>();
		Collection<Constant> outputTerms = new LinkedHashSet<>();
		Collection<Atom> inputAccessibleFacts = new LinkedHashSet<>();
		Atom inferredAccessibleFact = null;
		for(Atom fact:facts) {
			if(fact.getPredicate() instanceof AccessibleRelation) {
				Set<Constant> constants = Utility.getUntypedConstants(fact);
				if(!constants.isEmpty()) {
					inputTerms.addAll(constants);
					inputAccessibleFacts.add(fact);
				}
			}
			else if(fact.getPredicate() instanceof Relation) {
				outputTerms.addAll(Utility.getTypedAndUntypedConstants(fact));
				Relation relation = (Relation) fact.getPredicate();
				inferredAccessibleFact = new Atom(this.accessibleSchema.getInferredAccessibleRelation(relation), fact.getTerms() );
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		outputTerms.removeAll(inputTerms);
		return new Element(inputTerms, outputTerms, inputAccessibleFacts, inferredAccessibleFact);
	}

	/**
	 * The Class Element.
	 */
	private class Element {
		
		/** The input terms. */
		private final Collection<Constant> inputTerms;
		
		/** The output terms. */
		private final Collection<Constant> outputTerms;
		
		/** The input accessible facts. */
		private final Collection<Atom> inputAccessibleFacts;
		
		/** The inferred accessible fact. */
		private final Atom inferredAccessibleFact;

		/**
		 * Constructor for Element.
		 * @param inputTerms Collection<Constant>
		 * @param outputTerms Collection<Constant>
		 * @param accessibleFacts Collection<PredicateFormula>
		 * @param inferredAccessibleFact PredicateFormula
		 */
		public Element(Collection<Constant> inputTerms,
				Collection<Constant> outputTerms,
				Collection<Atom> accessibleFacts,
				Atom inferredAccessibleFact) {
			Preconditions.checkArgument(inputTerms != null);
			Preconditions.checkArgument(outputTerms != null);
			Preconditions.checkArgument(accessibleFacts != null);
			Preconditions.checkArgument(inferredAccessibleFact != null);
			this.inputTerms = inputTerms;
			this.outputTerms = outputTerms;
			this.inputAccessibleFacts = accessibleFacts;
			this.inferredAccessibleFact = inferredAccessibleFact;
		}

		/**
		 * Gets the input terms.
		 *
		 * @return Collection<Constant>
		 */
		public Collection<Constant> getInputTerms() {
			return this.inputTerms;
		}

		/**
		 * Gets the output terms.
		 *
		 * @return Collection<Constant>
		 */
		public Collection<Constant> getOutputTerms() {
			return this.outputTerms;
		}

		/**
		 * Gets the input accessible facts.
		 *
		 * @return Collection<PredicateFormula>
		 */
		public Collection<Atom> getInputAccessibleFacts() {
			return this.inputAccessibleFacts;
		}

		/**
		 * Gets the inferred accessible fact.
		 *
		 * @return PredicateFormula
		 */
		public Atom getInferredAccessibleFact() {
			return this.inferredAccessibleFact;
		}

		/**
		 * To string.
		 *
		 * @return String
		 */
		@Override
		public String toString() {
			return "Inputs " + this.inputTerms.toString() + "\n" +
					"Outputs " + this.outputTerms.toString() + "\n" +
					"Accessible facts " + this.inputAccessibleFacts.toString()  + "\n" +
					"Inferred Accessible facts " + this.inferredAccessibleFact.toString()  + "\n";
		}
	}
}
