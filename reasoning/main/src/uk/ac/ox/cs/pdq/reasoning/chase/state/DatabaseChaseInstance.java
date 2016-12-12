package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty.ActiveTriggerProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty.EGDHomomorphismProperty;
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
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClass;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 *
 * A collection of facts produced during chasing.
 * It also keeps a graph of the rule firings that took place during chasing.
 * This implementation keeps the facts produced during chasing in a database.
 * Homomorphisms are detected using the DBMS the stores the chase facts. 
 *
 * @author Efthymia Tsamoura
 * @author George K
 *
 */
public class DatabaseChaseInstance extends DatabaseInstance implements ChaseInstance  {


	/** The _is failed. */
	private boolean _isFailed = false;

	/**  The state's facts. */
	protected Collection<Atom> facts = new LinkedHashSet<Atom>();

	/**  Keeps the classes of equal constants. */
	protected EqualConstantsClasses classes;

	/** The canonical names. */
	protected final boolean canonicalNames = true;

	/** Maps each constant to the atom and the position inside this atom where it appears. 
	 * We need this table when we are applying an EGD chase step. **/
	protected final Multimap<Constant, Atom> constantsToAtoms;

	/**
	 * Instantiates a new database chase state.
	 *
	 * @param query the query
	 * @param chaseState the chaseState
	 * @throws SQLException 
	 */
	public DatabaseChaseInstance(ConjunctiveQuery query, DatabaseConnection connection) throws SQLException {
		super(connection);
		this.addFacts(Sets.newHashSet(uk.ac.ox.cs.pdq.reasoning.chase.Utility.applySubstitution(query, Utility.generateCanonicalMapping(query)).getAtoms()));
		this.classes = new EqualConstantsClasses();
		this.constantsToAtoms = inferConstantsMap(this.facts);
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
		this.constantsToAtoms = inferConstantsMap(this.facts);
		this.indexConstraints();
		this.indexLastAttributeOfAllRelations();
	}

	/**
	 * Instantiates a new database chase instance
	 * This protected constructor does not(!) add the facts into the rdbms. 
	 * Using this constructor one would need to call addFacts explicitly. It also does not perform any contraint or attribute indexing.
	 *
	 * @param chaseState TOCOMMENT: why doesn't state contain facts?
	 * @param facts 
	 * @param constants TOCOMMENT what is this
	 * @param classes TCOMMENT: what is this
	 */
	protected DatabaseChaseInstance(
			Collection<Atom> facts,
			EqualConstantsClasses classes,
			Multimap<Constant,Atom> constants,
			DatabaseConnection connection 
			) throws SQLException {
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
	protected static Multimap<Constant,Atom> inferConstantsMap(Collection<Atom> facts) {
		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		for(Atom fact:facts) {
			Preconditions.checkArgument(!fact.isEquality());
			for(Term term:fact.getTerms()) {
				constantsToAtoms.put((Constant)term, fact);
			}
		}
		return constantsToAtoms;
	}

	/**
	 * Creates indices in the RDBMS for all join positions in the dependencies' bodies
	 * 
	 * @throws SQLException
	 */
	public void indexConstraints() throws SQLException {
		Statement sqlStatement = this.getDatabaseConnection().getSynchronousConnections().get(0).createStatement();
		Relation equalityRelation = this.createDatabaseEqualityRelation();
		this.relationNamesToRelationObjects.put(QNames.EQUALITY.toString(), equalityRelation);
		sqlStatement.addBatch(this.getDatabaseConnection().getBuilder().createTableStatement(equalityRelation));
		//sqlStatement.addBatch(this.getDatabaseConnection().getBuilder().createColumnIndexStatement(equalityRelation, equalityRelation.getAttribute(equalityRelation.getArity()-1)));
		//Create indices for the joins in the body of the dependencies
		Set<String> joinIndexes = Sets.newLinkedHashSet();
		for (Dependency constraint:schema.getDependencies()) {
			joinIndexes.addAll(this.getDatabaseConnection().getBuilder().setupIndices(false, this.relationNamesToRelationObjects, constraint, this.existingIndices).getLeft());
		}
		for (String b: joinIndexes) {
			sqlStatement.addBatch(b);
		}
		sqlStatement.executeBatch();
	}
	
	/**
	 * Creates indices in the RDBMS for the last position of every relation schema
	 * 
	 * @throws SQLException
	 */
	private void indexLastAttributeOfAllRelations() throws SQLException {
		Statement sqlStatement = this.getDatabaseConnection().getSynchronousConnections().get(0).createStatement();

		for(Relation relation:this.relationNamesToRelationObjects.values())
			sqlStatement.addBatch(this.getDatabaseConnection().getBuilder().createColumnIndexStatement(relation, relation.getAttribute(relation.getArity()-1)));
		
		sqlStatement.executeBatch();
	}
	
	/**
	 * Creates an "equality" relation for storing terms in the chase that are equated by EGDs
	 * 
	 * @return
	 */
	private Relation createDatabaseEqualityRelation() {	
		
		String attrPrefix = "x";
		
		Attribute Fact = new Attribute(Integer.class, "FactID");
		List<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute(String.class, attrPrefix + 0));
		attributes.add(new Attribute(String.class, attrPrefix + 1));
		attributes.add(Fact);
		return new Relation(QNames.EQUALITY.toString(), attributes, true){};
	}	
	
