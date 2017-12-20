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
import uk.ac.ox.cs.pdq.util.Utility;

/**
 *
 * A collection of facts produced during chasing. It also keeps a graph of the
 * rule firings that took place during chasing. This implementation keeps the
 * facts produced during chasing in a database. Homomorphisms are detected using
 * the DBMS the stores the chase facts.
 *
 * @author Efthymia Tsamoura
 *
 */
public class DatabaseChaseInstance implements ChaseInstance {
	protected static Logger log = Logger.getLogger(LogicalDatabaseInstance.class);
	protected final LogicalDatabaseInstance canonicalDatabaseInstance; 
	/** The _is failed. */
	private boolean _isFailed = false;

	/**
	 * Keeps the classes of equivalence classes of constants, that are equated in
	 * EGD chasing.
	 */
	protected EqualConstantsClasses classes;

	/**
	 * Maps each constant to the atom and the position inside this atom where it
	 * appears. We need this table when we are applying an EGD chase step.
	 **/
	protected final Multimap<Constant, Atom> constantsToAtoms;

	// used to create an "instanceID" unique per instance
	private Integer hash = null;
	
	protected Set<String> existingIndices =  new LinkedHashSet<String>();

	
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
			canonicalDatabaseInstance = ((LogicalDatabaseInstance)connection).clone(GlobalCounterProvider.getNext("DatabaseInstanceId"));
		} catch (DatabaseException e) {
			throw new RuntimeException("database failure",e);
		}
		this.addFacts(Sets.newHashSet(uk.ac.ox.cs.pdq.reasoning.chase.Utility.applySubstitution(query, Utility.generateCanonicalMapping(query)).getAtoms()));
		this.classes = new EqualConstantsClasses();
		try {
			this.constantsToAtoms = ReasonerUtility.createdConstantsMap(canonicalDatabaseInstance.getCachedFacts());
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		this.initDatabase();
		//this.indexConstraints();
		//this.indexLastAttributeOfAllRelations();
	}

	@Override
	public void deleteFacts(Collection<Atom> facts) {
		try {
			canonicalDatabaseInstance.deleteFacts(facts);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
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
			canonicalDatabaseInstance = ((LogicalDatabaseInstance)connection).clone(GlobalCounterProvider.getNext("DatabaseInstanceId"));
		} catch (DatabaseException e) {
			throw new RuntimeException("database failure",e);
		}
		Preconditions.checkNotNull(facts);
		this.addFacts(facts);
		this.classes = new EqualConstantsClasses();
		try {
			this.constantsToAtoms = ReasonerUtility.createdConstantsMap(canonicalDatabaseInstance.getCachedFacts());
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		
		this.initDatabase();
		
		//this.indexConstraints();
		//this.indexLastAttributeOfAllRelations();
	}
	
	
	/** Cloning constructor.
	 * @param connection
	 * @throws SQLException
	 */
	private DatabaseChaseInstance(EqualConstantsClasses classes, Multimap<Constant, Atom> constants, DatabaseManager connection) {
			try {
				canonicalDatabaseInstance = ((LogicalDatabaseInstance)connection).clone(GlobalCounterProvider.getNext("DatabaseInstanceId"));
			} catch (DatabaseException e) {
				throw new RuntimeException("database failure",e);
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
			canonicalDatabaseInstance = ((LogicalDatabaseInstance)connection).clone(GlobalCounterProvider.getNext("DatabaseInstanceId"));
		} catch (DatabaseException e) {
			throw new RuntimeException("database failure",e);
		}
		Preconditions.checkNotNull(facts);
		Preconditions.checkNotNull(classes);
		Preconditions.checkNotNull(constants);
		try {
			canonicalDatabaseInstance.addFacts(facts);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		this.classes = classes;
		this.constantsToAtoms = constants;
	}
	private void initDatabase() {
		Relation equality = this.createDatabaseEqualityRelation();
		try {
			// ADD EQUALITY RELATION
			canonicalDatabaseInstance.addRelation(equality);
			
			// CREATE INDICES FOR DEPENDENCIES
			//TOCOMMENT add constraint insertion into database manager
//			for (Dependency constraint : canonicalDatabaseInstance.getSchema().getDependencies()) {
//				
//			}
			
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

//	/**
//	 * Creates indices in the RDBMS for all join positions in the dependencies'
//	 * bodies
//	 * 
//	 * @throws SQLException
//	 */
//	public void indexConstraints() throws SQLException {
//		List<String> statementBuffer = new ArrayList<>();
//		try {
//			Statement sqlStatement = canonicalDatabaseInstance.getDatabaseConnection().getSynchronousConnections().get(0).createStatement();
//			Relation equalityRelation = this.createDatabaseEqualityRelation();
//			canonicalDatabaseInstance.getDatabaseConnection().getRelationNamesToDatabaseTables().put(QNames.EQUALITY.toString(), equalityRelation);
//			String statement = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createTableStatement(equalityRelation);
//			sqlStatement.addBatch(statement);
//			statementBuffer.add(statement);
//			// Create indices for the joins in the body of the dependencies
//			Set<String> joinIndexes = Sets.newLinkedHashSet();
//			for (Dependency constraint : canonicalDatabaseInstance.getDatabaseConnection().getSchema().getDependencies())
//				joinIndexes.addAll(canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder()
//						.setupIndices(false, canonicalDatabaseInstance.getDatabaseConnection().getRelationNamesToDatabaseTables(), constraint, this.existingIndices).getLeft());
//			for (String b : joinIndexes) {
//				sqlStatement.addBatch(b);
//				statementBuffer.add(b);
//			}
//			sqlStatement.executeBatch();
//		} catch (SQLException e) {
//			System.err.println("Error while executing commands: " + statementBuffer);
//			if (e.getNextException() != null)
//				e.getNextException().printStackTrace();
//			else
//				e.printStackTrace();
//			throw e;
//		}
//	}

//	/**
//	 * Creates indices in the RDBMS for the last position of every relation schema
//	 * 
//	 * @throws SQLException
//	 */
//	private void indexLastAttributeOfAllRelations() throws SQLException {
//		Statement sqlStatement = canonicalDatabaseInstance.getDatabaseConnection().getSynchronousConnections().get(0).createStatement();
//		for (Relation relation : canonicalDatabaseInstance.getDatabaseConnection().getRelationNamesToDatabaseTables().values())
//			sqlStatement.addBatch(canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createColumnIndexStatement(relation, relation.getAttribute(relation.getArity() - 1)));
//		sqlStatement.executeBatch();
//	}

	/**
	 * Creates an "equality" relation for storing terms in the chase that are
	 * equated by EGDs
	 * 
	 * @return
	 */
	private Relation createDatabaseEqualityRelation() {
		String attrPrefix = "x";
		//Attribute Fact = Attribute.create(Integer.class, "InstanceID");
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
			// Formula left = grounded.getChild(0);
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
			// Formula left = grounded.getChild(0);
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
			// make sure equality atoms are always added in pairs ( EQUALITY(a,b) should have a pair EQUALITY(b,a) )
			for (Atom factToAdd : factsToAdd) {
				if ("Equality".equalsIgnoreCase(factToAdd.getPredicate().getName())) {
					Atom pair = Atom.create(factToAdd.getPredicate(),new Term[] {factToAdd.getTerms()[1],factToAdd.getTerms()[0]});
					if (!newFacts.contains(pair)) {
						newFacts.add(pair);
					}
				}
			}
			canonicalDatabaseInstance.addFacts(newFacts);
		} catch (Throwable t) {
			System.err.println("Could not add facts ("+factsToAdd+") to this: " + this);
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
		return new DatabaseChaseInstance(classes.clone(), constantsToAtoms,canonicalDatabaseInstance);
	}

	@Override
	public Collection<Atom> getFacts() {
		try {
			return this.canonicalDatabaseInstance.getCachedFacts();
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

		return new DatabaseChaseInstance(classes, constantsToAtoms, canonicalDatabaseInstance);
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
//		Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();
//		// Create a new query out of each input query that references only the cleaned
//		// predicates
//		ConjunctiveQuery converted = this.convert(query);
//		// Create an SQL statement for the cleaned query
//		Pair<String, LinkedHashMap<String, Variable>> pair = createSQLQuery(converted, substitutions);
//		queries.add(Triple.of((Formula) query, pair.getLeft(), pair.getRight()));
		List<Atom> convertedAtoms = new ArrayList<>();
		for (Atom a: query.getAtoms()) {
			Term[] terms = a.getTerms();
			for (int i = 0; i < terms.length; i++) {
				if (substitutions.containsKey(terms[i])) {
					terms[i] = substitutions.get(terms[i]);
				}
			}
			convertedAtoms.add(Atom.create(a.getPredicate(), terms));
		}
		List<Variable> convertedFreeVariables = new ArrayList<>();
		for (Term v: query.getTerms()) {
			if(v.isVariable()) {
				if (!substitutions.containsKey(v)) {
					convertedFreeVariables.add((Variable)v);
				} 
			}
		}
		ConjunctiveQuery converted = null;
		if (convertedAtoms.size() == 1)
			converted = ConjunctiveQuery.create(convertedFreeVariables.toArray(new Variable[convertedFreeVariables.size()]), 
				convertedAtoms.get(0));
		else 
			converted = ConjunctiveQuery.create(convertedFreeVariables.toArray(new Variable[convertedFreeVariables.size()]), 
					(Conjunction)Conjunction.of(convertedAtoms.toArray(new Atom[convertedAtoms.size()])));
		try {
			return canonicalDatabaseInstance.answerConjunctiveQuery(converted);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Same as above but it does not replaces the free variables in the query with
	 * the canonical constants. This function is only used for unit testing.
	 */
	public List<Match> getMatchesNoSubstitution(ConjunctiveQuery query) {
		try {
			return canonicalDatabaseInstance.answerConjunctiveQuery(query);
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
		// Create a new query out of each input query that references only the clean
		// predicates
		List<Match> results = new ArrayList<>();
		for (Dependency source : dependencies) {
			// gather free variables, and map of predicates to terms.
			Set<Variable> freeVariables = new HashSet<>();
			for (Atom a: source.getBodyAtoms()) {
				for (Term t: a.getTerms()) {
					if (t.isVariable())
						freeVariables.add((Variable)t);
				}
			}
			ConjunctiveQuery leftQuery = null;
			if (source.getBodyAtoms().length == 1) {
				leftQuery = ConjunctiveQuery.create(freeVariables.toArray(new Variable[freeVariables.size()]),source.getBodyAtoms()[0]);
			} else {
				leftQuery = ConjunctiveQuery.create(freeVariables.toArray(new Variable[freeVariables.size()]),(Conjunction)Conjunction.of(source.getBodyAtoms()));
			}
			
			if (triggerProperty == TriggerProperty.ALL) {
				try {
					results.addAll(updateFormula(source,filterSelfEqualityResults(canonicalDatabaseInstance.answerConjunctiveQuery(leftQuery),source)));
				} catch (DatabaseException e) {
					throw new RuntimeException("getTriggers error: " ,e);
				}
			}
			else if (triggerProperty == TriggerProperty.ACTIVE) {
				List<Atom> rightQueryAtoms = new ArrayList<>();
				// right query will contain the same as the left, plus extra conditions
				rightQueryAtoms.addAll(Arrays.asList(source.getBodyAtoms()));
				ConjunctiveQuery rightQuery= null;
				rightQueryAtoms.addAll(Arrays.asList(source.getHeadAtoms()));
				rightQuery = ConjunctiveQuery.create(freeVariables.toArray(new Variable[freeVariables.size()]),(Conjunction)Conjunction.of(rightQueryAtoms.toArray(new Atom[rightQueryAtoms.size()])));
				try {
					List<Match> queryResults = canonicalDatabaseInstance.answerQueryDifferences(leftQuery, rightQuery);
					// filter self pointing equalities
					results.addAll(updateFormula(source,filterSelfEqualityResults(queryResults,source)));
				} catch (DatabaseException e) {
					throw new RuntimeException("getTriggers error: " ,e);
				}		
			} else {
				throw new RuntimeException("Invalid trigger property: " + triggerProperty);
			}
		}
		return results;
	}
	/** Updates the formula from a CQ to a dependency
	 * @param source
	 * @param toUpdate
	 * @return
	 */
	private Collection<? extends Match> updateFormula(Dependency source, List<Match> toUpdate) {
		List<Match> results = new ArrayList<>();
		for (Match m: toUpdate) {
			results.add(Match.create(source, m.getMapping()));
		}
		return results;
	}

	/** In case we have a self join in the CQ we need to make sure every match connects two different facts, and remove the case when the left and right side of an equality is the same.
	 * @param list
	 * @param source
	 * @return
	 */
	private List<Match> filterSelfEqualityResults(List<Match> list, Dependency source) {
		if (source instanceof EGD) {
			List<Match> results = new ArrayList<>();
			Term leftVariable = source.getHead().getTerms()[0];
			Term rightVariable = source.getHead().getTerms()[1];
			for (Match m:list) {
				if (m.getMapping().get(leftVariable) != m.getMapping().get(rightVariable)) {
					results .add(m);
				}
			}
			return results;
		} else {
			return list;
		}
	}
	/**
	 * Converts the query extending its atoms to include an extra variable
	 * (different variable per atom) at the position where the instanceID attribute
	 * lies on the corresponding relation's extended schema . By convention, the
	 * underlying schema has been extended such that each relation contains an extra
	 * attribute used to store the id of the DatabaseChaseInstance every fact
	 * belongs to. This method transforms the query according to this extended
	 * schema.
	 * 
	 * @param source
	 *            the input query
	 * @return the transformed query where each atom is extended by one extra fresh
	 *         variable.
	 */
//	private ConjunctiveQuery convert(ConjunctiveQuery source) {
//		Atom[] body = extendAtomsWithInstanceIDAttribute(source.getAtoms(), 0);
//		if (body.length == 1)
//			return ConjunctiveQuery.create(((ConjunctiveQuery) source).getFreeVariables(), body[0]);
//		else
//			return ConjunctiveQuery.create(((ConjunctiveQuery) source).getFreeVariables(), (Conjunction) Conjunction.of(body));
//	}

	/**
	 * Creates an SQL statement that detects homomorphisms of the input dependency
	 * to facts kept in a database.
	 *
	 * @param source
	 *            the input dependency; the sql query return detects its triggers
	 * @param t
	 *            determines whether the triggers detected by the returned query are
	 *            active or not
	 * @param l
	 *            determines whether the triggers detected by the returned query
	 *            will be limited in facts of THIS DatabaseChaseInstance or of ALL
	 *            instances.
	 * @return This method returns a pair containing (a) an SQL query that detects
	 *         triggers (active or not depends on t) for the input dependency, and
	 *         (b) a map with the attributes to be projected. 
	 */
//	public Pair<String, LinkedHashMap<String, Variable>> createSQLQuery(Dependency dep, TriggerProperty t,Schema schema) {
//		boolean isEGD = (dep instanceof EGD);
//		int freshcounter = 0;
//		Atom[] extendedBodyAtoms = extendAtomsWithInstanceIDAttribute(dep.getBodyAtoms(), freshcounter);
//		Atom[] extendedHeadAtoms = extendAtomsWithInstanceIDAttribute(dep.getHeadAtoms(), freshcounter + extendedBodyAtoms.length + 1);
//		Atom[] allExtendedAtoms = new Atom[extendedBodyAtoms.length + extendedHeadAtoms.length];
//		System.arraycopy(extendedBodyAtoms, 0, allExtendedAtoms, 0, extendedBodyAtoms.length);
//		System.arraycopy(extendedHeadAtoms, 0, allExtendedAtoms, extendedBodyAtoms.length, extendedHeadAtoms.length);
//
//		String query = "";
//		FromCondition from = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createFromStatement(extendedBodyAtoms);
//		SelectCondition projections = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createProjections(extendedBodyAtoms,canonicalDatabaseInstance.getDatabaseConnection());
//		WhereCondition where = new WhereCondition();
//		WhereCondition equalities = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createAttributeEqualities(extendedBodyAtoms,schema);
//		WhereCondition constantEqualities = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createEqualitiesWithConstants(extendedBodyAtoms,schema);
//
//		WhereCondition factproperties = null;
//		if (facts != null && !facts.isEmpty())
//			factproperties = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().enforceStateMembership(extendedBodyAtoms, canonicalDatabaseInstance.getDatabaseConnection().getRelationNamesToDatabaseTables(),
//					this.facts);
//		else
//			factproperties = new WhereCondition();
//
//		if (isEGD) {
//			WhereCondition activenessFilter = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createEGDActivenessFilter((EGD) dep, extendedBodyAtoms,schema);
//			if (!activenessFilter.isEmpty())
//				where.addCondition(activenessFilter);
//			if (((EGD) dep).isFromFunctionalDependency()) {
//				WhereCondition egdProperties = uk.ac.ox.cs.pdq.reasoning.chase.Utility.createConditionForEGDsCreatedFromFunctionalDependencies(extendedBodyAtoms,
//						canonicalDatabaseInstance.getDatabaseConnection().getRelationNamesToDatabaseTables(), canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder());
//				if (egdProperties != null)
//					where.addCondition(egdProperties);
//			}
//		}
//		where.addCondition(equalities);
//		where.addCondition(constantEqualities);
//		where.addCondition(factproperties);
//
//		query = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().buildSQLQuery(projections, from, where);
//
//		if (t.equals(TriggerProperty.ACTIVE)) {
//			FromCondition from2 = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createFromStatement(extendedHeadAtoms);
//			SelectCondition nestedProjections = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createProjections(extendedHeadAtoms,canonicalDatabaseInstance.getDatabaseConnection());
//			WhereCondition predicates2 = new WhereCondition();
//			WhereCondition nestedAttributeEqualities = (!(isEGD)) ? canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createAttributeEqualities(allExtendedAtoms,schema)
//					: uk.ac.ox.cs.pdq.reasoning.chase.Utility.createNestedAttributeEqualitiesForActiveTriggers(extendedBodyAtoms, extendedHeadAtoms,
//							canonicalDatabaseInstance.getDatabaseConnection());
//			WhereCondition nestedConstantEqualities = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().createEqualitiesWithConstants(allExtendedAtoms,schema);
//			predicates2.addCondition(nestedAttributeEqualities);
//			predicates2.addCondition(nestedConstantEqualities);
//
//			WhereCondition nestedFactproperties = null;
//			if (facts != null && !facts.isEmpty())
//				nestedFactproperties = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().enforceStateMembership(extendedHeadAtoms,
//						canonicalDatabaseInstance.getDatabaseConnection().getRelationNamesToDatabaseTables(), this.facts );
//			else
//				nestedFactproperties = new WhereCondition();
//			predicates2.addCondition(nestedFactproperties);
//
//			String nestedQuery = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().buildSQLQuery(nestedProjections, from2, predicates2);
//
//			query = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder().nestQueries(query, where, nestedQuery);
//		}
//
//		log.trace(dep);
//		log.trace(query);
//		log.trace("\n\n");
//		return Pair.of(query, projections.getInternalMap());
//	}

	/**
	 * Creates an SQL statement that detects homomorphisms of the input query to
	 * facts kept in a database.
	 * 
	 * @param source
	 *            the input query
	 * @param l
	 *            determines whether the answers of the query will be limited in
	 *            facts of THIS DatabaseChaseInstance or of ALL instances.
	 * @param finalProjectionMapping
	 *            a mapping of the query free variables to canonical constants
	 * @return This method returns a pair containing (a) an SQL query that detects
	 *         homomorphisms of the input query, and (b) a map with the attributes
	 *         to be projected. 
	 */
//	public Pair<String, LinkedHashMap<String, Variable>> createSQLQuery(ConjunctiveQuery source, Map<Variable, Constant> finalProjectionMapping) {
//		String query = "";
//		SQLStatementBuilder stb = canonicalDatabaseInstance.getDatabaseConnection().getSQLStatementBuilder();
//		FromCondition from = stb.createFromStatement(source.getAtoms());
//		SelectCondition projections = stb.createProjections(source.getAtoms(),canonicalDatabaseInstance.getDatabaseConnection());
//		WhereCondition where = new WhereCondition();
//		WhereCondition equalities = stb.createAttributeEqualities(source.getAtoms(),canonicalDatabaseInstance.getDatabaseConnection().getSchema());
//		WhereCondition constantEqualities = stb.createEqualitiesWithConstants(source.getAtoms(),canonicalDatabaseInstance.getDatabaseConnection().getSchema());
//		WhereCondition equalitiesWithProjectedVars = stb.createEqualitiesRespectingInputMapping(source.getAtoms(), finalProjectionMapping,canonicalDatabaseInstance.getDatabaseConnection().getSchema());
//
//		WhereCondition factproperties = null;
//		if (facts != null && !facts.isEmpty())
//			factproperties = stb.enforceStateMembership(source.getAtoms(), canonicalDatabaseInstance.getDatabaseConnection().getRelationNamesToDatabaseTables(),this.facts);
//		else
//			factproperties = new WhereCondition();
//
//		where.addCondition(equalities);
//		where.addCondition(constantEqualities);
//		where.addCondition(equalitiesWithProjectedVars);
//		where.addCondition(factproperties);
//
//		query = stb.buildSQLQuery(projections, from, where);
//
//		log.trace(source);
//		log.trace(query);
//		log.trace("\n\n");
//		return Pair.of(query, projections.getInternalMap());
//	}

	@Override
	public int hashCode() {
		if (this.hash == null)
			this.hash = Objects.hash(canonicalDatabaseInstance);
		return this.hash;
	}

	public int getInstanceId() {
		return this.hashCode();
	}

	public void close() throws Exception {
		if (canonicalDatabaseInstance!=null) {
			canonicalDatabaseInstance.dropDatabase();
			canonicalDatabaseInstance.shutdown();
		}
	}

}
