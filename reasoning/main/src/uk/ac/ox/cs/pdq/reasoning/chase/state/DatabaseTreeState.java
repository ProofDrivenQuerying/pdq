package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.Bag;
import uk.ac.ox.cs.pdq.reasoning.chase.Bag.BagStatus;
import uk.ac.ox.cs.pdq.reasoning.chase.BagBoundPredicate;
import uk.ac.ox.cs.pdq.reasoning.chase.BagMatch;
import uk.ac.ox.cs.pdq.reasoning.chase.BagsTree;
import uk.ac.ox.cs.pdq.reasoning.chase.BagsTreeEdge;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.chase.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * The facts of this chase state are organised into a tree of bags. This type of
 * state is used in blocking chase implementations.
 * It keeps the facts of this state in a database.
 * @author Efthymia Tsamoura
 *
 */
public class DatabaseTreeState extends DatabaseChaseState implements TreeState{

	protected static Logger log = Logger.getLogger(DatabaseTreeState.class);
	
	protected final Query<?> query;
		
	/** True if Skolem terms are assigned to existentially quantified variables*/
	protected final boolean canonicalNames = true;
	
	/** A tree of bags */
	protected BagsTree tree;
	
	/** The firings that took place in this state*/
	protected FiringGraph graph;
	
	/** The chase constants of this state */
	protected Collection<Term> terms;
	
	/** The bags that we have to update with new facts */
	protected Collection<Bag> toUpdate = new LinkedHashSet<>();
	
	/**
	 * 
	 * @param query
	 * @param manager
	 */
	public DatabaseTreeState(Query<?> query, DBHomomorphismManager manager) {
		this(query, manager, BagsTree.initialiseTree(query.getCanonical().getPredicates()), 
				inferTerms(query.getCanonical().getPredicates()), new MapFiringGraph());
		this.manager.addFacts(this.tree.getFacts());
	}
	
	private static Collection<Term> inferTerms(Collection<Predicate> facts) {
		Collection<Term> terms = new LinkedHashSet<>();
		for(Predicate fact:facts) {
			terms.addAll(fact.getTerms());
		}
		return terms;
	}
	
	/**
	 * 
	 * @param query
	 * @param manager
	 * @param tree
	 * @param graph
	 */
	protected DatabaseTreeState(Query<?> query,
			DBHomomorphismManager manager,
			BagsTree tree,
			Collection<Term> terms,
			FiringGraph graph
			) {
		super(manager);
		Preconditions.checkNotNull(tree);
		Preconditions.checkNotNull(graph);
		this.query = query;
		this.terms = terms;
		this.tree = tree;
		this.graph = graph;
	}


	/**
	 * Calls the manager to detect matches of the
	 * left-hand side conjunction of the input dependency to the facts in this.state.
	 * The manager detects homomorphisms using a database backend.
	 * We search for homomorphisms only in non-blocked bags.
	 * The match found must not be constraint in a single bag.
	 * @param dependency Constraint
	 * @param constraints HomomorphismConstraint...
	 * @return List<Match>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getMaches(Constraint,HomomorphismConstraint...)
	 */
	@Override
	public List<Match> getMaches(Constraint dependency, HomomorphismConstraint... constraints) {
		HomomorphismConstraint[] c = new HomomorphismConstraint[constraints.length+2];
		System.arraycopy(constraints, 0, c, 0, constraints.length);
		c[constraints.length] = HomomorphismConstraint.factScope(Conjunction.of(this.getFacts()));
		c[constraints.length+1] = HomomorphismConstraint.bagScope(true, this.tree.getBags(BagStatus.NONBLOCKED));
		return this.getManager().getMatches(dependency, c);
	}

	/**
	 * @param dependencies Collection<D>
	 * @param constraints HomomorphismConstraint...
	 * @return Map<D,List<Match>>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getHomomorphisms(Collection<D>,HomomorphismConstraint...)
	 */
	@Override
	public List<Match> getMaches(Collection<? extends Constraint> dependencies, HomomorphismConstraint... constraints) {
		HomomorphismConstraint[] c = new HomomorphismConstraint[constraints.length+2];
		System.arraycopy(constraints, 0, c, 0, constraints.length);
		c[constraints.length] = HomomorphismConstraint.factScope(Conjunction.of(this.getFacts()));
		c[constraints.length+1] = HomomorphismConstraint.bagScope(true, this.tree.getBags(BagStatus.NONBLOCKED));
		return this.getManager().getMatches(dependencies, c);
	}

	/**
	 * Calls the manager to detect matches of the accessible query to facts.
	 * The manager detects homomorphisms using a database backend.
	 * @param query Query
	 * @param constraints Map<Variable,Term>
	 * @return List<Match>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#matchesQuery(Query, Map<Variable,Term>)
	 */
	@Override
	public List<Match> getMatches(Query<?> query) {
		return this.getManager().getMatches(
				query,
				HomomorphismConstraint.topK(1),
				HomomorphismConstraint.factScope(Conjunction.of(this.getFacts())),
				HomomorphismConstraint.bagScope(false, this.tree.getBags(null)),
				HomomorphismConstraint.satisfies(query.getFree2Canonical()));
	}

