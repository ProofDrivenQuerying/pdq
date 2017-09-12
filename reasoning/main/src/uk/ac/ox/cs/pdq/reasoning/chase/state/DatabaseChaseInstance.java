package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.sql.FromCondition;
import uk.ac.ox.cs.pdq.db.sql.SelectCondition;
import uk.ac.ox.cs.pdq.db.sql.WhereCondition;
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
import uk.ac.ox.cs.pdq.util.Utility;

/**
 *
 * A collection of facts produced during chasing.
 * It also keeps a graph of the rule firings that took place during chasing.
 * This implementation keeps the facts produced during chasing in a database.
 * Homomorphisms are detected using the DBMS the stores the chase facts. 
 *
 * @author Efthymia Tsamoura
 *
 */
public class DatabaseChaseInstance extends DatabaseInstance implements ChaseInstance  {
	/** The _is failed. */
	private boolean _isFailed = false;

	/**  The state's facts. */
	protected Collection<Atom> facts = new LinkedHashSet<Atom>();

	/**  Keeps the classes of equivalence classes of constants, that are equated in EGD chasing. */
	protected EqualConstantsClasses classes;

	/** Maps each constant to the atom and the position inside this atom where it appears. 
	 * We need this table when we are applying an EGD chase step. **/
	protected final Multimap<Constant, Atom> constantsToAtoms;
	
	//used to create an "instanceID" unique per instance
	private Integer hash = null;

	/**
	 * Instantiates a new DatabaseChaseInstance in order to chase a (canonical database of a query).
	 * It creates and executes SQL in order to insert the facts of the canonical database of the query 
	 * in the underlying instance. By convention the schema of the input query is assumed 
	 * to contain an instanceID attribute, where each fact will 
	 * store the instanceID of the instance that contains it. This constructor also performs indexing
	 * for all positions that contain joins in the dependencies, as well as the last position of all
	 * relations. Additionally the constructor creates an "Equality" relation (used in EGD chasing), 
	 * and initialises internal maps needed for EGD chasing.
	 * 
	 * @param query The query whose canonical database is to be inserted and then chased
	 * @param connection The database connection to the RDBMS
	 * @throws SQLException Exception risen if something goes wrong with the SQL that inserts the facts.
	 */
	public DatabaseChaseInstance(ConjunctiveQuery query, DatabaseConnection connection) throws SQLException {
		super(connection);
		this.addFacts(Sets.newHashSet(uk.ac.ox.cs.pdq.reasoning.chase.Utility.applySubstitution(query, Utility.generateCanonicalMapping(query)).getAtoms()));
		this.classes = new EqualConstantsClasses();
		this.constantsToAtoms = createdConstantsMap(this.facts);
		this.indexConstraints();
		this.indexLastAttributeOfAllRelations();
	}

	/**
	 * Instantiates a new database list state.
	 *
	 * @param chaseState the chaseState
	 * @param facts the facts
	 */
	public DatabaseChaseInstance(Collection<Atom> facts, DatabaseConnection connection) throws SQLException {
		super(connection);
		Preconditions.checkNotNull(facts);
		this.addFacts(facts);
		this.classes = new EqualConstantsClasses();
		this.constantsToAtoms = createdConstantsMap(this.facts);
		this.indexConstraints();
		this.indexLastAttributeOfAllRelations();
	}

	/**
	 * Instantiates a new DatabaseChaseInstance in order to chase a set of facts.
	 * This protected constructor does not(!) add the facts into the RDBMS. 
	 * Using this constructor one would need to call addFacts explicitly. 
	 * It also does not perform any constraint or attribute indexing. 
	 * It accepts as input the sets internal maps needed for EGD chasing.
	 * 
	 * 
	 * @param facts The facts to inserted and later chased
	 * @param classes an EqualConstantsClasses object that keeps multiple classes of equal constants created during EGD chasing.
	 * @param constantsToAtoms a map of each constant to the atom and the position inside this atom where it appears, used in EGD chasing.
	 * @param connection The database connection to the RDBMS
	 */
	protected DatabaseChaseInstance(
			Collection<Atom> facts,
			EqualConstantsClasses classes,
			Multimap<Constant,Atom> constants,
			DatabaseConnection connection 
			) {
		super(connection);
		Preconditions.checkNotNull(facts);
		Preconditions.checkNotNull(classes);
		Preconditions.checkNotNull(constants);
		this.facts = facts;
		this.classes = classes;
		this.constantsToAtoms = constants; 
	}

