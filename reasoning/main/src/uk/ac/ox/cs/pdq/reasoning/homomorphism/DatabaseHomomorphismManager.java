package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.reasoning.sqlstatement.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.reasoning.sqlstatement.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Detects homomorphisms from a conjunction to a set of facts.
 * For each schema relation of N attributes the object creates a new table with "clean name"
 * and attributes x0, x1, ..., x_{N-1}, FACT where x_i corresponds to the
 * i-th relation attribute and FACT is a unique fact identifier. 
 *
 * @author Efthymia Tsamoura
 *
 */
public class DatabaseHomomorphismManager implements HomomorphismManager {

	/** Logger. */
	private static Logger log = Logger.getLogger(DatabaseHomomorphismManager.class);

	/** The schema relations */
	protected final List<Relation> relations;

	/** The schema constraints */
	protected Set<Evaluatable> constraints;

	/**  Maps of the string representation of a constant to the constant. */
	protected final Map<String, TypedConstant<?>> constants;

	/**  Creates SQL statements to detect homomorphisms or add/delete facts in a database. */
	protected final SQLStatementBuilder builder;

	/** Map schema relation to database tables. */
	protected final Map<String, DatabaseRelation> toDatabaseTables;

	/** The open connections. */
	protected static List<Connection> openConnections = new ArrayList<>();

	/** Number of parallel threads. **/
	protected final int synchronousThreadsNumber = 1;

	protected final long timeout = 3600000;

	protected final TimeUnit unit = TimeUnit.MILLISECONDS;

	/**  Open database connections. */
	protected final List<Connection> synchronousConnections = Lists.newArrayList();

	/**  The database. */
	protected final String database;

	/**  Database driver. */
	protected final String driver;

	/** The url. */
	protected final String url;

	/** The username. */
	protected final String username;

	/** The password. */
	protected final String password;

	/** The is initialized. */
	protected boolean isInitialized = false;

	//TODO Cleanup the following variables
	//------------------------------------------------------//
	/** The current query. */
	Evaluatable currentQuery = null;

	/** True if previous query indices were cleared. */
	private boolean clearedLastQuery = true;

	/** ??? */
	private Set<String> constraintIndices =  new LinkedHashSet<String>();

	/** Statemenets that drop the query indices. */
	Set<String> dropQueryIndexStatements = Sets.newLinkedHashSet();
	//------------------------------------------------------//

	/** Inmemory cache size**/
	protected final static int insertCacheSize = 1000; 


	/**
	 * Instantiates a new DB homomorphism manager.
	 *
	 * @param driver 		Database driver
	 * @param url 		Database url
	 * @param database 		Database name
	 * @param username 		Database user
	 * @param password 		Database pass
	 * @param builder 		Builds SQL queries that detect homomorphisms
	 * @param schema 		Input schema
	 * @throws SQLException the SQL exception
	 */
	public DatabaseHomomorphismManager(
			String driver, 
			String url, 
			String database,
			String username, 
			String password,
			SQLStatementBuilder builder,
			Schema schema
			) throws SQLException {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.database = database;
		this.builder = builder;
		this.constraints = Sets.newLinkedHashSet();
		for (Constraint<?,?> dependency: schema.getDependencies()) {
			this.constraints.add(dependency);
		}
		this.constants = schema.getConstants();
		this.relations = Lists.newArrayList(schema.getRelations());
		this.toDatabaseTables = new LinkedHashMap<>();
		
		for(int i = 0; i < this.synchronousThreadsNumber; ++i) {
			this.synchronousConnections.add(getConnection(this.driver, this.url, this.database, this.username, this.password));
		}
		DatabaseHomomorphismManager.openConnections.addAll(this.synchronousConnections);
	}

