package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQueryWithInequality;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Implication;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClass;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.reasoning.utility.ReasonerUtility;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 *
 * A collection of facts produced during chasing. It also keeps a graph of the
 * rule firings that took place during chasing. This implementation keeps the
 * facts produced during chasing in a database. Homomorphisms are detected using
 * the DBMS the stores the chase facts.
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class DatabaseChaseInstance implements ChaseInstance {
	protected static Logger log = Logger.getLogger(LogicalDatabaseInstance.class);
	protected final DatabaseManager chaseDatabaseInstance;
	private boolean _isFailed = false;

	/**
	 * Keeps the classes of equivalence classes of constants, that are equated in
	 * EGD chasing.
	 */
	protected EqualConstantsClasses classes;

	/**
	 * Maps each constant to a list of atoms that have the same constant.
	 * We need this table when we are applying an EGD chase step, to easily find all facts that has an obsolete
	 * constant in order to update them with the new representative constant. 
	 * Could be replaced with a query.
	 **/
	// TOCOMMENT discuss constantsToAtoms 
	protected final Multimap<Constant, Atom> constantsToAtoms;

	/**
	 * The hashcode of this class. used to create an "instanceID" unique per
	 * instance
	 * 
	 */
	private Integer hash = null;

	/**
	 * Instantiates a new DatabaseChaseInstance in order to chase a (canonical
	 * database of a query). It creates and executes SQL in order to insert the
	 * facts of the canonical database of the query in the underlying instance. By
	 * convention the schema of the input query is assumed to contain an instanceID
	 * attribute, where each fact will store the instanceID of the instance that
	 * contains it. This constructor also performs indexing for all positions that
	 * contain joins in the dependencies, as well as the last position of all
	 * relations. Additionally the constructor creates an "Equality" relation (used
	 * in EGD chasing), and initialises internal maps needed for EGD chasing.
	 * 
	 * @param query
	 *            The query whose canonical database is to be inserted and then
	 *            chased
	 * @param connection
	 *            The database connection to the RDBMS
	 * @throws SQLException
	 *             Exception risen if something goes wrong with the SQL that inserts
	 *             the facts.
	 */
	public DatabaseChaseInstance(ConjunctiveQuery query, DatabaseManager connection) throws SQLException {
		try {
			chaseDatabaseInstance = connection.clone(GlobalCounterProvider.getNext("DatabaseInstanceId"));
		} catch (DatabaseException e) {
			throw new RuntimeException("database failure", e);
		}
		this.addFacts(Sets.newHashSet(uk.ac.ox.cs.pdq.reasoning.chase.Utility.applySubstitution(query, uk.ac.ox.cs.pdq.reasoning.chase.Utility.generateCanonicalMapping(query)).getAtoms()));
		this.classes = new EqualConstantsClasses();
		try {
			this.constantsToAtoms = ReasonerUtility.createdConstantsMap(chaseDatabaseInstance.getCachedFacts());
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		this.initDatabase();
	}

	@Override
	public void deleteFacts(Collection<Atom> facts) {
		try {
			chaseDatabaseInstance.deleteFacts(facts);
		} catch (DatabaseException e) {
			System.err.println("Could not delete facts (" + facts + ") from this: " + this);
			e.printStackTrace();
		}
	}

	/**
	 * Instantiates a new database list state.
	 *
	 * @param chaseState
	 *            the chaseState
	 * @param facts
	 *            the facts
	 */
	public DatabaseChaseInstance(Collection<Atom> facts, DatabaseManager connection) throws SQLException {
		try {
			chaseDatabaseInstance = connection.clone(GlobalCounterProvider.getNext("DatabaseInstanceId"));
		} catch (DatabaseException e) {
			throw new RuntimeException("database failure", e);
		}
		Preconditions.checkNotNull(facts);
		this.addFacts(facts);
		this.classes = new EqualConstantsClasses();
		try {
			this.constantsToAtoms = ReasonerUtility.createdConstantsMap(chaseDatabaseInstance.getCachedFacts());
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}

		this.initDatabase();
	}

	/**
	 * Cloning constructor.
	 * 
	 * @param connection
	 * @throws SQLException
	 */
	private DatabaseChaseInstance(EqualConstantsClasses classes, Multimap<Constant, Atom> constants, DatabaseManager connection) {
		try {
			chaseDatabaseInstance = connection.clone(GlobalCounterProvider.getNext("DatabaseInstanceId"));
		} catch (DatabaseException e) {
			throw new RuntimeException("database failure", e);
		}
		Preconditions.checkNotNull(classes);
		Preconditions.checkNotNull(constants);
		this.classes = classes;
		this.constantsToAtoms = constants;
	}

	/**
	 * Instantiates a new DatabaseChaseInstance in order to chase a set of facts.
	 * This protected constructor does not(!) add the facts into the RDBMS. Using
	 * this constructor one would need to call addFacts explicitly. It also does not
	 * perform any constraint or attribute indexing. It accepts as input the sets
	 * internal maps needed for EGD chasing.
	 * 
	 * 
	 * @param facts
	 *            The facts to inserted and later chased
	 * @param classes
	 *            an EqualConstantsClasses object that keeps multiple classes of
	 *            equal constants created during EGD chasing.
	 * @param constantsToAtoms
	 *            a map of each constant to the atom and the position inside this
	 *            atom where it appears, used in EGD chasing.
	 * @param connection
	 *            The database connection to the RDBMS
	 */
	protected DatabaseChaseInstance(Collection<Atom> facts, EqualConstantsClasses classes, Multimap<Constant, Atom> constants, DatabaseManager connection) {
		try {
			chaseDatabaseInstance = connection.clone(GlobalCounterProvider.getNext("DatabaseInstanceId"));
		} catch (DatabaseException e) {
			throw new RuntimeException("database failure", e);
		}
		Preconditions.checkNotNull(facts);
		Preconditions.checkNotNull(classes);
		Preconditions.checkNotNull(constants);
		try {
			chaseDatabaseInstance.addFacts(facts);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		this.classes = classes;
		this.constantsToAtoms = constants;
	}

	private void initDatabase() throws SQLException {
		Relation equality = this.createDatabaseEqualityRelation();
		try {
			// ADD EQUALITY RELATION
			chaseDatabaseInstance.addRelation(equality);
		} catch (DatabaseException e) {
			throw new SQLException(e);
		}
	}

	/**
	 * Creates an "equality" relation for storing terms in the chase that are
	 * equated by EGDs
	 * 
	 * @return
	 */
	private Relation createDatabaseEqualityRelation() {
		String attrPrefix = "x";
		Attribute[] attributes = new Attribute[] { Attribute.create(String.class, attrPrefix + 0), Attribute.create(String.class, attrPrefix + 1) };
		return Relation.create(QNames.EQUALITY.toString(), attributes, true);
	}

	/*
	 * ??? What does false here mean? (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#chaseStep(java.util.
	 * Collection)
	 */
	@Override
	public boolean chaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		if (!matches.isEmpty()) {
			Match match = matches.iterator().next();
			if (match.getFormula() instanceof EGD) {
				return this.EGDchaseStep(matches);
			} else if (match.getFormula() instanceof TGD) {
				return this.TGDchaseStep(matches);
			}
		}
		return false;
	}

	/**
	 * Applies chase steps for TGDs. An exception is thrown when a match does not
	 * come from a TGD
	 * 
	 * @param matches
	 *            the input triggers (and dependencies) to be applied
	 * @return false if and only if the update of equivalence classes fails.
	 */
	public boolean TGDchaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Atom> newFacts = new LinkedHashSet<>();
		for (Match match : matches) {
			Dependency dependency = (Dependency) match.getFormula();
			Preconditions.checkArgument(dependency instanceof TGD, "EGDs are not allowed inside TGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Implication grounded = uk.ac.ox.cs.pdq.reasoning.chase.Utility.ground(dependency, mapping, true);
			Formula right = grounded.getChild(1);
			// Add information about new facts to constantsToAtoms
			for (Atom atom : right.getAtoms()) {
				for (Term term : atom.getTerms())
					this.constantsToAtoms.put((Constant) term, atom);
			}
			newFacts.addAll(Arrays.asList(right.getAtoms()));
		}
		// Add the newly created facts to the database
		this.addFacts(newFacts);
		return !this._isFailed;
	}

	/**
	 * 
	 * Applies chase steps for EGDs. An exception is thrown when a match does not
	 * come from an EGD or from different EGDs. The following steps are performed:
	 * The classes of equal constants are updated according to the input equalities
	 * The constants whose representatives will change are detected. We create new
	 * facts based on the updated representatives. We delete the facts with obsolete
	 * representatives
	 * 
	 * @param matches
	 *            the input triggers (and dependencies) to be applied
	 * @return true, if the chase step is successful
	 */
	public boolean EGDchaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Atom> newFacts = new LinkedHashSet<>();
		// For each fired EGD update the classes of equal constants
		// and find all constants with updated representatives
		// Maps each constant to its new representative
		Map<Constant, Constant> obsoleteToRepresentative = Maps.newHashMap();
		for (Match match : matches) {
			Dependency dependency = (Dependency) match.getFormula();
			Preconditions.checkArgument(dependency instanceof EGD, "TGDs are not allowed inside EGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Implication grounded = uk.ac.ox.cs.pdq.reasoning.chase.Utility.fire(dependency, mapping);
			Formula right = grounded.getChild(1);
			for (Atom atom : right.getAtoms()) {
				// Find all the constants that each constant in the equality is representing
				obsoleteToRepresentative.putAll(this.updateEqualConstantClasses(atom));
				if (this._isFailed)
					return false;

				// Equalities should be added into the database
				newFacts.addAll(Arrays.asList(right.getAtoms()));
			}
		}

		// Remove all outdated facts from the database
		// Find all database facts that should be updated
		Collection<Atom> obsoleteFacts = Sets.newHashSet();
		// Find the facts with the obsolete constant
		for (Constant obsoleteConstant : obsoleteToRepresentative.keySet()) {
			obsoleteFacts.addAll((Collection<? extends Atom>) this.constantsToAtoms.get(obsoleteConstant));
		}

		for (Atom fact : obsoleteFacts) {
			Term[] newTerms = new Term[fact.getNumberOfTerms()];
			for (int termIndex = 0; termIndex < fact.getNumberOfTerms(); ++termIndex) {
				EqualConstantsClass cls = this.classes.getClass(fact.getTerm(termIndex));
				newTerms[termIndex] = cls != null ? cls.getRepresentative() : fact.getTerm(termIndex);
			}
			Atom newFact = Atom.create(fact.getPredicate(), newTerms);
			newFacts.add(newFact);

			// Add information about new facts to constantsToAtoms
			for (Term term : newTerms) {
				this.constantsToAtoms.put((Constant) term, newFact);
			}
		}

		// Delete all obsolete constants from constantsToAtoms
		for (Constant obsoleteConstant : obsoleteToRepresentative.keySet()) {
			this.constantsToAtoms.removeAll(obsoleteConstant);
		}

		obsoleteFacts.removeAll(newFacts); // do not delete what we will add back anyway.
		deleteFacts(obsoleteFacts);
		this.addFacts(newFacts);
		return !this._isFailed;
	}

	/**
	 * Updates the classes of equal constants and returns the constants whose
	 * representative changed, for use in fact-propagation
	 * 
	 * @param atom
	 *            an equality atom
	 * @return a map mapping each constant that is to be replaced by its
	 *         representative to the a latter, after taking the input equality into
	 *         account.
	 */
	public Map<Constant, Constant> updateEqualConstantClasses(Atom atom) {
		Preconditions.checkArgument(atom.isEquality());
		// Maps each constant to its new representative
		Map<Constant, Constant> obsoleteToRepresentative = Maps.newHashMap();

		Constant c_l = (Constant) atom.getTerm(0);
		// Find the class of the first input constant and its representative
		EqualConstantsClass class_l = this.classes.getClass(c_l);
		// Find all constants of this class and its representative
		Collection<Term> constants_l = Sets.newHashSet();
		Term old_representative_l = null;
		if (class_l != null) {
			constants_l.addAll(class_l.getConstants());
			old_representative_l = class_l.getRepresentative();
		}

		Constant c_r = (Constant) atom.getTerm(1);
		// Find the class of the first input constant and its representative
		EqualConstantsClass class_r = this.classes.getClass(c_r);
		// Find all constants of this class and its representative
		Collection<Term> constants_r = Sets.newHashSet();
		Term old_representative_r = null;
		if (class_r != null) {
			constants_r.addAll(class_r.getConstants());
			old_representative_r = class_r.getRepresentative();
		}

		boolean successfull;
		// if already the same class nothing to do
		if (class_r != null && class_l != null && class_r.equals(class_l)) {
			successfull = true;
		} else {
			// does the actual update of the representatives, or returns false if the update
			// cannot be done and the chase fails
			successfull = this.classes.add(atom);
			if (!successfull) {
				this._isFailed = true;
				return Maps.newHashMap();
			}

			// doesn't matter left or right representative, the new one should always be the
			// same.
			Constant newRepresentative = (Constant) this.classes.getClass(c_r).getRepresentative();

			// Detect all constants whose representative will change
			if (old_representative_l == null && old_representative_r == null) {
				// Both new, no classes to update
				if (newRepresentative.equals(c_l)) {
					obsoleteToRepresentative.put(c_r, newRepresentative);
				} else if (newRepresentative.equals(c_r)) {
					obsoleteToRepresentative.put(c_l, newRepresentative);
				}
			} else if (old_representative_l == null) {
				// left side is new
				if (!newRepresentative.equals(c_l))
					obsoleteToRepresentative.put(c_l, newRepresentative);

				if (!old_representative_r.equals(newRepresentative)) {
					// the new element on the left become the representative for the whole class on
					// the right
					for (Term term : constants_r) {
						obsoleteToRepresentative.put((Constant) term, newRepresentative);
					}
				}
			} else if (old_representative_r == null) {
				// right side is new
				if (!newRepresentative.equals(c_r))
					obsoleteToRepresentative.put(c_r, newRepresentative);
				if (!old_representative_l.equals(newRepresentative)) {
					// the new element on the right become the representative for the whole class on
					// the left
					for (Term term : constants_l) {
						obsoleteToRepresentative.put((Constant) term, newRepresentative);
					}
				}
			} else {
				// both side had representative we need to update according who is the winner
				if (newRepresentative.equals(old_representative_r)) {
					// the right side constant's representative become the king of all classes
					for (Term term : constants_l) {
						obsoleteToRepresentative.put((Constant) term, newRepresentative);
					}
					obsoleteToRepresentative.put(c_l, newRepresentative);
				} else {
					// the left side constant's representative become the king of all classes
					for (Term term : constants_r) {
						obsoleteToRepresentative.put((Constant) term, newRepresentative);
					}
					obsoleteToRepresentative.put(c_r, newRepresentative);
				}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#isFailed()
	 */
	@Override
	public boolean isFailed() {
		return this._isFailed;
	}

	// public Multimap<Constant, Atom> getConstantsToAtoms() {
	// return this.constantsToAtoms;
	// }

	/*
	 * (non-Javadoc) add the input set of facts to the database
	 */
	@Override
	public void addFacts(Collection<Atom> factsToAdd) {
		try {
			LinkedHashSet<Atom> newFacts = new LinkedHashSet<Atom>();
			newFacts.addAll(factsToAdd);
			// make sure equality atoms are always added in pairs ( EQUALITY(a,b) should
			// have a pair EQUALITY(b,a) )
			for (Atom factToAdd : factsToAdd) {
				if ("Equality".equalsIgnoreCase(factToAdd.getPredicate().getName())) {
					Atom pair = Atom.create(factToAdd.getPredicate(), new Term[] { factToAdd.getTerms()[1], factToAdd.getTerms()[0] });
					if (!newFacts.contains(pair)) {
						newFacts.add(pair);
					}
				}
			}
			chaseDatabaseInstance.addFacts(newFacts);
		} catch (Throwable t) {
			System.err.println("Could not add facts (" + factsToAdd + ") to this: " + this);
			t.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance#clone()
	 */
	@Override
	public DatabaseChaseInstance clone() {
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		return new DatabaseChaseInstance(classes.clone(), constantsToAtoms, chaseDatabaseInstance);
	}

	@Override
	public Collection<Atom> getFacts() {
		try {
			return this.chaseDatabaseInstance.getCachedFacts();
		} catch (DatabaseException e) {
			throw new RuntimeException("get cached facts failed." + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#merge(uk.ac.ox.cs.pdq.
	 * reasoning.chase.state.ChaseInstance)
	 */
	@Override
	public ChaseInstance merge(ChaseInstance s) throws SQLException {
		Preconditions.checkState(s instanceof DatabaseChaseInstance);

		EqualConstantsClasses classes = this.classes.clone();
		if (!classes.merge(((DatabaseChaseInstance) s).classes))
			return null;

		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		constantsToAtoms.putAll(((DatabaseChaseInstance) s).constantsToAtoms);

		return new DatabaseChaseInstance(classes, constantsToAtoms, chaseDatabaseInstance);
	}

	/**
	 *
	 * (Conjunctive query match definition) If Q′ is a conjunctive query and v is
	 * a chase configuration having elements for each free variable of Q′, then a
	 * homomorphism of Q′ into v mapping each free variable into the corresponding
	 * element is called a match for Q′ in v.
	 * 
	 * @param query
	 *            An input query
	 * @return the list of matches of the input query to the facts of this state.
	 */
	public List<Match> getMatches(ConjunctiveQuery query, Map<Variable, Constant> substitutions) {
		List<Atom> convertedAtoms = new ArrayList<>();
		for (Atom a : query.getAtoms()) {
			Term[] terms = a.getTerms();
			for (int i = 0; i < terms.length; i++) {
				if (substitutions.containsKey(terms[i])) {
					terms[i] = substitutions.get(terms[i]);
				}
			}
			convertedAtoms.add(Atom.create(a.getPredicate(), terms));
		}
		List<Variable> convertedFreeVariables = new ArrayList<>();
		for (Term v : query.getTerms()) {
			if (v.isVariable()) {
				if (!substitutions.containsKey(v)) {
					convertedFreeVariables.add((Variable) v);
				}
			}
		}
		ConjunctiveQuery converted = null;
		if (convertedAtoms.size() == 1)
			converted = ConjunctiveQuery.create(convertedFreeVariables.toArray(new Variable[convertedFreeVariables.size()]), convertedAtoms.get(0));
		else
			converted = ConjunctiveQuery.create(convertedFreeVariables.toArray(new Variable[convertedFreeVariables.size()]),
					(Conjunction) Conjunction.of(convertedAtoms.toArray(new Atom[convertedAtoms.size()])));
		try {
			return chaseDatabaseInstance.answerConjunctiveQuery(converted);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#getTriggers(java.util.
	 * Collection, uk.ac.ox.cs.pdq.db.homomorphism.TriggerProperty,
	 * uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitTofacts)
	 */
	public List<Match> getTriggers(Dependency[] dependencies, TriggerProperty triggerProperty) {
		Preconditions.checkNotNull(dependencies);
		List<Match> results = new ArrayList<>();
		for (Dependency source : dependencies) {
			// gather free variables, and map of predicates to terms.
			Set<Variable> freeVariables = new HashSet<>();
			for (Atom a : source.getBodyAtoms()) {
				for (Term t : a.getTerms()) {
					if (t.isVariable())
						freeVariables.add((Variable) t);
				}
			}
			ConjunctiveQuery leftQuery = null;
			List<Pair<Variable,Variable>> inequalities = new ArrayList<>();
			if (source instanceof EGD) {
				// filter self pointing equalities
				inequalities.add(Pair.of((Variable)source.getHead().getTerms()[0],(Variable)source.getHead().getTerms()[1]));
			}
			if (source.getBodyAtoms().length == 1) {
				// a ConjunctiveQueryWithInequality with an empty list of inequalities is the same as a normal CQ
				leftQuery = ConjunctiveQueryWithInequality.create(freeVariables.toArray(new Variable[freeVariables.size()]), source.getBodyAtoms()[0],inequalities);
			} else {
				leftQuery = ConjunctiveQueryWithInequality.create(freeVariables.toArray(new Variable[freeVariables.size()]), (Conjunction) Conjunction.of(source.getBodyAtoms()),inequalities);
			}

			if (triggerProperty == TriggerProperty.ALL) {
				try {
					results.addAll(replaceFormulaInMatches(source, chaseDatabaseInstance.answerConjunctiveQuery(leftQuery)));
				} catch (DatabaseException e) {
					throw new RuntimeException("getTriggers error: ", e);
				}
			} else if (triggerProperty == TriggerProperty.ACTIVE) {
				List<Atom> rightQueryAtoms = new ArrayList<>();
				// right query will contain the same as the left, plus extra conditions
				rightQueryAtoms.addAll(Arrays.asList(source.getBodyAtoms()));
				ConjunctiveQuery rightQuery = null;
				rightQueryAtoms.addAll(Arrays.asList(source.getHeadAtoms()));
				rightQuery = ConjunctiveQueryWithInequality.create(freeVariables.toArray(new Variable[freeVariables.size()]),
						(Conjunction) Conjunction.of(rightQueryAtoms.toArray(new Atom[rightQueryAtoms.size()])),inequalities);
				try {
					List<Match> queryResults = chaseDatabaseInstance.answerQueryDifferences(leftQuery, rightQuery);
					results.addAll(replaceFormulaInMatches(source, queryResults));
				} catch (DatabaseException e) {
					throw new RuntimeException("getTriggers error: ", e);
				}
			} else {
				throw new RuntimeException("Invalid trigger property: " + triggerProperty);
			}
		}
		return results;
	}

	/**
	 * Updates the formula from a CQ to a dependency
	 * 
	 * @param source
	 * @param toUpdate
	 * @return
	 */
	private Collection<? extends Match> replaceFormulaInMatches(Dependency source, List<Match> toUpdate) {
		List<Match> results = new ArrayList<>();
		for (Match m : toUpdate) {
			results.add(Match.create(source, m.getMapping()));
		}
		return results;
	}

	@Override
	public int hashCode() {
		if (this.hash == null)
			this.hash = Objects.hash(chaseDatabaseInstance);
		return this.hash;
	}

	public int getInstanceId() {
		return this.hashCode();
	}

	public void close() throws Exception {
		if (chaseDatabaseInstance != null) {
			chaseDatabaseInstance.dropDatabase();
			chaseDatabaseInstance.shutdown();
		}
	}

}
