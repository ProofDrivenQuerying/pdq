package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClass;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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

	/**  Keeps the classes of equal constants. */
	protected EqualConstantsClasses classes;

	/** The canonical names. */
	protected final boolean canonicalNames = true;

	/** Maps each constant to the atom and the position inside this atom where it appears. 
	 * We need this table when we are applying an EGD chase step. **/
	protected final Multimap<Constant, Atom> constantsToAtoms;

	/**
	 * Instantiates a new database list state.
	 *
	 * @param query the query
	 * @param manager the manager
	 */
	public DatabaseChaseListState(ConjunctiveQuery query, 
			DatabaseHomomorphismManager manager) {
		super(manager);
		this.facts = Sets.newHashSet(query.ground(ConjunctiveQuery.generateCanonicalMapping(query.getBody())).getAtoms());
		this.classes = new EqualConstantsClasses();
		this.constantsToAtoms = inferConstantsMap(this.facts);
		this.manager.addFacts(this.facts);
	}

	/**
	 * Instantiates a new database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 */
	public DatabaseChaseListState(
			DatabaseHomomorphismManager manager,
			Collection<Atom> facts) {
		super(manager);
		Preconditions.checkNotNull(facts);
		this.facts = facts;
		this.classes = new EqualConstantsClasses();
		this.constantsToAtoms = inferConstantsMap(this.facts);
		this.manager.addFacts(this.facts);
	}

	/**
	 * Instantiates a new database list state.
	 *
	 * @param manager the manager
	 * @param facts the facts
	 * @param graph the graph
	 * @param classes the constant classes
	 */
	protected DatabaseChaseListState(
			DatabaseHomomorphismManager manager,
			Collection<Atom> facts,
			EqualConstantsClasses classes,
			Multimap<Constant,Atom> constants
			) {
		super(manager);
		Preconditions.checkNotNull(facts);
		Preconditions.checkNotNull(classes);
		Preconditions.checkNotNull(constants);
		this.facts = facts;
		this.classes = classes;
		this.constantsToAtoms = constants; 
	}

	/**
	 * 
	 * @param facts
	 * @return a map of each constant to the atom and the position inside this atom where it appears. 
	 * An exception is thrown when there is an equality in the input
	 */
	protected static Multimap<Constant,Atom> inferConstantsMap(Collection<Atom> facts) {
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		for(Atom fact:facts) {
			Preconditions.checkArgument(!(fact instanceof Equality));
			for(Term term:fact.getTerms()) {
				constantsToAtoms.put((Constant)term, fact);
			}
		}
		return constantsToAtoms;
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

	/* 
	 * 
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#chaseStep(java.util.Collection)
	 */
	@Override
	public boolean chaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		if(!matches.isEmpty()) {
			Match match = matches.iterator().next();
			if(match.getQuery() instanceof EGD) {
				return this.EGDchaseStep(matches);
			}
			else if(match.getQuery() instanceof TGD) {
				return this.TGDchaseStep(matches);
			}
		}
		return false;
	}

	/**
	 * Applies chase steps for TGDs. 
	 * An exception is thrown when a match does not come from a TGD
	 * @param matches
	 * @return
	 */
	public boolean TGDchaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Atom> newFacts = new LinkedHashSet<>();
		for(Match match:matches) {
			Constraint dependency = (Constraint) match.getQuery();
			Preconditions.checkArgument(dependency instanceof TGD, "EGDs are not allowed inside TGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Constraint grounded = dependency.fire(mapping, true);
			Formula left = grounded.getLeft();
			Formula right = grounded.getRight();
			//Add information about new facts to constantsToAtoms
			for(Atom atom:right.getAtoms()) {
				for(Term term:atom.getTerms()) {
					this.constantsToAtoms.put((Constant)term, atom);
				}
			}
			newFacts.addAll(right.getAtoms());
		}
		//Add the newly created facts to the database
		this.addFacts(newFacts);
		return !this._isFailed;
	}

	/** 
	 * TODO we need to think what will happen with a map firing graph in the presence of EGDs.
	 * 
	 * Applies chase steps for EGDs. 
	 * An exception is thrown when a match does not come from an EGD or from different EGDs.
	 * The following steps are performed:
	 * The classes of equal constants are updated according to the input equalities 
	 * The constants whose representatives will change are detected.
	 * We create new facts based on the updated representatives. 
	 * We delete the facts with obsolete representatives  
	 */
	public boolean EGDchaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Atom> newFacts = new LinkedHashSet<>();
		//For each fired EGD update the classes of equal constants
		//and find all constants with updated representatives
		//Maps each constant to its new representative  
		Map<Constant,Constant> obsoleteToRepresentative = Maps.newHashMap();
		for(Match match:matches) {
			Constraint dependency = (Constraint) match.getQuery();
			Preconditions.checkArgument(dependency instanceof EGD, "TGDs are not allowed inside EGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Constraint grounded = dependency.fire(mapping, true);
			Formula left = grounded.getLeft();
			Formula right = grounded.getRight();
			for(Atom atom:right.getAtoms()) {
				//Find all the constants that each constant in the equality is representing 
				Equality equality = (Equality)atom;
				obsoleteToRepresentative.putAll(this.updateEqualConstantClasses(equality));
				if(this._isFailed) {
					return false;
				}
				//Equalities should be added into the database 
				newFacts.addAll(right.getAtoms());
			}	
		}

		//Remove all outdated facts from the database
		//Find all database facts that should be updated  
		Collection<Atom> obsoleteFacts = Sets.newHashSet(); 
		//Find the facts with the obsolete constant 
		for(Constant obsoleteConstant:obsoleteToRepresentative.keySet()) {
			obsoleteFacts.addAll((Collection<? extends Atom>) this.constantsToAtoms.get(obsoleteConstant));
		}

		for(Atom fact:obsoleteFacts) {
			List<Term> newTerms = Lists.newArrayList();
			for(Term term:fact.getTerms()) {
				EqualConstantsClass cls = this.classes.getClass(term);
				newTerms.add(cls != null ? cls.getRepresentative() : term);
			}
			Atom newFact = new Atom(fact.getPredicate(), newTerms);
			newFacts.add(newFact);

			//Add information about new facts to constantsToAtoms
			for(Term term:newTerms) {
				this.constantsToAtoms.put((Constant)term, newFact);
			}
		}

		//Delete all obsolete constants from constantsToAtoms
		for(Constant obsoleteConstant:obsoleteToRepresentative.keySet()) {
			this.constantsToAtoms.removeAll(obsoleteConstant);
		}
		
		this.facts.removeAll(obsoleteFacts);
		this.manager.deleteFacts(obsoleteFacts);
		this.addFacts(newFacts);
		return !this._isFailed;
	}

	/**
	 * Updates the classes of equal constants and returns the constants whose representative changed. 
	 * @param atom
	 * @return
	 */
	public Map<Constant,Constant> updateEqualConstantClasses(Equality atom) {
		//Maps each constant to its new representative  
		Map<Constant,Constant> obsoleteToRepresentative = Maps.newHashMap();

		Constant c_l = (Constant) atom.getTerm(0);
		//Find the class of the first input constant and its representative
		EqualConstantsClass class_l = this.classes.getClass(c_l);
		//Find all constants of this class and its representative
		Collection<Term> constants_l = Sets.newHashSet();
		Term representative_l = null;
		if(class_l != null) {
			constants_l.addAll(class_l.getConstants());
			representative_l = class_l.getRepresentative().clone();
		}

		Constant c_r = (Constant) atom.getTerm(1);
		//Find the class of the first input constant and its representative
		EqualConstantsClass class_r = this.classes.getClass(c_r);
		//Find all constants of this class and its representative
		Collection<Term> constants_r = Sets.newHashSet();
		Term representative_r = null;
		if(class_r != null) {
			constants_r.addAll(class_r.getConstants());
			representative_r = class_r.getRepresentative().clone();
		}

		boolean successfull;
		if(class_r != null && class_l != null && class_r.equals(class_l)) {
			successfull = true;
		}
		else {
			successfull = this.classes.add((Equality)atom);
			if(!successfull) {
				this._isFailed = true;
				return Maps.newHashMap();
			}
			//Detect all constants whose representative will change 
			if(representative_l == null && representative_r == null) {
				if(this.classes.getClass(c_l).getRepresentative().equals(c_l)) {
					obsoleteToRepresentative.put(c_r, c_l);
				}
				else if(this.classes.getClass(c_r).getRepresentative().equals(c_r)) {
					obsoleteToRepresentative.put(c_l, c_r);
				}
			}
			else if(representative_l == null && !this.classes.getClass(c_l).getRepresentative().equals(c_l) || 
					!this.classes.getClass(c_l).getRepresentative().equals(representative_l)) {
				for(Term term:constants_l) {
					obsoleteToRepresentative.put((Constant)term, (Constant)this.classes.getClass(c_l).getRepresentative());
				}
				obsoleteToRepresentative.put(c_l, (Constant)this.classes.getClass(c_l).getRepresentative());
			}
			else if(representative_r == null && !this.classes.getClass(c_r).getRepresentative().equals(c_r) || 
					!this.classes.getClass(c_r).getRepresentative().equals(representative_r)) {
				for(Term term:constants_r) {
					obsoleteToRepresentative.put((Constant)term, (Constant)this.classes.getClass(c_r).getRepresentative());
				}
				obsoleteToRepresentative.put(c_r, (Constant)this.classes.getClass(c_r).getRepresentative());
			}
		}
		return obsoleteToRepresentative;
	}

	/**
	 * Gets the constant classes.
	 *
	 * @return the constant classes
	 */
	public EqualConstantsClasses getConstantClasses() {
		return this.classes;
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
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState#getFacts()
	 */
	@Override
	public Collection<Atom> getFacts() {
		return this.facts;
	}
	
	public Multimap<Constant, Atom> getConstantsToAtoms() {
		return this.constantsToAtoms;
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
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		return new DatabaseChaseListState(this.manager, Sets.newHashSet(this.facts), this.classes.clone(), constantsToAtoms);
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
		HomomorphismProperty[] c = new HomomorphismProperty[constraints.length+1];
		System.arraycopy(constraints, 0, c, 0, constraints.length);
		c[constraints.length] = HomomorphismProperty.createFactProperty(Conjunction.of(this.getFacts()));
		return this.manager.getMatches(dependencies, c);
	}


}