	/**
	 *
	 * 
	 * @param facts
	 * @return a map of each constant to the atom and the position inside this atom where it appears. 
	 * An exception is thrown when there is an equality in the input
	 */
	//TOCOMMENT: What does this do exactly? where is it used?
	protected static Multimap<Constant,Atom> createdConstantsMap(Collection<Atom> facts) {
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		for(Atom fact:facts) {
			Preconditions.checkArgument(!fact.isEquality());
			for(Term term:fact.getTerms()) 
				constantsToAtoms.put((Constant)term, fact);
		}
		return constantsToAtoms;
	}

	/**
	 * Creates indices in the RDBMS for all join positions in the dependencies' bodies
	 * 
	 * @throws SQLException
	 */
	public void indexConstraints() throws SQLException {
		List<String> statementBuffer = new ArrayList<>(); 
		try {
			Statement sqlStatement = this.getDatabaseConnection().getSynchronousConnections().get(0).createStatement();
			Relation equalityRelation = this.createDatabaseEqualityRelation();
			this.databaseConnection.getRelationNamesToDatabaseTables().put(QNames.EQUALITY.toString(), equalityRelation);
			String statement = this.databaseConnection.getSQLStatementBuilder().createTableStatement(equalityRelation);
			sqlStatement.addBatch(statement);
			statementBuffer.add(statement);
			//Create indices for the joins in the body of the dependencies
			Set<String> joinIndexes = Sets.newLinkedHashSet();
			for (Dependency constraint:this.databaseConnection.getSchema().getDependencies()) 
				joinIndexes.addAll(this.databaseConnection.getSQLStatementBuilder().setupIndices(false, this.databaseConnection.getRelationNamesToDatabaseTables(), constraint, this.existingIndices).getLeft());
			for (String b: joinIndexes) { 
				sqlStatement.addBatch(b);
				statementBuffer.add(b);
			}
			sqlStatement.executeBatch();
		}catch(SQLException e) {
			System.err.println("Error while executing commands: " + statementBuffer);
			if (e.getNextException()!=null)
				e.getNextException().printStackTrace();
			else
				e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Creates indices in the RDBMS for the last position of every relation schema
	 * 
	 * @throws SQLException
	 */
	private void indexLastAttributeOfAllRelations() throws SQLException {
		Statement sqlStatement = this.databaseConnection.getSynchronousConnections().get(0).createStatement();
		for(Relation relation:this.databaseConnection.getRelationNamesToDatabaseTables().values())
			sqlStatement.addBatch(this.databaseConnection.getSQLStatementBuilder().createColumnIndexStatement(relation, relation.getAttribute(relation.getArity()-1)));
		sqlStatement.executeBatch();
	}

	/**
	 * Creates an "equality" relation for storing terms in the chase that are equated by EGDs
	 * 
	 * @return
	 */
	private Relation createDatabaseEqualityRelation() {	
		String attrPrefix = "x";
		Attribute Fact = Attribute.create(Integer.class, "InstanceID");
		Attribute[] attributes = new Attribute[]{Attribute.create(String.class, attrPrefix + 0), Attribute.create(String.class, attrPrefix + 1), Fact};
		return Relation.create(QNames.EQUALITY.toString(), attributes, true);
	}	

	/* 
	 * ??? What does false here mean?
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#chaseStep(java.util.Collection)
	 */
	@Override
	public boolean chaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		if(!matches.isEmpty()) {
			Match match = matches.iterator().next();
			if(match.getFormula() instanceof EGD) {
				return this.EGDchaseStep(matches);
			}
			else if(match.getFormula() instanceof TGD) {
				return this.TGDchaseStep(matches);
			}
		}
		return false;
	}

	/**
	 * Applies chase steps for TGDs. 
	 * An exception is thrown when a match does not come from a TGD
	 * @param matches the input triggers (and dependencies) to be applied
	 * @return //TOCOMMENT: WHY DOESN'T THIS RETURN ALWAYS TRUE??
	 */
	public boolean TGDchaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Atom> newFacts = new LinkedHashSet<>();
		for(Match match:matches) {
			Dependency dependency = (Dependency) match.getFormula();
			Preconditions.checkArgument(dependency instanceof TGD, "EGDs are not allowed inside TGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Implication grounded = uk.ac.ox.cs.pdq.reasoning.chase.Utility.ground(dependency, mapping, true);
//			Formula left = grounded.getChild(0);
			Formula right = grounded.getChild(1);
			//Add information about new facts to constantsToAtoms
			for(Atom atom:right.getAtoms()) {
				for(Term term:atom.getTerms()) 
					this.constantsToAtoms.put((Constant)term, atom);
			}
			newFacts.addAll(Arrays.asList(right.getAtoms()));
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
	 * 
	 * @param matches the input triggers (and dependencies) to be applied
	 * @return  true, if the chase step is successful
	 */
	public boolean EGDchaseStep(Collection<Match> matches) {
		Preconditions.checkNotNull(matches);
		Collection<Atom> newFacts = new LinkedHashSet<>();
		//For each fired EGD update the classes of equal constants
		//and find all constants with updated representatives
		//Maps each constant to its new representative  
		Map<Constant,Constant> obsoleteToRepresentative = Maps.newHashMap();
		for(Match match:matches) {
			Dependency dependency = (Dependency) match.getFormula();
			Preconditions.checkArgument(dependency instanceof EGD, "TGDs are not allowed inside EGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Implication grounded = uk.ac.ox.cs.pdq.reasoning.chase.Utility.fire(dependency, mapping);
//			Formula left = grounded.getChild(0);
			Formula right = grounded.getChild(1);
			for(Atom atom:right.getAtoms()) {
				//Find all the constants that each constant in the equality is representing 
				obsoleteToRepresentative.putAll(this.updateEqualConstantClasses(atom));
				if(this._isFailed) 
					return false;
				
				//Equalities should be added into the database 
				newFacts.addAll(Arrays.asList(right.getAtoms()));
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
			Term[] newTerms = new Term[fact.getNumberOfTerms()];
			for(int termIndex = 0; termIndex < fact.getNumberOfTerms(); ++termIndex) {
				EqualConstantsClass cls = this.classes.getClass(fact.getTerm(termIndex));
				newTerms[termIndex] = cls != null ? cls.getRepresentative() : fact.getTerm(termIndex);
			}
			Atom newFact = Atom.create(fact.getPredicate(), newTerms);
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
		deleteFacts(obsoleteFacts);
		this.addFacts(newFacts);
		return !this._isFailed;
	}

	/**
	 * Updates the classes of equal constants and returns the constants whose representative changed, for use in fact-propagation 
	 * @param atom an equality atom
	 * @return a map mapping each constant that is to be replaced by its representative to the a latter, 
	 * after taking the input equality into account.
	 */
	public Map<Constant,Constant> updateEqualConstantClasses(Atom atom) {
		Preconditions.checkArgument(atom.isEquality());
		//Maps each constant to its new representative  
		Map<Constant,Constant> obsoleteToRepresentative = Maps.newHashMap();

		Constant c_l = (Constant) atom.getTerm(0);
		//Find the class of the first input constant and its representative
		EqualConstantsClass class_l = this.classes.getClass(c_l);
		//Find all constants of this class and its representative
		Collection<Term> constants_l = Sets.newHashSet();
		Term old_representative_l = null;
		if(class_l != null) {
			constants_l.addAll(class_l.getConstants());
			old_representative_l = class_l.getRepresentative();
		}

		Constant c_r = (Constant) atom.getTerm(1);
		//Find the class of the first input constant and its representative
		EqualConstantsClass class_r = this.classes.getClass(c_r);
		//Find all constants of this class and its representative
		Collection<Term> constants_r = Sets.newHashSet();
		Term old_representative_r = null;
		if(class_r != null) {
			constants_r.addAll(class_r.getConstants());
			old_representative_r = class_r.getRepresentative();
		}

		boolean successfull;
		//if already the same class nothing to do
		if(class_r != null && class_l != null && class_r.equals(class_l)) {
			successfull = true;
		}
		else {
			//does the actual update of the representatives, or returns false if the update cannot be done and the chase fails
			successfull = this.classes.add(atom);
			if(!successfull) {
				this._isFailed = true;
				return Maps.newHashMap();
			}
			
			// doesn't matter left or right representative, the new one should always be the same.
			Constant newRepresentative = (Constant)this.classes.getClass(c_r).getRepresentative();
			
			//Detect all constants whose representative will change 
			if(old_representative_l == null && old_representative_r == null) {
				// Both new, no classes to update
				if(newRepresentative.equals(c_l)) {
					obsoleteToRepresentative.put(c_r, newRepresentative);
				}
				else if(newRepresentative.equals(c_r)) {
					obsoleteToRepresentative.put(c_l, newRepresentative);
				} 
			}
			else if(old_representative_l == null) {
				// left side is new
				if (!newRepresentative.equals(c_l)) obsoleteToRepresentative.put(c_l, newRepresentative);
				
				if (!old_representative_r.equals(newRepresentative)) {
					// the new element on the left become the representative for the whole class on the right
					for(Term term:constants_r) {
						obsoleteToRepresentative.put((Constant)term, newRepresentative);
					}
				}
			}
			else if(old_representative_r == null) {
				// right side is new
				if (!newRepresentative.equals(c_r)) obsoleteToRepresentative.put(c_r, newRepresentative);
				if (!old_representative_l.equals(newRepresentative)) {
					// the new element on the right become the representative for the whole class on the left
					for(Term term:constants_l) {
						obsoleteToRepresentative.put((Constant)term, newRepresentative);
					}
				}
			} else {
				// both side had representative we need to update according who is the winner
				if (newRepresentative.equals(old_representative_r)) {
					// the right side constant's representative become the king of all classes
					for(Term term:constants_l) {
						obsoleteToRepresentative.put((Constant)term, newRepresentative);
					}
					obsoleteToRepresentative.put(c_l, newRepresentative);
				} else {
					// the left side constant's representative become the king of all classes
					for(Term term:constants_r) {
						obsoleteToRepresentative.put((Constant)term, newRepresentative);
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


	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#isFailed()
	 */
	@Override
	public boolean isFailed() {
		return this._isFailed;
	}

//	public Multimap<Constant, Atom> getConstantsToAtoms() {
//		return this.constantsToAtoms;
//	}

	/* (non-Javadoc)
	 * add the input set of facts to the database 
	 */
	@Override
	public void addFacts(Collection<Atom> facts) {
		try {
			LinkedHashSet<Atom> newFacts = new LinkedHashSet<Atom>();
			LinkedHashSet<Atom> factsToAddToTheDatabase = new LinkedHashSet<Atom>();
			newFacts.addAll(this.facts);
/* copy existing facts */
			for (Atom fact: facts) {
/* loop through input facts , checking if they overlap existing ones 
 * TODO: THIS SHOULD EVENTUALLY BE AN ADDITIONAL CHECK SINCE THE DB SHOULD CHECK UNIQUENESS AND DISCARD DUPLIACATES
 */
				if (!newFacts.contains(fact)) {
					factsToAddToTheDatabase.add(fact);
				}
			}
			newFacts.addAll(factsToAddToTheDatabase);
/* The actual adding of the facts to the db */
			super.addFacts(extendFactsUsingInstanceID(factsToAddToTheDatabase));
			this.facts = newFacts;
		}catch(Throwable t) {
			System.err.println("Could not add facts: " + this.facts);
			t.printStackTrace();
		}
		
	}
	
	/**
	 * Takes a collection of facts and extends them (returns facts with all the same plus an extra term)
	 * with the id of this instance.
	 * @param facts the collection of facts to be changed
	 * @return the input facts each one extended by one term: the instance id
	 */
	private Collection<Atom> extendFactsUsingInstanceID(Collection<Atom> facts) {		
		Collection<Atom> extendedFacts = new LinkedHashSet<Atom>();
		for(Atom f: facts) {
			Term[] terms = new Term[f.getNumberOfTerms()+1]; 
			System.arraycopy(f.getTerms(), 0, terms, 0, f.getNumberOfTerms());
			terms[terms.length-1] = TypedConstant.create(f.getId()/*this.getInstanceId()*/);
			extendedFacts.add(Atom.create(f.getPredicate(), terms)); 
		}

		return extendedFacts;
	}
	
	/**
	 * Converts the input list of atoms extending them to include an extra attribute at the end (different variable per atom).
	 * By convention, the underlying schema has been extended such that each relation contains an extra attribute used to
	 * store the id of the DatabaseChaseInstance every fact belongs to. This method transforms the input according to
	 * this extended schema. When this method is called more than once for the same formula, the variablecount argument should
	 * be increased so that the new invented variables do not accidentally join in the formula. The variablecount participates
	 * in the fresh name of the new variables.
	 * 
	 * @param atoms the input list of atoms
	 * @param variablecount a counter that is used to invent variable names. 
	 * @return
	 */
	 
	private Atom[] extendAtomsWithInstanceIDAttribute(Atom[] atoms, int variablecount) {
		Atom[] result = new Atom[atoms.length];
		for(int atomIndex = 0; atomIndex < atoms.length; ++atomIndex) {
			Atom atom = atoms[atomIndex];
			Relation relation = this.databaseConnection.getRelationNamesToDatabaseTables().get(atom.getPredicate().getName());
			try{
				relation.getAttributePosition("InstanceID");
			}
			catch (NullPointerException e) {
				System.out.println(relation + " has no instance id!");
				throw new RuntimeException("InstanceID attribute is missing from the schema");
			}
			Term[] terms = new Term[atom.getNumberOfTerms()+1];
			System.arraycopy(atom.getTerms(), 0, terms, 0, atom.getNumberOfTerms());
			terms[relation.getAttributePosition("InstanceID")] = Variable.create("instance_id" + variablecount++);
			result[atomIndex]= Atom.create(relation, terms);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance#clone()
	 */
	@Override
	public DatabaseChaseInstance clone() {
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		try { 
			return new DatabaseChaseInstance(Sets.newHashSet(this.facts),this.getDatabaseConnection().clone());
		} catch (SQLException e) {
			throw new RuntimeException("Cloning a DatabaseChaseInstance failed due to an SQL exception "+ e);
		}
	}	

	@Override
	public Collection<Atom> getFacts() {
		return this.facts;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#merge(uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance)
	 */
	@Override
	public ChaseInstance merge(ChaseInstance s) throws SQLException {
		Preconditions.checkState(s instanceof DatabaseChaseInstance);
		Collection<Atom> facts =  new LinkedHashSet<>(this.facts);
		facts.addAll(s.getFacts());

		EqualConstantsClasses classes = this.classes.clone();
		if(!classes.merge(((DatabaseChaseInstance)s).classes)) 
			return null;
		
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		constantsToAtoms.putAll(((DatabaseChaseInstance)s).constantsToAtoms);

		return new DatabaseChaseInstance(
				facts, 
				classes,
				constantsToAtoms, this.getDatabaseConnection());
	}

	/**
	 *
	 * (Conjunctive query match definition) If Q′ is a conjunctive query and v is a chase configuration
	 * having elements for each free variable of Q′, then a homomorphism of Q′ into v
	 * mapping each free variable into the corresponding element is called a match for Q′ in v.
	 * @param query
	 * 		An input query
	 * @return
	 * 		the list of matches of the input query to the facts of this state.
	 */
	public List<Match> getMatches(ConjunctiveQuery query, LimitToThisOrAllInstances l) {
		Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();
		//Create a new query out of each input query that references only the cleaned predicates
		ConjunctiveQuery converted = this.convert(query);
		//Create an SQL statement for the cleaned query
		Pair<String, LinkedHashMap<String, Variable>> pair = createSQLQuery(converted, l, query.getSubstitutionOfFreeVariablesToCanonicalConstants());
		queries.add(Triple.of((Formula)query, pair.getLeft(), pair.getRight()));
		return this.answerQueries(queries);
	}
	
	/**
	 * Same as above but it does not replaces the free variables in the query with the canonical constants. This function is only used for unit testing.
	 */
	public List<Match> getMatchesNoSubstitution(ConjunctiveQuery query, LimitToThisOrAllInstances l) {
		Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();
		//Create a new query out of each input query that references only the cleaned predicates
		ConjunctiveQuery converted = this.convert(query);
		//Create an SQL statement for the cleaned query
		Pair<String, LinkedHashMap<String, Variable>> pair = createSQLQuery(converted, l, new HashMap<Variable,Constant>());
		queries.add(Triple.of((Formula)query, pair.getLeft(), pair.getRight()));
		return this.answerQueries(queries);
	}



	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#getTriggers(java.util.Collection, uk.ac.ox.cs.pdq.db.homomorphism.TriggerProperty, uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitTofacts)
	 */
	public List<Match> getTriggers(Dependency[] dependencies, TriggerProperty t, LimitToThisOrAllInstances limitToThisOrAllInstances) {
		Preconditions.checkNotNull(dependencies);
		Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();
		//Create a new query out of each input query that references only the clean predicates
		for(Dependency source:dependencies) {
			Pair<String, LinkedHashMap<String, Variable>> pair = createSQLQuery(source, t, limitToThisOrAllInstances);
			queries.add(Triple.of((Formula)source, pair.getLeft(), pair.getRight()));
		}
		return this.answerQueries(queries);
	}

	/**
	 * Converts the query extending its atoms to include an extra variable (different variable per atom) at the position
	 * where the instanceID attribute lies on the corresponding relation's extended schema .
	 * By convention, the underlying schema has been extended such that each relation contains an extra attribute used to
	 * store the id of the DatabaseChaseInstance every fact belongs to. This method transforms the query according to
	 * this extended schema.
	 * 
	 * @param source the input query
	 * @return  the transformed query where each atom is extended by one extra fresh variable.
	 */
	private ConjunctiveQuery convert(ConjunctiveQuery source) {
		Atom[] body = extendAtomsWithInstanceIDAttribute(source.getAtoms(), 0);
		if(body.length == 1) 
			return ConjunctiveQuery.create(((ConjunctiveQuery) source).getFreeVariables(), body[0]);
		else 
			return ConjunctiveQuery.create(((ConjunctiveQuery) source).getFreeVariables(), (Conjunction) Conjunction.of(body));
	}
	
	/**
	 * Enumeration that holds the option to execute a query limiting the answers to facts of this DatabaseChaseInstance, or to facts of the entire database. 
	 *
	 */
	public static enum LimitToThisOrAllInstances{
		ALL,
		THIS
	}
	
	public void setDatabaseConnection(DatabaseConnection connection) {
		this.databaseConnection = connection;
	}

	/**
	 * Creates an SQL statement that detects homomorphisms of the input dependency to facts kept in a database.
	 *
	 * @param source the input dependency; the sql query return detects its triggers
	 * @param t determines whether the triggers detected by the returned query are active or not
	 * @param l determines whether the triggers detected by the returned query will be limited in facts of THIS DatabaseChaseInstance or of ALL instances.
	 * @param isEGD if the input dependency comes from an EGD. TOCOMMENT:// convert method in getTriggers converts all dependencies to TGDs - WHY? - so we we need this flag
	 * @return This method returns a pair containing (a) an SQL query that detects triggers (active or not depends on t) for the input dependency, 
	 * and (b) a map with the attributes to be projected. TOCOMMENT:// I'm not sure what is in this map exactly.
	 */
	public Pair<String,LinkedHashMap<String,Variable>> createSQLQuery(Dependency dep, TriggerProperty t, LimitToThisOrAllInstances l) {
		boolean isEGD = (dep instanceof EGD);
		int freshcounter = 0;
		Atom[] extendedBodyAtoms = extendAtomsWithInstanceIDAttribute(dep.getBodyAtoms(), freshcounter);
		Atom[] extendedHeadAtoms =  extendAtomsWithInstanceIDAttribute(dep.getHeadAtoms(), freshcounter+extendedBodyAtoms.length+1);
		Atom[] allExtendedAtoms =  new Atom[extendedBodyAtoms.length + extendedHeadAtoms.length];
		System.arraycopy(extendedBodyAtoms, 0, allExtendedAtoms, 0, extendedBodyAtoms.length);
		System.arraycopy(extendedHeadAtoms, 0, allExtendedAtoms, extendedBodyAtoms.length, extendedHeadAtoms.length);
		
		String query = "";
		//TOCOMMENT: Rename appropriately and comment each of the following methods.
		FromCondition from = this.databaseConnection.getSQLStatementBuilder().createFromStatement(extendedBodyAtoms);
		SelectCondition projections = this.databaseConnection.getSQLStatementBuilder().createProjections(extendedBodyAtoms);
		WhereCondition where = new WhereCondition();
		WhereCondition equalities = this.databaseConnection.getSQLStatementBuilder().createAttributeEqualities(extendedBodyAtoms);
		WhereCondition constantEqualities = this.databaseConnection.getSQLStatementBuilder().createEqualitiesWithConstants(extendedBodyAtoms);

		WhereCondition factproperties = null;
		if(facts != null && !facts.isEmpty())
			factproperties = this.databaseConnection.getSQLStatementBuilder().enforceStateMembership(extendedBodyAtoms, this.databaseConnection.getRelationNamesToDatabaseTables(), ((l.equals(LimitToThisOrAllInstances.THIS))?this.facts:null));
		else
			factproperties = new WhereCondition();
			
		if(isEGD) {
			WhereCondition activenessFilter = this.databaseConnection.getSQLStatementBuilder().createEGDActivenessFilter((EGD)dep,extendedBodyAtoms);
			if (!activenessFilter.isEmpty()) where.addCondition(activenessFilter);
			if (((EGD)dep).isFromFunctionalDependency()) {
				WhereCondition egdProperties = uk.ac.ox.cs.pdq.reasoning.chase.Utility.createConditionForEGDsCreatedFromFunctionalDependencies(extendedBodyAtoms, this.databaseConnection.getRelationNamesToDatabaseTables(),this.databaseConnection.getSQLStatementBuilder());
				if (egdProperties!=null) where.addCondition(egdProperties);
			}
		}
		where.addCondition(equalities);
		where.addCondition(constantEqualities);
		where.addCondition(factproperties);

		query = this.databaseConnection.getSQLStatementBuilder().buildSQLQuery(projections,from,where);

		if(t.equals(TriggerProperty.ACTIVE)) {
			FromCondition from2 = this.databaseConnection.getSQLStatementBuilder().createFromStatement(extendedHeadAtoms);
			SelectCondition nestedProjections = this.databaseConnection.getSQLStatementBuilder().createProjections(extendedHeadAtoms);
			WhereCondition predicates2 = new WhereCondition();
			WhereCondition nestedAttributeEqualities = (!(isEGD))?this.databaseConnection.getSQLStatementBuilder().createAttributeEqualities(allExtendedAtoms):uk.ac.ox.cs.pdq.reasoning.chase.Utility.createNestedAttributeEqualitiesForActiveTriggers(extendedBodyAtoms,extendedHeadAtoms,this.databaseConnection.getSQLStatementBuilder());
			WhereCondition nestedConstantEqualities = this.databaseConnection.getSQLStatementBuilder().createEqualitiesWithConstants(allExtendedAtoms);
			predicates2.addCondition(nestedAttributeEqualities);
			predicates2.addCondition(nestedConstantEqualities);

			WhereCondition nestedFactproperties = null;
			if(facts != null && !facts.isEmpty())
				nestedFactproperties = this.databaseConnection.getSQLStatementBuilder().enforceStateMembership(extendedHeadAtoms, this.databaseConnection.getRelationNamesToDatabaseTables(), ((l.equals(LimitToThisOrAllInstances.THIS))?this.facts:null));
			else
				nestedFactproperties = new WhereCondition();
			predicates2.addCondition(nestedFactproperties);

			String nestedQuery = this.databaseConnection.getSQLStatementBuilder().buildSQLQuery(nestedProjections, from2, predicates2);

			query = this.databaseConnection.getSQLStatementBuilder().nestQueries(query,where,nestedQuery);
		}

		log.trace(dep);
		log.trace(query);
		log.trace("\n\n");
		return Pair.of(query, projections.getInternalMap());
	}

	/**
	 * Creates an SQL statement that detects homomorphisms of the input query to facts kept in a database.
	 * @param source the input query
	 * @param l determines whether the answers of the query will be limited in facts of THIS DatabaseChaseInstance or of ALL instances.
	 * @param finalProjectionMapping a mapping of the query free variables to canonical constants
	 * @return This method returns a pair containing (a) an SQL query that detects homomorphisms of the input query, 
	 * and (b) a map with the attributes to be projected. TOCOMMENT:// I'm not sure what is in this map exactly.
	 */
	public Pair<String,LinkedHashMap<String,Variable>> createSQLQuery(ConjunctiveQuery source, LimitToThisOrAllInstances l, Map<Variable, Constant> finalProjectionMapping) {
		String query = "";
		FromCondition from = this.databaseConnection.getSQLStatementBuilder().createFromStatement(source.getAtoms());
		//TOCOMMENT: Rename appropriately and comment each of the following methods.
		SelectCondition projections = this.databaseConnection.getSQLStatementBuilder().createProjections(source.getAtoms());
		WhereCondition where = new WhereCondition();
		WhereCondition equalities = this.databaseConnection.getSQLStatementBuilder().createAttributeEqualities(source.getAtoms());
		WhereCondition constantEqualities = this.databaseConnection.getSQLStatementBuilder().createEqualitiesWithConstants(source.getAtoms());
		WhereCondition equalitiesWithProjectedVars = this.databaseConnection.getSQLStatementBuilder().createEqualitiesRespectingInputMapping(source.getAtoms(), finalProjectionMapping);

		WhereCondition factproperties = null;
		if(facts != null && !facts.isEmpty())
			factproperties = this.databaseConnection.getSQLStatementBuilder().enforceStateMembership(source.getAtoms(), this.databaseConnection.getRelationNamesToDatabaseTables(), ((l.equals(LimitToThisOrAllInstances.THIS))?this.facts:null));
		else
			factproperties = new WhereCondition();
		
		where.addCondition(equalities);
		where.addCondition(constantEqualities);
		where.addCondition(equalitiesWithProjectedVars);
		where.addCondition(factproperties);

		query = this.databaseConnection.getSQLStatementBuilder().buildSQLQuery(projections, from, where);

		log.trace(source);
		log.trace(query);
		log.trace("\n\n");
		return Pair.of(query, projections.getInternalMap());
	}
	
	
	@Override
	public int hashCode() {
		if(this.hash == null) 
			this.hash = Objects.hash(this.facts, this.databaseConnection);
		return this.hash;
	}
	
	public int getInstanceId() {
		return this.hashCode();
	}
}
