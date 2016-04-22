package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.utility.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.MapFiringGraph;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;
import uk.ac.ox.cs.pdq.util.Utility;

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
public class DatabaseRestrictedState extends DatabaseChaseState implements ListState {

	/** The _is failed. */
	private boolean _isFailed = false;

	/**  Keeps the classes of equal constants *. */
	protected EqualConstantsClasses constantClasses;
	
	/** The canonical names. */
	protected final boolean canonicalNames = true;
	
	
	/**
	 * Instantiates a new database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 */
	public DatabaseRestrictedState(
			DBHomomorphismManager manager,
			Collection<Atom> facts) {
		this(manager, facts, inferEqualConstantsClasses(facts));
//		this.manager.addFacts(this.facts);
	}

	/**
	 * Instantiates a new database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 * @param graph the graph
	 * @param constantClasses the constant classes
	 */
	protected DatabaseRestrictedState(
			DBHomomorphismManager manager,
			Collection<Atom> facts,
			EqualConstantsClasses constantClasses
			) {
		super(manager);
		Preconditions.checkNotNull(facts);
//		this.facts = facts;
		this.constantClasses = constantClasses;
		this.manager.addFacts(facts);
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
		long start1 = System.currentTimeMillis();
		Collection<Atom> created = new LinkedHashSet<>();
		
		System.out.println("Total number of matches: " + matches.size());
		/*
		//For each fired EGD create classes of equivalent constants
		for(Match match:matches) {
			Constraint dependency = (Constraint) match.getQuery();
			Map<Variable, Constant> mapping = match.getMapping();
			Constraint grounded = dependency.fire(mapping, true);
			Formula left = grounded.getLeft();
			Formula right = grounded.getRight();
			if(dependency instanceof EGD) {
				for(Predicate equality:right.getPredicates()) {
					boolean successfull = this.constantClasses.add((Equality)equality);
					if(!successfull) {
						this._isFailed = true;
						break;
					}
				}
			}	
			this.graph.put(dependency, Sets.newHashSet(left.getPredicates()), Sets.newHashSet(right.getPredicates()));
		}

		//Iterate over all the database facts and replace their chase constants based on the classes of equal constants 
		//Delete the old facts from this state
		Collection<Predicate> obsoleteFacts = Sets.newHashSet();
		for(Match match:matches) {
			if(match.getQuery() instanceof EGD) {
				for(Predicate fact:this.facts) {
					List<Term> newTerms = Lists.newArrayList();
					for(Term term:fact.getTerms()) {
						EqualConstantsClass cls = this.constantClasses.getClass(term);;
						newTerms.add(cls != null ? cls.getRepresentative() : term);
					}
					if(!newTerms.equals(fact.getTerms())) {
						created.add(new Predicate(fact.getSignature(), newTerms));
						obsoleteFacts.add(fact);
					}
				}
			}
		}
*/		

		//Do not add the Equalities inside the database
		for(Match match:matches) {
			if(!(match.getQuery() instanceof EGD)) {
				Constraint dependency = (Constraint) match.getQuery();
				Map<Variable, Constant> mapping = match.getMapping();
				Constraint grounded = dependency.fire(mapping, true);
				Formula right = grounded.getRight();
				
				/*
				for(Predicate fact:right.getPredicates()) {
					List<Term> newTerms = Lists.newArrayList();
					for(Term term:fact.getTerms()) {
						EqualConstantsClass cls = this.constantClasses.getClass(term);
						newTerms.add(cls != null ? cls.getRepresentative() : term);
					}
					created.add(new Predicate(fact.getSignature(), newTerms));
				}
				*/
				created.addAll(right.getAtoms());
			}
		}
		
		long end1 = System.currentTimeMillis();
		System.out.println("Time to do inmemory staff: " + (end1-start1));
		
		/*
		long start2 = System.currentTimeMillis();
		this.facts.removeAll(obsoleteFacts);
		this.manager.deleteFacts(obsoleteFacts);
		long end2 = System.currentTimeMillis();
		System.out.println("Time to delete facts: " + (end2-start2));
		*/
		
		
		long start3 = System.currentTimeMillis();
		this.addFacts(created);
		long end3 = System.currentTimeMillis();
		
		System.out.println("Time to insert facts: " + (end3-start3));
		
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
	public boolean isSuccessful(ConjunctiveQuery query) {
		return !this.getMatches(query).isEmpty();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFiringGraph()
	 */
	@Override
	public FiringGraph getFiringGraph() {
		throw new java.lang.UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFacts()
	 */
	@Override
	public Collection<Atom> getFacts() {
		throw new java.lang.UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#merge(uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState)
	 */
	@Override
	public ChaseState merge(ChaseState s) {
		throw new java.lang.UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ListState#addFacts(java.util.Collection)
	 */
	@Override
	public void addFacts(Collection<Atom> facts) {
		this.manager.addFacts(facts);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState#clone()
	 */
	@Override
	public DatabaseRestrictedState clone() {
//		return new DatabaseRestrictedState(this.manager, Sets.newHashSet(this.facts), this.constantClasses.clone());
		throw new java.lang.UnsupportedOperationException();
	}	


	/**
	 * Calls the manager to detect homomorphisms of the input query to facts in this state.
	 * The manager detects homomorphisms using a database backend.
	 * @param query Query
	 * @return List<Match>
	 * @see uk.ac.ox.cs.pdq.chase.state.ChaseState#getMatches(Query)
	 */
	@Override
	public List<Match> getMatches(ConjunctiveQuery query) {
		return this.manager.getMatches(
				Lists.<Query<?>>newArrayList(query),
//				HomomorphismConstraint.createTopKConstraint(1),
				HomomorphismProperty.createFactProperty(Conjunction.of(this.getFacts())),
				HomomorphismProperty.createMapProperty(query.getGroundingsProjectionOnFreeVars()));
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
	public List<Match> getMatches(ConjunctiveQuery query, HomomorphismProperty... constraints) {
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
		return this.manager.getMatches(dependencies, constraints);
	}

}