	/**
	 * Instantiates a new DB homomorphism manager.
	 *
	 * @param driver 		Database driver
	 * @param url 		Database url
	 * @param database 		Database name
	 * @param username 		Database user
	 * @param password 		Database pass
	 * @param builder 		Builds SQL queries that detect homomorphisms
	 * @param relations 		Database relations
	 * @param constants 		Schema constants
	 * @param toDatabaseRelations 		A map from the schema relation names to the created relations (the ones that
	 * 		correspond to the created database tables)
	 * @param constraints the constraints
	 * @throws SQLException the SQL exception
	 */
	protected DatabaseHomomorphismManager(
			String driver, 
			String url, 
			String database,
			String username, 
			String password,
			SQLStatementBuilder builder,
			List<Relation> relations,
			Map<String, TypedConstant<?>> constants,
			Map<String, DatabaseRelation> toDatabaseRelations,
			Set<Evaluatable> constraints) throws SQLException {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.database = database;
		this.builder = builder;
		this.toDatabaseTables = toDatabaseRelations;
		this.constraints = constraints;
		this.constants = constants;
		this.relations = relations;

		for(int i = 0; i < this.synchronousThreadsNumber; ++i) {
			this.synchronousConnections.add(getConnection(this.driver, this.url, this.database, this.username, this.password));
		}
		DatabaseHomomorphismManager.openConnections.addAll(this.synchronousConnections);
	}

	/**
	 * Initialize.
	 *
	 * @see uk.ac.ox.cs.pdq.homomorphism.HomomorphismManager#initialize()
	 */
	@Override
	public void initialize() {
		if (!this.isInitialized) {
			this.setup();
			this.isInitialized = true;
		}
	}

