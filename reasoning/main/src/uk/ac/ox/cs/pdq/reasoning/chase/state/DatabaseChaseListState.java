package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClass;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 *
 * 	Organises the facts during chasing into a list. 
 *	This type of state is used in terminating chase implementations.
 *	It also maintains the classes of equal chase constants that are derived after chasing with EGDs.
 *	This implementation does not store equality facts into the database, but when a class of equal constants is created
 *	the database facts are updated; update includes replacing every chase constant c, with a constant c' that is equal to c
 *	under the constraints and c' is a representative.
 *	The database is cleared from the obsolete facts after a chase step is applied.
 *	 
 *
 * @author Efthymia Tsamoura
 *
 */
public class DatabaseChaseListState extends DatabaseChaseState implements ListState {


	/** The _is failed. */
	private boolean _isFailed = false;

	/**  The state's facts. */
	protected Collection<Atom> facts;

	/**  The firings that took place in this state. */
	protected FiringGraph graph;

	/**  Keeps the classes of equal constants *. */
	protected EqualConstantsClasses constantClasses;
	
	/** The canonical names. */
	protected final boolean canonicalNames = true;

	/**
	 * Instantiates a new database list state.
	 *
	 * @param query the query
	 * @param manager the manager
	 */
	public DatabaseChaseListState(Query<?> query, DBHomomorphismManager manager) {
		this(manager, Sets.newHashSet(query.getCanonical().getAtoms()), new MapFiringGraph(), inferEqualConstantsClasses(query.getCanonical().getAtoms()));
		this.manager.addFacts(this.facts);
	}

	/**
	 * Instantiates a new database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 */
	public DatabaseChaseListState(
			DBHomomorphismManager manager,
			Collection<Atom> facts) {
		this(manager, facts, new MapFiringGraph(), inferEqualConstantsClasses(facts));
		this.manager.addFacts(this.facts);
	}

	/**
	 * Instantiates a new database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 * @param graph the graph
	 * @param constantClasses the constant classes
	 */
	protected DatabaseChaseListState(
			DBHomomorphismManager manager,
			Collection<Atom> facts,
			FiringGraph graph, 
			EqualConstantsClasses constantClasses
			) {
		super(manager);
		Preconditions.checkNotNull(facts);
		Preconditions.checkNotNull(graph);
		this.facts = facts;
		this.graph = graph;
		this.constantClasses = constantClasses;
	}

	/**
	 * Infer equal constants classes.
	 *
	 * @param facts the facts
	 * @return the equal constants classes
	 */
	public static EqualConstantsClasses inferEqualConstantsClasses(Collection<Atom> facts) {
		EqualConstantsClasses constantClasses = new EqualConstantsClasses();
		for(Atom fact:facts) {
			if(fact instanceof Equality) {
				constantClasses.add((Equality) fact);
			}
		}
		return constantClasses;
	}

	/**
	 * Updates that state given the input match. 
	 *
	 * @param match the match
	 * @return true, if successful
	 */
	@Override
	public boolean chaseStep(Match match) {	
		return this.chaseStep(Sets.newHashSet(match));
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#chaseStep(java.util.Collection)
	 */
	@Override
	public boolean chaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Atom> created = new LinkedHashSet<>();
		
		//For each fired EGD create classes of equivalent constants
		for(Match match:matches) {
			Constraint dependency = (Constraint) match.getQuery();
			Map<Variable, Constant> mapping = match.getMapping();
			Constraint grounded = dependency.fire(mapping, true);
			Formula left = grounded.getLeft();
			Formula right = grounded.getRight();
			if(dependency instanceof EGD) {
				for(Atom equality:right.getAtoms()) {
					boolean successfull = this.constantClasses.add((Equality)equality);
					if(!successfull) {
						this._isFailed = true;
						break;
					}
				}
			}	
			this.graph.put(dependency, Sets.newHashSet(left.getAtoms()), Sets.newHashSet(right.getAtoms()));
		}

		//Iterate over all the database facts and replace their chase constants based on the classes of equal constants 
		//Delete the old facts from this state
		Collection<Atom> obsoleteFacts = Sets.newHashSet();
		for(Match match:matches) {
			if(match.getQuery() instanceof EGD) {
				for(Atom fact:this.facts) {
					List<Term> newTerms = Lists.newArrayList();
					for(Term term:fact.getTerms()) {
						EqualConstantsClass cls = this.constantClasses.getClass(term);;
						newTerms.add(cls != null ? cls.getRepresentative() : term);
					}
					if(!newTerms.equals(fact.getTerms())) {
						created.add(new Atom(fact.getPredicate(), newTerms));
						obsoleteFacts.add(fact);
					}
				}
			}
		}

		//Do not add the Equalities inside the database
		for(Match match:matches) {
			if(!(match.getQuery() instanceof EGD)) {
				Constraint dependency = (Constraint) match.getQuery();
				Map<Variable, Constant> mapping = match.getMapping();
				Constraint grounded = dependency.fire(mapping, true);
				Formula right = grounded.getRight();
				for(Atom fact:right.getAtoms()) {
					List<Term> newTerms = Lists.newArrayList();
					for(Term term:fact.getTerms()) {
						EqualConstantsClass cls = this.constantClasses.getClass(term);
						newTerms.add(cls != null ? cls.getRepresentative() : term);
					}
					created.add(new Atom(fact.getPredicate(), newTerms));
				}
			}
		}
		
		this.facts.removeAll(obsoleteFacts);
		this.manager.deleteFacts(obsoleteFacts);
		this.addFacts(created);
		return !this._isFailed;
	}

