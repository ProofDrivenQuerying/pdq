package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseEGD;
import uk.ac.ox.cs.pdq.db.DatabaseEquality;
import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.DatabaseRelation;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClass;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;

/**
 *
 * A collection of facts produced during chasing.
 * It also keeps a graph of the rule firings that took place during chasing.
 * This implementation keeps the facts produced during chasing in a database.
 * Homomorphisms are detected using the DBMS the stores the chase facts. 
 *
 * @author George K
 * @author Efthymia Tsamoura
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
		this.addFacts(Sets.newHashSet(query.ground(ConjunctiveQuery.generateCanonicalMapping(query.getBody())).getAtoms()));
		this.classes = new EqualConstantsClasses();
		this.constantsToAtoms = inferConstantsMap(this.facts);
		this.indexConstraints();
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
	}
	
	/**
	 * Instantiates a new database list state. 
	 * This protected constructor does not(!) add the facts into the rdbms. 
	 * Using this constructor on would need to call addFacts explicilty.
	 *
	 * @param chaseState the chaseState
	 * @param facts the facts
	 * @param graph the graph
	 * @param classes the constant classes
	 * @param relationNamesToRelationObjects 
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
	//TOCOMMENT: What does this do exaclty? where is it used?
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
	
	
	public void indexConstraints() throws SQLException
	{
	
		Statement sqlStatement = this.getDatabaseConnection().getSynchronousConnections().get(0).createStatement();

		this.relationNamesToRelationObjects.put(QNames.EQUALITY.toString(), DatabaseRelation.DatabaseEqualityRelation);
		sqlStatement.addBatch(this.getDatabaseConnection().getBuilder().createTableStatement(DatabaseRelation.DatabaseEqualityRelation));
		sqlStatement.addBatch(this.getDatabaseConnection().getBuilder().createColumnIndexStatement(DatabaseRelation.DatabaseEqualityRelation, DatabaseRelation.Fact));

		
		//Create indices for the joins in the body of the dependencies
		Set<String> joinIndexes = Sets.newLinkedHashSet();
		for (Evaluatable constraint:schema.getDependencies()) {
			joinIndexes.addAll(this.getDatabaseConnection().getBuilder().setupIndices(false, this.relationNamesToRelationObjects, constraint, this.existingIndices).getLeft());
		}
		for (String b: joinIndexes) {
			sqlStatement.addBatch(b);
		}
		
		sqlStatement.executeBatch();
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
			Dependency grounded = dependency.fire(mapping, true);
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
			Dependency dependency = (Dependency) match.getQuery();
			Preconditions.checkArgument(dependency instanceof EGD, "TGDs are not allowed inside EGDchaseStep");
			Map<Variable, Constant> mapping = match.getMapping();
			Dependency grounded = dependency.fire(mapping, true);
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
		deleteFacts(obsoleteFacts);
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
			if(l.equals(LimitTofacts.THIS))
			{
					properties[0] = HomomorphismProperty.createMapProperty(query.getGroundingsProjectionOnFreeVars());
					properties[1] = HomomorphismProperty.createFactProperty(Conjunction.of(facts));
			}
			else
			{
				properties = new HomomorphismProperty[1];
				properties[0] = HomomorphismProperty.createMapProperty(query.getGroundingsProjectionOnFreeVars());
			}
			
			Queue<Triple<ConjunctiveQuery, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();;
			//Create a new query out of each input query that references only the cleaned predicates
			ConjunctiveQuery converted = this.convert(query);
			HomomorphismProperty[] c = null;
			//Create an SQL statement for the cleaned query
			Pair<String, LinkedHashMap<String, Variable>> pair = this.builder.createQuery(converted, properties);
			queries.add(Triple.of(query, pair.getLeft(), pair.getRight()));

			return this.answerQueries(queries);
		
	}



	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance#getTriggers(java.util.Collection, uk.ac.ox.cs.pdq.db.homomorphism.TriggerProperty, uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitTofacts)
	 */
	public List<Match>getTriggers(Collection<? extends Dependency> dependencies,TriggerProperty t, LimitTofacts limitToFacts) {
		
		
		HomomorphismProperty[] properties = new HomomorphismProperty[2];
		if(t.equals(TriggerProperty.ACTIVE))
		{
			properties[0] = HomomorphismProperty.createActiveTriggerProperty();
		}
		if(limitToFacts.equals(LimitTofacts.THIS))
		{
			Preconditions.checkNotNull(facts);
			properties[1] = HomomorphismProperty.createFactProperty(Conjunction.of(this.facts));
		}
		Preconditions.checkNotNull(dependencies);
		Queue<Triple<Dependency, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();;
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
			Pair<String, LinkedHashMap<String, Variable>> pair = this.builder.createQuery(s, c);
			queries.add(Triple.of(source, pair.getLeft(), pair.getRight()));
		}

		return this.answerQueries(queries);
	}
	
	/**
	 * Adds fact id column to all relations, extending their arity.
	 * @param source
	 * @return
	 */
	private <Q extends Evaluatable> Q convert(Q source) {
		if(source instanceof TGD) {
			int f = 0;
			List<Atom> left = Lists.newArrayList();
			for(Atom atom:((TGD) source).getLeft()) {
				Relation relation = this.relationNamesToRelationObjects.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				left.add(new Atom(relation, terms));
			}
			List<Atom> right = Lists.newArrayList();
			for(Atom atom:((TGD) source).getRight()) {
				Relation relation = this.relationNamesToRelationObjects.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				right.add(new Atom(relation, terms));
			}
			return (Q) new TGD(Conjunction.of(left), Conjunction.of(right));
		}
		else if (source instanceof EGD) {
			int f = 0;
			List<Atom> left = Lists.newArrayList();
			for(Atom atom:((EGD) source).getLeft()) {
				Relation relation = this.relationNamesToRelationObjects.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				left.add(new Atom(relation, terms));
			}
			List<DatabaseEquality> right = new ArrayList<DatabaseEquality>();
			for(Equality atom:((EGD) source).getRight()) {
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				right.add(new DatabaseEquality(terms));
			}
			return (Q) new DatabaseEGD(Conjunction.of(left), Conjunction.of(right));
		}
		else if(source instanceof Query) {
			int f = 0;
			List<Atom> body = Lists.newArrayList();
			for(Atom atom:((Query<?>) source).getBody().getAtoms()) {
				Relation relation = this.relationNamesToRelationObjects.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				body.add(new Atom(relation, terms));
			}
			return (Q) new ConjunctiveQuery(((Query) source).getHead(), Conjunction.of(body));
		}
		else {
			throw new java.lang.UnsupportedOperationException();
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
		this.constants = connection.getSchema().getConstants();
	}

}