	/**
	 * @param facts Collection<PredicateFormula>
	 * @return Collection<BagBoundPredicate>
	 */
	protected Collection<BagBoundPredicate> toPropagate(Collection<Predicate> facts) {
		Collection<BagBoundPredicate> propagatedFacts = new ArrayList<>();
		for (Predicate fact: facts) {
			for (Bag bag:this.tree.vertexSet()) {
				if (bag.containsTerms(fact.getTerms()) && !bag.getFacts().contains(fact)) {
					propagatedFacts.add(new BagBoundPredicate(fact, bag.getId()));
					bag.addFacts(fact);
				}
			}
		}
		return propagatedFacts;
	}

	/**
	 *
	 * @param parentBag
	 * 			The bag over which the input dependency has been fired
	 * @param dependency
	 * 			The input dependency
	 * @param left
	 * 			The grounded left-hand side of the input dependency
	 * @param right
	 * 			The grounded right-hand side of the input dependency
	 * @return
	 * 			a mapping of atoms to the bags the latter should be propagated to
	 */
	public Collection<BagBoundPredicate> updateBags(Bag parentBag, Constraint dependency, Formula left, Formula right) {
		if(parentBag == null) {
			System.out.println();
		}
		Preconditions.checkNotNull(parentBag);
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		Preconditions.checkNotNull(dependency);
		Collection<BagBoundPredicate> propagatedFacts = this.toPropagate(right.getPredicates());
		if (this.freshTermsExist(right)) {
			Bag childBag = this.tree.getBagFactory().createBag(right.getPredicates());
			this.tree.addVertex(childBag);
			this.tree.addEdge(parentBag, childBag, new BagsTreeEdge(parentBag, childBag, dependency));
			for(Predicate fact: right.getPredicates()) {
				propagatedFacts.add(new BagBoundPredicate(fact, childBag.getId()));
			}
		}
		
		this.getFacts().addAll(right.getPredicates());
		this.getFiringGraph().put(dependency, Sets.newHashSet(left.getPredicates()), Sets.newHashSet(right.getPredicates()));
		
		for(BagBoundPredicate propagatedFact:propagatedFacts) {
			this.toUpdate.add(this.tree.getVertex(propagatedFact.getBag()));
		}
		return propagatedFacts;
	}
	
	@Override
	public boolean chaseStep(Match match) {
		return this.chaseStep(Sets.newHashSet(match));
	}


	@Override
	public boolean chaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Predicate> created = new LinkedList<>();
		for (Match match: matches) {
			Preconditions.checkState(match instanceof BagMatch);
			//For each match, get
			//(i) 	the corresponding dependency,
			//(ii) 	the map of dependency's variables to chase constants and
			//(iii) the bag where the match is found
			//and update the input state
			Constraint constraint = (Constraint) match.getQuery();
			Map<Variable, Constant> mapping = match.getMapping();
			Constraint grounded = constraint.fire(mapping, this.canonicalNames);
			log.debug("Fire rule " + grounded + " on bag " + ((BagMatch) match).getBag());
			Formula left = grounded.getLeft();
			Formula right = grounded.getRight();
			//Find to which bag each newly created fact should be propagated
			Bag parentBag = this.tree.getVertex(((BagMatch) match).getBag());
			created.addAll(this.updateBags(parentBag, constraint, left, right));
			log.trace("Updated facts: " + right.getPredicates().size());
		}
		this.manager.addFacts(created);
		return true;
	}
	
	/**
	 * @param formula
	 * @return
	 * 		true if the input formula contains fresh terms
	 */
	protected Boolean freshTermsExist(Formula formula) {
		return !this.terms.containsAll(formula.getTerms());
	}

	/**
	 * Updates the list of satisfied queries/dependencies of each bag
	 */
	@Override
	public void updateTree() {
		/*
		For each bag in the tree we update (i) the collection of the
		dependencies whose left-hand side is satisfied, as well as,
		(ii) the satisfied sub-queries of the input query. A bag must
 		be updated when new facts are propagated to it
		 */
		for (Bag bag:this.tree.vertexSet()) {
			if (this.toUpdate.contains(bag) || !bag.isUpdated()) {
				bag.update(this.getManager());
			}
		}
		this.toUpdate.clear();
	}
	
	/**
	 * @param facts Collection<? extends PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#addFacts(Collection<? extends PredicateFormula>)
	 */
	public  void addFacts(Collection<BagBoundPredicate> facts) {
		this.manager.addFacts(facts);
		for(BagBoundPredicate fact:facts) {
			this.tree.getVertex(fact.getBag()).addFacts(fact);
		}
	}
	
	/**
	 * @return BagsTree<Bag>
	 */
	@Override
	public BagsTree getTree() {
		return this.tree;
	}
	
	@Override
	public Collection<Bag> getUnupdated() {
		return this.toUpdate;
	}
	
	/**
	 * @return Collection<PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFacts()
	 */
	@Override
	public Collection<Predicate> getFacts() {
		return this.tree.getFacts();
	}

	/**
	 * @return FiringGraph<ChaseState>
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFiringGraph()
	 */
	@Override
	public FiringGraph getFiringGraph() {
		return this.graph;
	}
	
	/**
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.tree.equals(((DatabaseTreeState) o).tree);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.tree);
	}
	
	/**
	 * @return DatabaseTreeState
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#clone()
	 */
	@Override
	public DatabaseTreeState clone() {
		return new DatabaseTreeState(this.query, 
				this.manager,
				this.tree.clone(),
				Sets.newHashSet(this.terms),
				this.graph.clone());
	}

	@Override
	public boolean isSuccessful() {
		return !this.getMatches(this.query).isEmpty();
	}


	@Override
	public boolean isFailed() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