	/**
	 * Updates that state given the input match. 
	 *
	 * @param match the match
	 * @return true, if step successful
	 */
	@Override
	public boolean chaseStep(Match match) {	
		return this.chaseStep(Sets.newHashSet(match));
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
			Dependency dependency = (Dependency) match.getQuery();
			Preconditions.checkArgument(dependency instanceof TGD, "EGDs are not allowed inside TGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Implication grounded = uk.ac.ox.cs.pdq.reasoning.chase.Utility.fire(dependency, mapping, true);
			Formula left = grounded.getChildren().get(0);
			Formula right = grounded.getChildren().get(1);
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
			Dependency dependency = (Dependency) match.getQuery();
			Preconditions.checkArgument(dependency instanceof EGD, "TGDs are not allowed inside EGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Implication grounded = uk.ac.ox.cs.pdq.reasoning.chase.Utility.fire(dependency, mapping);
			Formula left = grounded.getChildren().get(0);
			Formula right = grounded.getChildren().get(1);
			for(Atom atom:right.getAtoms()) {
				//Find all the constants that each constant in the equality is representing 
				obsoleteToRepresentative.putAll(this.updateEqualConstantClasses(atom));
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
		deleteFacts(obsoleteFacts);
		this.addFacts(newFacts);
		return !this._isFailed;
	}

	/**
	 * Updates the classes of equal constants and returns the constants whose representative changed. 
	 * @param atom
	 * @return
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
			successfull = this.classes.add(atom);
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
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#isFailed()
	 */
	@Override
	public boolean isFailed() {
		return this._isFailed;
	}


	public Multimap<Constant, Atom> getConstantsToAtoms() {
		return this.constantsToAtoms;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ListState#addFacts(java.util.Collection)
	 */
	@Override
	public void addFacts(Collection<Atom> facts) {
		super.addFacts(facts);
		this.facts.addAll(facts);
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
		if(!classes.merge(((DatabaseChaseInstance)s).classes)) {
			return null;
		}

		Multimap<Constant, Atom> constantsToAtoms = HashMultimap.create();
		constantsToAtoms.putAll(this.constantsToAtoms);
		constantsToAtoms.putAll(((DatabaseChaseInstance)s).constantsToAtoms);

		return new DatabaseChaseInstance(
				facts, 
				classes,
				constantsToAtoms, this.getDatabaseConnection());
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#getMatches(uk.ac.ox.cs.pdq.fol.ConjunctiveQuery, uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitTofacts)
	 */
	public List<Match> getMatches(ConjunctiveQuery query, LimitTofacts l) {
		HomomorphismProperty[] properties = new HomomorphismProperty[2];
		if(l.equals(LimitTofacts.THIS)) {
			properties[0] = HomomorphismProperty.createMapProperty(query.getSubstitutionOfFreeVariablesToCanonicalConstants());
			properties[1] = HomomorphismProperty.createFactProperty(this.facts);
		}
		else {
			properties = new HomomorphismProperty[1];
			properties[0] = HomomorphismProperty.createMapProperty(query.getSubstitutionOfFreeVariablesToCanonicalConstants());
		}

		Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();
		//Create a new query out of each input query that references only the cleaned predicates
		ConjunctiveQuery converted = this.convert(query);
		HomomorphismProperty[] c = null;
		//Create an SQL statement for the cleaned query
		Pair<String, LinkedHashMap<String, Variable>> pair = createQuery(converted, properties);
		queries.add(Triple.of((Formula)query, pair.getLeft(), pair.getRight()));
		return this.answerQueries(queries);
	}



	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#getTriggers(java.util.Collection, uk.ac.ox.cs.pdq.db.homomorphism.TriggerProperty, uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitTofacts)
	 */
	public List<Match> getTriggers(Collection<? extends Dependency> dependencies, TriggerProperty t, LimitTofacts limitToFacts) {
		HomomorphismProperty[] properties = new HomomorphismProperty[2];
		if(t.equals(TriggerProperty.ACTIVE)) {
			properties[0] = HomomorphismProperty.createActiveTriggerProperty();
		}
		if(limitToFacts.equals(LimitTofacts.THIS)) {
			Preconditions.checkNotNull(facts);
			properties[1] = HomomorphismProperty.createFactProperty(this.facts);
		}
		Preconditions.checkNotNull(dependencies);
		Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();
		//Create a new query out of each input query that references only the cleaned predicates
		for(Dependency source:dependencies) {
			Dependency s = this.convert(source);
			HomomorphismProperty[] c = null;
			if(source instanceof EGD) {
				c = new HomomorphismProperty[properties.length+1];
				System.arraycopy(properties, 0, c, 0, properties.length);
				c[properties.length] = HomomorphismProperty.createEGDHomomorphismProperty();
			}
			else {
				c = properties;
			}
			//Create an SQL statement for the cleaned query
			Pair<String, LinkedHashMap<String, Variable>> pair = createQuery(s, c);
			queries.add(Triple.of((Formula)source, pair.getLeft(), pair.getRight()));
		}
		return this.answerQueries(queries);
	}

	/**

	/**
	 * Convert.
	 *
	 * @param <Q> the generic type
	 * @param source 		An input formula
	 * @param toDatabaseTables 		Map of schema relation names to *clean* names
	 * @param constraints 		A set of constraints that should be satisfied by the homomorphisms of the input formula to the facts of the database 
	 * @return 		a formula that uses the input *clean* names
	 */
	private Dependency convert(Dependency source) {
		int f = 0;
		List<Formula> left = Lists.newArrayList();
		for(Atom atom:((Dependency) source).getBody().getAtoms()) {
			Relation relation = this.relationNamesToRelationObjects.get(atom.getPredicate().getName());
			List<Term> terms = Lists.newArrayList(atom.getTerms());
			terms.add(new Variable("fact" + f++));
			left.add(new Atom(relation, terms));
		}
		List<Formula> right = Lists.newArrayList();
		for(Atom atom:((Dependency) source).getHead().getAtoms()) {
			Relation relation = this.relationNamesToRelationObjects.get(atom.getPredicate().getName());
			List<Term> terms = Lists.newArrayList(atom.getTerms());
			terms.add(new Variable("fact" + f++));
			right.add(new Atom(relation, terms));
		}
		if(source instanceof TGD || source instanceof EGD) {
			return new TGD(Conjunction.of(left), Conjunction.of(right));
		}
		throw new java.lang.RuntimeException("Unsupported formula type.");
	}


	private ConjunctiveQuery convert(ConjunctiveQuery source) {
		int f = 0;
		List<Formula> body = Lists.newArrayList();
		for(Atom atom:((ConjunctiveQuery) source).getAtoms()) {
			Relation relation = this.relationNamesToRelationObjects.get(atom.getPredicate().getName());
			List<Term> terms = Lists.newArrayList(atom.getTerms());
			terms.add(new Variable("fact" + f++));
			body.add(new Atom(relation, terms));
		}
		if(body.size() == 1) {
			return new ConjunctiveQuery(((ConjunctiveQuery) source).getFreeVariables(), (Atom)body.get(0));
		}
		else {
			return new ConjunctiveQuery(((ConjunctiveQuery) source).getFreeVariables(), (Conjunction) Conjunction.of(body));
		}
		
	}
	public enum LimitTofacts{
		ALL,
		THIS
	}
	public void setDatabaseConnection(DatabaseConnection connection) {
		this.databaseConnection = connection;
		this.connections = connection.getSynchronousConnections();
		this.builder = connection.getSQLStatementBuilder();
		this.relationNamesToRelationObjects = connection.getRelationNamesToRelationObjects();
		this.synchronousThreadsNumber = connection.synchronousThreadsNumber;
		this.constants = connection.getSchema().getTypedConstants();
	}

	/**
	 * Creates an SQL statement that detects homomorphisms of the input query to facts kept in a database.
	 *
	 * @param source the source
	 * @param constraints 		A set of constraints that should be satisfied by the homomorphisms of the input formula to the facts of the database 
	 * @param typedConstants the constants
	 * @param connection the connection
	 * @return homomorphisms of the input query to facts kept in a database.
	 */
	public Pair<String,LinkedHashMap<String,Variable>> createQuery(Dependency source, HomomorphismProperty[] constraints) {
		String query = "";
		List<String> from = this.builder.createFromStatement(source.getBody().getAtoms());
		LinkedHashMap<String,Variable> projections = this.builder.createProjections(source.getBody().getAtoms());
		List<String> predicates = new ArrayList<String>();
		List<String> equalities = this.builder.createAttributeEqualities(source.getBody().getAtoms());
		List<String> constantEqualities = this.builder.createEqualitiesWithConstants(source.getBody().getAtoms());
		List<String> equalitiesForHomomorphicProperties = this.builder.createEqualitiesForHomomorphicProperties(source.getBody().getAtoms(), constraints);

		/*
		 * if the target set of facts is not null, we
		 * add in the WHERE statement a predicate which limits the identifiers
		 * of the facts that satisfy any homomorphism to the identifiers of these facts
		 */
		List<String> factproperties = this.builder.translateFactProperties(source.getBody().getAtoms(),relationNamesToRelationObjects, constraints);

		String egdProperties = this.translateEGDHomomorphicProperties(source, relationNamesToRelationObjects, constraints);
		if(egdProperties!=null) {
			predicates.add(egdProperties);
		}
		predicates.addAll(equalities);
		predicates.addAll(constantEqualities);
		predicates.addAll(equalitiesForHomomorphicProperties);
		predicates.addAll(factproperties);

		//Limit the this.builder of returned homomorphisms
		String limit = this.builder.translateLimitConstraints(constraints); 

		query = "SELECT " 	+ Joiner.on(",").join(projections.keySet()) + "\n" +  
				"FROM " 	+ Joiner.on(",").join(from);
		if(!predicates.isEmpty()) {
			query += "\n" + "WHERE " + Joiner.on(" AND ").join(predicates);
		}	

		boolean activeTrigger = false;
		for(HomomorphismProperty c:constraints) {
			if(c instanceof ActiveTriggerProperty) {
				activeTrigger = true;
				break;
			}			
		}

		if(activeTrigger) {
			List<String> from2 = this.builder.createFromStatement(source.getHead().getAtoms());
			LinkedHashMap<String,Variable> nestedProjections = this.builder.createProjections(source.getHead().getAtoms());
			List<String> predicates2 = new ArrayList<String>();
			List<String> nestedAttributeEqualities = this.createNestedAttributeEqualitiesForActiveTriggers(source);
			List<String> nestedConstantEqualities = this.builder.createEqualitiesWithConstants(source.getAtoms());
			predicates2.addAll(nestedAttributeEqualities);
			predicates2.addAll(nestedConstantEqualities);
			
			/*
			 * if the target set of facts is not null, we
			 * add in the WHERE statement a predicate which limits the identifiers
			 * of the facts that satisfy any homomorphism to the identifiers of these facts
			 */
			List<String> nestedFactproperties = this.builder.translateFactProperties(source.getHead().getAtoms(), relationNamesToRelationObjects, constraints);
			predicates2.addAll(nestedFactproperties);
			
			String query2 = 
					"(SELECT " 	+ Joiner.on(",").join(nestedProjections.keySet()) + "\n" +  
							"FROM " 	+ Joiner.on(",").join(from2);
			if(!predicates2.isEmpty()) {
				query2 += "\n" + "WHERE " + Joiner.on(" AND ").join(predicates2);
			}	
			query2 += ")";
			if(predicates.isEmpty()) {
				query += "\n" + "WHERE " + " NOT EXISTS" + "\n" + query2;
			}	
			else {
				query += "\n" + "AND " + " NOT EXISTS" + "\n" + query2;
			}
		}
		if(limit != null) {
			query += "\n" + limit;
		}
		
		log.trace(source);
		log.trace(query);
		log.trace("\n\n");
		return Pair.of(query, projections);
	}
	
	
	public Pair<String,LinkedHashMap<String,Variable>> createQuery(ConjunctiveQuery source, HomomorphismProperty[] constraints) {
		String query = "";
		List<String> from = this.builder.createFromStatement(source.getAtoms());
		LinkedHashMap<String,Variable> projections = this.builder.createProjections(source.getAtoms());
		List<String> predicates = new ArrayList<String>();
		List<String> equalities = this.builder.createAttributeEqualities(source.getAtoms());
		List<String> constantEqualities = this.builder.createEqualitiesWithConstants(source.getAtoms());
		List<String> equalitiesForHomomorphicProperties = this.builder.createEqualitiesForHomomorphicProperties(source.getAtoms(), constraints);

		/*
		 * if the target set of facts is not null, we
		 * add in the WHERE statement a predicate which limits the identifiers
		 * of the facts that satisfy any homomorphism to the identifiers of these facts
		 */
		List<String> factproperties = this.builder.translateFactProperties(source.getAtoms(), relationNamesToRelationObjects, constraints);

		predicates.addAll(equalities);
		predicates.addAll(constantEqualities);
		predicates.addAll(equalitiesForHomomorphicProperties);
		predicates.addAll(factproperties);

		//Limit the number of returned homomorphisms
		String limit = this.builder.translateLimitConstraints(constraints); 

		query = "SELECT " 	+ Joiner.on(",").join(projections.keySet()) + "\n" +  
				"FROM " 	+ Joiner.on(",").join(from);
		if(!predicates.isEmpty()) {
			query += "\n" + "WHERE " + Joiner.on(" AND ").join(predicates);
		}	

		if(limit != null) {
			query += "\n" + limit;
		}
		
		log.trace(source);
		log.trace(query);
		log.trace("\n\n");
		return Pair.of(query, projections);
	}


	/**
	 * Creates the attribute equalities.
	 *
	 * @param source the source
	 * @return 		explicit equalities (String objects of the form A.x1 = B.x2) of the implicit equalities in the input conjunction (the latter is denoted by repetition of the same term)
	 */
	public List<String> createNestedAttributeEqualitiesForActiveTriggers(Dependency source) {
		if(!(source instanceof EGD)) {
			return this.builder.createAttributeEqualities(source.getAtoms());
		}
		else {
			List<String> attributePredicates = new ArrayList<String>();
			//The right atom should be an equality
			//We add additional checks to be sure that we have to do with EGDs
			for(Atom rightAtom:source.getHead().getAtoms()) {
				Relation rightRelation = (Relation) rightAtom.getPredicate();
				String rightAlias = this.builder.aliases.get(rightAtom);
				Map<Integer,Pair<String,Attribute>> rightToLeft = new HashMap<Integer,Pair<String,Attribute>>();
				for(Term term:rightAtom.getTerms()) {
					List<Integer> rightPositions = rightAtom.getTermPositions(term); //all the positions for the same term should be equated
					Preconditions.checkArgument(rightPositions.size() == 1);
					for(Atom leftAtom:source.getBody().getAtoms()) {
						Relation leftRelation = (Relation) leftAtom.getPredicate();
						String leftAlias = this.builder.aliases.get(leftAtom);
						List<Integer> leftPositions = leftAtom.getTermPositions(term); 
						Preconditions.checkArgument(leftPositions.size() <= 1);
						if(leftPositions.size() == 1) {
							rightToLeft.put(rightPositions.get(0), Pair.of(leftAlias==null ? leftRelation.getName():leftAlias, leftRelation.getAttribute(leftPositions.get(0))));
						}
					}
				}
				Preconditions.checkArgument(rightToLeft.size()==2);
				Iterator<Entry<Integer, Pair<String, Attribute>>> entries;
				Entry<Integer, Pair<String, Attribute>> entry;
				
				entries = rightToLeft.entrySet().iterator();
				entry = entries.next();
				
				StringBuilder result = new StringBuilder();
				result.append("(");
				result.append(entry.getValue().getLeft()).append(".").append(entry.getValue().getRight().getName()).append('=');
				result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(0).getName());
				
				entry = entries.next();
				
				result.append(" AND ");
				result.append(entry.getValue().getLeft()).append(".").append(entry.getValue().getRight().getName()).append('=');
				result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(1).getName());
				
				entries = rightToLeft.entrySet().iterator();
				entry = entries.next();
	
				result.append(" OR ");
				result.append(entry.getValue().getLeft()).append(".").append(entry.getValue().getRight().getName()).append('=');
				result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(1).getName());
				
				entry = entries.next();
				
				result.append(" AND ");
				result.append(entry.getValue().getLeft()).append(".").append(entry.getValue().getRight().getName()).append('=');
				result.append(rightAlias==null ? rightRelation.getName():rightAlias).append(".").append(rightRelation.getAttribute(0).getName());
				
				result.append(")");

				attributePredicates.add(result.toString());
				
			}
			return attributePredicates;
		}
	}
	
	
	/**
	 * Translate egd homomorphism constraints.
	 * TOCOMMENT: I am not sure what this is for. It seems to be returning the part of the where clause of an sql query that compares
	 * two atoms in the body of an EGD and checks they have different fact.id
	 *
	 * @param source the source
	 * @param constraints the constraints
	 * @param relationNamesToRelationObjects 
	 * @return 		predicates that correspond to fact constraints
	 */
	public String translateEGDHomomorphicProperties(Dependency source, Map<String, Relation> relationNamesToRelationObjects,HomomorphismProperty... constraints) {
		for(HomomorphismProperty c:constraints) {
			if(c instanceof EGDHomomorphismProperty) {
				List<Atom> conjuncts = source.getBody().getAtoms();
				String lalias = this.builder.aliases.get(conjuncts.get(0));
				String ralias = this.builder.aliases.get(conjuncts.get(1));
				lalias = lalias==null ? conjuncts.get(0).getPredicate().getName():lalias;
				ralias = ralias==null ? conjuncts.get(1).getPredicate().getName():ralias;
				StringBuilder eq = new StringBuilder();
				
				String leftAttributeName = relationNamesToRelationObjects.get(conjuncts.get(0).getPredicate().getName()).getAttribute(conjuncts.get(0).getPredicate().getArity()-1).getName();
				String rightAttributeName = relationNamesToRelationObjects.get(conjuncts.get(1).getPredicate().getName()).getAttribute(conjuncts.get(1).getPredicate().getArity()-1).getName();

				
				eq.append(lalias).append(".").
				append(leftAttributeName).append(">");
				eq.append(ralias).append(".").
				append(rightAttributeName);
				return eq.toString();
			}
		}
		return null;
	}
	
	
}