	/**
	 * Sets up the database that will store the facts.
	 */
	protected void setup() {
		try {
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();

			for (String sql: this.builder.createDatabaseStatements(this.database)) {
				sqlStatement.addBatch(sql);
			}
//			DatabaseRelation equality = DatabaseRelation.createEqualityTable();

			this.toDatabaseTables.put(QNames.EQUALITY.toString(), DatabaseRelation.DatabaseEqualityRelation);
			sqlStatement.addBatch(this.builder.createTableStatement(DatabaseRelation.DatabaseEqualityRelation));
			sqlStatement.addBatch(this.builder.createColumnIndexStatement(DatabaseRelation.DatabaseEqualityRelation, DatabaseRelation.Fact));

			//Put relations into a set so as to make them unique
			Set<Relation> relationset = new HashSet<Relation>();
			relationset.addAll(this.relations);
			this.relations.clear();
			this.relations.addAll(relationset);

			//Create the database tables and create column indices
			for (Relation relation:this.relations) {
				DatabaseRelation dbRelation = DatabaseRelation.createDatabaseRelation(relation);
				this.toDatabaseTables.put(relation.getName(), dbRelation);
				sqlStatement.addBatch(this.builder.createTableStatement(dbRelation));
				sqlStatement.addBatch(this.builder.createColumnIndexStatement(dbRelation, DatabaseRelation.Fact));
			}

			//Create indices for the joins in the body of the dependencies
			Set<String> joinIndexes = Sets.newLinkedHashSet();
			for (Evaluatable constraint:this.constraints) {
				joinIndexes.addAll(this.builder.setupIndices(false, this.toDatabaseTables, constraint, this.constraintIndices).getLeft());
			}
			for (String b: joinIndexes) {
				sqlStatement.addBatch(b);
			}

			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}


	/**
	 * Cleans up the database.
	 *
	 * @throws HomomorphismException the homomorphism exception
	 */
	protected void dropDatabase() throws HomomorphismException {
		try {
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();

			for (String sql: this.builder.createDropStatements(this.database)) {
				sqlStatement.addBatch(sql);
			}
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new HomomorphismException(ex.getMessage(), ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		this.dropDatabase();
		for(Connection con:DatabaseHomomorphismManager.openConnections) {
			con.close();
		}
	}

	/**
	 * Gets the connection.
	 *
	 * @param driver String
	 * @param url the url
	 * @param database the database
	 * @param username the username
	 * @param password the password
	 * @return a connection database connection for the given properties.
	 * @throws SQLException the SQL exception
	 */
	public static Connection getConnection(String driver, String url, String database, String username, String password) throws SQLException {
		if (!Strings.isNullOrEmpty(driver)) {
			try {
				Class.forName(driver).newInstance();
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Could not load chase database driver '" + driver + "'");
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(),e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(),e);
			}
		}
		String u = null;
		if (url.contains("{1}")) {
			u = url.replace("{1}", database);
		} else {
			u = url + database;
		}
		try {
			Connection result = DriverManager.getConnection(u, username, password);
			result.setAutoCommit(true);
			return result;
		} catch (SQLException e) {
			log.debug(e.getMessage());
		}
		Connection result = DriverManager.getConnection(url, username, password);
		result.setAutoCommit(true);
		return result;
	}


	/**
	 * Clone.
	 *
	 * @return DBHomomorphismManager
	 * @see uk.ac.ox.cs.pdq.homomorphism.HomomorphismDetector#clone()
	 */
	@Override
	public DatabaseHomomorphismManager clone() {
		try {
			DatabaseHomomorphismManager clone = new DatabaseHomomorphismManager(
					this.driver, this.url, this.database, this.username, this.password,
					this.builder.clone(), 
					this.relations, 
					this.constants,
					this.toDatabaseTables, 
					this.constraints);
			clone.isInitialized = this.isInitialized;
			return clone;
		} catch (SQLException e) {
			log.error(e.getMessage(),e);
			return null;
		}
	}

	/**
	 * Initializes the database tables needed to detect homomorphisms for a specific query.
	 * In this implementation after you detect the homomorphisms from a query you have "consumed" any related machinery and have to add the query again.
	 *
	 * @param query the query
	 */
	@Override
	public void addQuery(Query<?> query) {
		if(!this.clearedLastQuery)
			throw new RuntimeException("Method clearQuery should be called in order to clear previous query's tables from the database.");
		this.clearedLastQuery = false;
		try {
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();

			//Create statements that set up or drop the indices for the joins in the body of the input query
			Set<String> joinIndexes = Sets.newLinkedHashSet();
			Pair<Collection<String>, Collection<String>> dropAndCreateStms = 
					this.builder.setupIndices(true, this.toDatabaseTables, query, this.constraintIndices);
			this.dropQueryIndexStatements.addAll(dropAndCreateStms.getRight());
			joinIndexes.addAll(dropAndCreateStms.getLeft());
			for (String b: joinIndexes) {
				sqlStatement.addBatch(b);
			}
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
		this.currentQuery = query;
	}

	/**
	 * This method clears the database tables constructed for an earlier query. In certain implementation one needs to call this before adding a new Query.
	 */
	@Override
	public void clearQuery() {
		try {
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();
			//Drop the join indices for input query
			for (String b: this.dropQueryIndexStatements) {
				sqlStatement.addBatch(b);
			}
			//Clear the database tables built for the query
			Collection<String> clearTablesSQLExpressions = this.builder.createTruncateTableStatements(this.currentQuery.getBody().getAtoms(), 
					this.toDatabaseTables);
			for (String b: clearTablesSQLExpressions) {
				sqlStatement.addBatch(b);
			}
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
		this.clearedLastQuery = true;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector#getMatches(uk.ac.ox.cs.pdq.fol.Evaluatable, uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint[])
	 */
	@Override
	public <Q extends Evaluatable> List<Match> getMatches(Collection<Q> sources, HomomorphismProperty... constraints) {
		Preconditions.checkNotNull(sources);
		List<Match> result = new LinkedList<>();
		Queue<Triple<Q, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();;
		//Create a new query out of each input query that references only the cleaned predicates
		for(Q source:sources) {
			Q s = this.convert(source, constraints);
			HomomorphismProperty[] c = null;
			if(source instanceof EGD) {
				c = new HomomorphismProperty[constraints.length+1];
				System.arraycopy(constraints, 0, c, 0, constraints.length);
				c[constraints.length] = HomomorphismProperty.createEGDHomomorphismProperty();
			}
			else {
				c = constraints;
			}
			//Create an SQL statement for the cleaned query
			Pair<String, LinkedHashMap<String, Variable>> pair = this.builder.createQuery(s, c);
			queries.add(Triple.of(source, pair.getLeft(), pair.getRight()));
		}

		//Run the SQL query statements in multiple threads
		ExecutorService executorService = null;
		try {
			
			//Create a pool of threads to run in parallel
			executorService = Executors.newFixedThreadPool(this.synchronousThreadsNumber);
			List<Callable<List<Match>>> threads = new ArrayList<>();
			for(int j = 0; j < this.synchronousThreadsNumber; ++j) {
				//Create the threads that will run the database queries
				threads.add(new ExecuteSQLQueryThread<Q>(queries, this.constants, this.synchronousConnections.get(j)));
			}
			long start = System.currentTimeMillis();
			try {
				for(Future<List<Match>> output:executorService.invokeAll(threads, this.timeout, this.unit)){
					result.addAll(output.get());
				}
			} catch(java.util.concurrent.CancellationException e) {
				executorService.shutdownNow();
				if (this.timeout <= (System.currentTimeMillis() - start)) {
					try {
						throw new LimitReachedException(Reasons.TIMEOUT);
					} catch (LimitReachedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				return null;
			}
			executorService.shutdown();
		} catch (InterruptedException | ExecutionException e) {
			executorService.shutdownNow();
			e.printStackTrace();
			return null;
		} 
		return result;
	}

	/**
	 * Adds the facts.
	 *
	 * @param facts Collection<? extends PredicateFormula>
	 * @see uk.ac.ox.cs.pdq.homomorphism.HomomorphismManager#addFacts(Collection<? extends PredicateFormula>)
	 */
	@Override
	public void addFacts(Collection<? extends Atom> facts) {
		Queue<String> queries = new ConcurrentLinkedQueue<>();

		if(this.builder instanceof DerbyStatementBuilder) {
			queries.addAll(this.builder.createInsertStatements(facts, this.toDatabaseTables));
		}
		else {
			Map<Predicate, List<Atom>> clusters = HomomorphismUtility.clusterAtoms(facts);
			//Find the total number of tuples that will be inserted in the database
			int totalTuples = facts.size();
			int tuplesPerThread;
			if(totalTuples < this.synchronousThreadsNumber) {
				tuplesPerThread = totalTuples;
			}
			else {
				tuplesPerThread = (int) Math.ceil(totalTuples / this.synchronousThreadsNumber);
			}
			if(tuplesPerThread > insertCacheSize) {
				tuplesPerThread = insertCacheSize;
			}
			for(Entry<Predicate, List<Atom>> entry:clusters.entrySet()) {
				Predicate predicate = entry.getKey();
				List<Atom> clusterFacts = entry.getValue();
				while(!clusterFacts.isEmpty()) {
					int position = tuplesPerThread < clusterFacts.size() ? tuplesPerThread:clusterFacts.size();
					List<Atom> subList = clusterFacts.subList(0, position);
					queries.add(this.builder.createBulkInsertStatement(predicate, subList, this.toDatabaseTables));
					subList.clear();
				}
			}
			clusters.clear();
		}

		ExecutorService executorService = null;
		try {
			//Create a pool of threads to run in parallel
			executorService = Executors.newFixedThreadPool(this.synchronousThreadsNumber);
			List<Callable<Boolean>> threads = new ArrayList<>();
			for(int j = 0; j < this.synchronousThreadsNumber; ++j) {
				//Create the threads that will run the database update statements
				threads.add(new ExecuteSynchronousSQLUpdateThread(queries, this.synchronousConnections.get(j)));
			}
			long start = System.currentTimeMillis();
			try {
				for(Future<Boolean> output:executorService.invokeAll(threads, this.timeout, this.unit)){
					output.get();
				}
			} catch(java.util.concurrent.CancellationException e) {
				executorService.shutdownNow();
				if (this.timeout <= (System.currentTimeMillis() - start)) {
					try {
						throw new LimitReachedException(Reasons.TIMEOUT);
					} catch (LimitReachedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			executorService.shutdown();
		} catch (InterruptedException | ExecutionException e) {
			executorService.shutdownNow();
			e.printStackTrace();
		} 
	}

	/**
	 * Deletes the facts of the list in the database.
	 *
	 * @param facts Input list of facts
	 */
	@Override
	public void deleteFacts(Collection<? extends Atom> facts) {
		Queue<String> queries = new ConcurrentLinkedQueue<>();
		Map<Predicate, List<Atom>> clusters = HomomorphismUtility.clusterAtoms(facts);

		//Find the total number of tuples that will be inserted in the database
		int totalTuples = facts.size();
		int tuplesPerThread;
		if(totalTuples < this.synchronousThreadsNumber) {
			tuplesPerThread = totalTuples;
		}
		else {
			tuplesPerThread = (int) Math.ceil(totalTuples / this.synchronousThreadsNumber);
		}
		if(tuplesPerThread > insertCacheSize) {
			tuplesPerThread = insertCacheSize;
		}
		for(Entry<Predicate, List<Atom>> entry:clusters.entrySet()) {
			Predicate predicate = entry.getKey();
			List<Atom> clusterFacts = entry.getValue();
			while(!clusterFacts.isEmpty()) {
				int position = tuplesPerThread > clusterFacts.size() ? clusterFacts.size():tuplesPerThread;
				List<Atom> subList = clusterFacts.subList(0, position);
				queries.add(this.builder.createBulkDeleteStatement(predicate, subList, this.toDatabaseTables));
				subList.clear();
			}
		}

		ExecutorService executorService = null;
		try {

			//Create a pool of threads to run in parallel
			executorService = Executors.newFixedThreadPool(this.synchronousThreadsNumber);
			List<Callable<Boolean>> threads = new ArrayList<>();
			for(int j = 0; j < this.synchronousThreadsNumber; ++j) {
				//Create the threads that will create new binary configurations using the input left, right collections
				threads.add(new ExecuteSynchronousSQLUpdateThread(queries, this.synchronousConnections.get(j)));
			}
			long start = System.currentTimeMillis();
			try {
				for(Future<Boolean> output:executorService.invokeAll(threads, this.timeout, this.unit)){
					output.get();
				}
			} catch(java.util.concurrent.CancellationException e) {
				executorService.shutdownNow();
				if (this.timeout <= (System.currentTimeMillis() - start)) {
					try {
						throw new LimitReachedException(Reasons.TIMEOUT);
					} catch (LimitReachedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			executorService.shutdown();
		} catch (InterruptedException | ExecutionException e) {
			executorService.shutdownNow();
			e.printStackTrace();
		} 
	}

	/**
	 * Convert.
	 *
	 * @param <Q> the generic type
	 * @param source 		An input formula
	 * @param toDatabaseTables 		Map of schema relation names to *clean* names
	 * @param constraints 		A set of constraints that should be satisfied by the homomorphisms of the input formula to the facts of the database 
	 * @return 		a formula that uses the input *clean* names
	 */
	private <Q extends Evaluatable> Q convert(Q source, HomomorphismProperty... constraints) {
		if(source instanceof TGD) {
			int f = 0;
			List<Atom> left = Lists.newArrayList();
			for(Atom atom:((TGD) source).getLeft()) {
				Relation relation = this.toDatabaseTables.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				left.add(new Atom(relation, terms));
			}
			List<Atom> right = Lists.newArrayList();
			for(Atom atom:((TGD) source).getRight()) {
				Relation relation = this.toDatabaseTables.get(atom.getName());
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
				Relation relation = this.toDatabaseTables.get(atom.getName());
				List<Term> terms = Lists.newArrayList(atom.getTerms());
				terms.add(new Variable(DatabaseRelation.Fact.getName() + f++));
				left.add(new Atom(relation, terms));
			}
			List<DatabaseEquality> right = Lists.newArrayList();
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
				Relation relation = this.toDatabaseTables.get(atom.getName());
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

}