	/**
	 * Gets the constant classes.
	 *
	 * @return the constant classes
	 */
	public EqualConstantsClasses getConstantClasses() {
		return this.constantClasses;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#isFailed()
	 */
	@Override
	public boolean isFailed() {
		return this._isFailed;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#isSuccessful(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean isSuccessful(Query<?> query) {
		return !this.getMatches(query).isEmpty();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFiringGraph()
	 */
	@Override
	public FiringGraph getFiringGraph() {
		return this.graph;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFacts()
	 */
	@Override
	public Collection<Atom> getFacts() {
		return this.facts;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#merge(uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState)
	 */
	@Override
	public ChaseState merge(ChaseState s) {
		Preconditions.checkState(s instanceof DatabaseChaseListState);
		Collection<Atom> facts =  new LinkedHashSet<>(this.facts);
		facts.addAll(s.getFacts());

		EqualConstantsClasses classes = this.constantClasses.clone();
		if(!classes.merge(((DatabaseChaseListState)s).constantClasses)) {
			return null;
		}
		return new DatabaseChaseListState(
				this.getManager(),
				facts, 
				this.getFiringGraph().merge(s.getFiringGraph()), classes);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ListState#addFacts(java.util.Collection)
	 */
	@Override
	public void addFacts(Collection<Atom> facts) {
		this.manager.addFacts(facts);
		this.facts.addAll(facts);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState#clone()
	 */
	@Override
	public DatabaseChaseListState clone() {
		return new DatabaseChaseListState(this.manager, Sets.newHashSet(this.facts), this.graph.clone(), this.constantClasses.clone());
	}	

	/**
	 * Calls the manager to detect homomorphisms of the input query to facts in this state.
	 * The manager detects homomorphisms using a database backend.
	 * @param query Query
	 * @return List<Match>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getMatches(Query)
	 */
	@Override
	public List<Match> getMatches(Query<?> query) {
		return this.manager.getMatches(
				Lists.<Query<?>>newArrayList(query),
//				HomomorphismProperty.createTopKProperty(1),
				HomomorphismProperty.createFactProperty(Conjunction.of(this.getFacts())),
				HomomorphismProperty.createMapProperty(query.getFreeToCanonical()));
	}
	
	/**
	 * Calls the manager to detect homomorphisms of the input query to facts in this state.
	 * The manager detects homomorphisms using a database backend.
	 *
	 * @param query Query
	 * @param constraints the constraints
	 * @return List<Match>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getMatches(Query)
	 */
	@Override
	public List<Match> getMatches(Query<?> query, HomomorphismProperty... constraints) {
		HomomorphismProperty[] c = new HomomorphismProperty[constraints.length+1];
		System.arraycopy(constraints, 0, c, 0, constraints.length);
		c[constraints.length] = HomomorphismProperty.createFactProperty(Conjunction.of(this.getFacts()));
		return this.manager.getMatches(Lists.<Query<?>>newArrayList(query), c);
	}
	

	/**
	 * Calls the manager to detect homomorphisms of the input dependencies to facts in this state.
	 * The manager detects homomorphisms using a database backend.
	 * @param dependencies Collection<D>
	 * @param constraints HomomorphismConstraint...
	 * @return Map<D,List<Match>>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getHomomorphisms(Collection<D>)
	 */
	@Override
	public List<Match> getMatches(Collection<? extends Constraint> dependencies, HomomorphismProperty... constraints) {
		HomomorphismProperty[] c = new HomomorphismProperty[constraints.length+1];
		System.arraycopy(constraints, 0, c, 0, constraints.length);
		c[constraints.length] = HomomorphismProperty.createFactProperty(Conjunction.of(this.getFacts()));
		return this.manager.getMatches(dependencies, c);
	}


}
