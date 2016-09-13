package uk.ac.ox.cs.pdq.db;

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
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.Parameters.EnumParameterValue;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismException;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismUtility;
import uk.ac.ox.cs.pdq.db.sql.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.ExecuteSQLQueryThread;
import uk.ac.ox.cs.pdq.db.sql.ExecuteSynchronousSQLUpdateThread;
import uk.ac.ox.cs.pdq.db.sql.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;
/**
 * 
 * @author George K
 *
 */
public class DatabaseInstance implements Instance {


	/** Logger. */
	private static Logger log = Logger.getLogger(DatabaseInstance.class);

	/** The schema relations */
	protected List<Relation> relations = null;

	/** The schema constraints */
	protected Set<Evaluatable> constraints = null;

	/**  Maps of the string representation of a constant to the constant. */
	protected Map<String, TypedConstant<?>> constants = null;

	/**  Creates SQL statements to detect homomorphisms or add/delete facts in a database. */
	public SQLStatementBuilder builder = null;

	/** Map schema relation to database tables. */
	public Map<String, DatabaseRelation> RelationNamesToRelationObjects = null;

	/** The open connections. */
	protected List<Connection> openConnections = new ArrayList<>();

	/** Number of parallel threads. **/
	protected final int synchronousThreadsNumber = 1;

	protected final long timeout = 3600000;

	protected final TimeUnit unit = TimeUnit.MILLISECONDS;

	/**  Open database connections. */
	protected final List<Connection> synchronousConnections = Lists.newArrayList();

	/**  The database. */
	private String database = null;

	/**  Database driver. */
	private String driver = null;

	/** The url. */
	private String url = null;

	/** The username. */
	private String username = null;

	/** The password. */
	private String password = null;

	/** The is initialized. */
	protected boolean isInitialized = false;

	//TODO Cleanup the following variables
	//------------------------------------------------------//
	/** The current query. */
	Evaluatable currentQuery = null;

	/** True if previous query indices were cleared. */
	private boolean clearedLastQuery = true;

	/** TOCOMMENT */
	private Set<String> constraintIndices =  new LinkedHashSet<String>();

	/** Statemenets that drop the query indices. */
	Set<String> dropQueryIndexStatements = Sets.newLinkedHashSet();
	//------------------------------------------------------//

	public Schema schema;

	/** Inmemory cache size**/
	protected final static int insertCacheSize = 1000; 
	private static Integer counter = 0;
	
	public DatabaseInstance(ReasoningParameters reasoningParams, Schema schema) throws SQLException
	{
		
		HomomorphismDetectorTypes type =reasoningParams.getHomomorphismDetectorType(); 
		String driver = reasoningParams.getDatabaseDriver();
		String url = reasoningParams.getConnectionUrl();
		String database = reasoningParams.getDatabaseName(); 
		String username = reasoningParams.getDatabaseUser();
		String password = reasoningParams.getDatabasePassword();
		SQLStatementBuilder builder = null;
		if (type != null && type == HomomorphismDetectorTypes.DATABASE) {
			if (url != null && url.contains("mysql")) {
				builder = new MySQLStatementBuilder();
			} else {
				if (Strings.isNullOrEmpty(driver)) {
					driver = "org.apache.derby.jdbc.EmbeddedDriver";
				}
				if (Strings.isNullOrEmpty(url)) {
					url = "jdbc:derby:memory:{1};create=true";
				}
				if (Strings.isNullOrEmpty(database)) {
					database = "chase";
				}
				database +=  "_" + System.currentTimeMillis() + "_" + counter++;
				synchronized (counter) {
					username = "APP_" + (counter++);
				}
				password = "";
				builder = new DerbyStatementBuilder();
			}
			
			this.schema = schema;
			this.driver = driver;
			this.url = url;
			this.username = username;
			this.password = password;
			this.database = database;
			this.builder = builder;
			this.constraints = Sets.newLinkedHashSet();
			for (Dependency<?,?> dependency: schema.getDependencies()) {
				this.constraints.add(dependency);
			}
			this.constants = schema.getConstants();
			this.relations = Lists.newArrayList(schema.getRelations());
			this.RelationNamesToRelationObjects = new LinkedHashMap<>();

			for(int i = 0; i < this.synchronousThreadsNumber; ++i) {
				this.synchronousConnections.add(getConnection(this.getDriver(), this.getUrl(), this.getDatabase(), this.getUsername(), this.getPassword()));			
			}
			this.initialize();
		}
			
	}

	public DatabaseInstance(
			String driver, 
			String url, 
			String database,
			String username, 
			String password,
			SQLStatementBuilder builder,
			Schema schema
			) throws SQLException {
		this.schema = schema;
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.database = database;
		this.builder = builder;
		this.constraints = Sets.newLinkedHashSet();
		for (Dependency<?,?> dependency: schema.getDependencies()) {
			this.constraints.add(dependency);
		}
		this.constants = schema.getConstants();
		this.relations = Lists.newArrayList(schema.getRelations());
		this.RelationNamesToRelationObjects = new LinkedHashMap<>();

		for(int i = 0; i < this.synchronousThreadsNumber; ++i) {
			this.synchronousConnections.add(getConnection(this.getDriver(), this.getUrl(), this.getDatabase(), this.getUsername(), this.getPassword()));			

		}
	}
	protected DatabaseInstance(
			String driver, 
			String url, 
			String database,
			String username, 
			String password,
			SQLStatementBuilder builder,
			List<Relation> relations,
			Map<String, TypedConstant<?>> constants,
			Map<String, DatabaseRelation> toDatabaseRelations,
			Set<Evaluatable> constraints, Schema schema) throws SQLException {
		this.schema = schema;
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.database = database;
		this.builder = builder;
		this.RelationNamesToRelationObjects = toDatabaseRelations;
		this.constraints = constraints;
		this.constants = constants;
		this.relations = relations;

		for(int i = 0; i < this.synchronousThreadsNumber; ++i) {
			this.synchronousConnections.add(getConnection(this.getDriver(), this.getUrl(), this.getDatabase(), this.getUsername(), this.getPassword()));
		}
	}

	/**
	 * Initialize.
	 *
	 * @see uk.ac.ox.cs.pdq.db.homomorphism.homomorphism.HomomorphismManager#initialize()
	 */
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

			for (String sql: this.builder.createDatabaseStatements(this.getDatabase())) {
				sqlStatement.addBatch(sql);
			}

			this.RelationNamesToRelationObjects.put(QNames.EQUALITY.toString(), DatabaseRelation.DatabaseEqualityRelation);
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
				this.RelationNamesToRelationObjects.put(relation.getName(), dbRelation);
				sqlStatement.addBatch(this.builder.createTableStatement(dbRelation));
				sqlStatement.addBatch(this.builder.createColumnIndexStatement(dbRelation, DatabaseRelation.Fact));
			}

			//Create indices for the joins in the body of the dependencies
			Set<String> joinIndexes = Sets.newLinkedHashSet();
			for (Evaluatable constraint:this.constraints) {
				joinIndexes.addAll(this.builder.setupIndices(false, this.RelationNamesToRelationObjects, constraint, this.constraintIndices).getLeft());
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
			//Statement sqlStatement = this.synchronousConnections.createStatement();

			for (String sql: this.builder.createDropStatements(this.getDatabase())) {
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
		for(Connection con:this.synchronousConnections) {
			con.close();
		}
		for(Connection con:this.openConnections) {
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
	 * The Enum HomomorphismDetectorTypes.
	 */
	public static enum HomomorphismDetectorTypes {

		/** The database. */
		@EnumParameterValue(description = "Homomorphism detection relying on an internal relational database")
		DATABASE;
	}

	@Override
	public void addFacts(Collection<Atom> facts) {
		Queue<String> queries = new ConcurrentLinkedQueue<>();

		if(this.builder instanceof DerbyStatementBuilder) {
			queries.addAll(this.builder.createInsertStatements(facts, this.RelationNamesToRelationObjects));
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
					queries.add(this.builder.createBulkInsertStatement(predicate, subList, this.RelationNamesToRelationObjects));
					subList.clear();
				}
			}
			clusters.clear();
		}

		executeQueries(queries);		
	}


	public void executeQueries(Queue<String> queries)
	{		
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

	@Override
	public void deleteFacts(Collection<Atom> facts) {
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
				queries.add(this.builder.createBulkDeleteStatement(predicate, subList, this.RelationNamesToRelationObjects));
				subList.clear();
			}
		}

		executeQueries(queries);
	}


	@Override
	public List<Match> answerQuery(Evaluatable q) {
		throw new UnsupportedOperationException("Method not implemented yet - use answerQueries()");
	}

	public DatabaseInstance clone() {
		try {
			DatabaseInstance clone = new DatabaseInstance(
					this.getDriver(), this.getUrl(), this.getDatabase(), this.getUsername(), this.getPassword(),
					this.builder.clone(), 
					this.relations, 
					this.constants,
					this.RelationNamesToRelationObjects, 
					this.constraints, this.schema);
			clone.isInitialized = this.isInitialized;
			this.openConnections.addAll(clone.synchronousConnections);
			return clone;
		} catch (SQLException e) {
			log.error(e.getMessage(),e);
			return null;
		}
	}

	public void addQuery(Query<?> query) {
		if(!this.clearedLastQuery)
			throw new RuntimeException("Method clearQuery should be called in order to clear previous query's tables from the database.");
		this.clearedLastQuery = false;
		try {
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();

			//Create statements that set up or drop the indices for the joins in the body of the input query
			Set<String> joinIndexes = Sets.newLinkedHashSet();
			Pair<Collection<String>, Collection<String>> dropAndCreateStms = 
					this.builder.setupIndices(true, this.RelationNamesToRelationObjects, query, this.constraintIndices);
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

	public void clearQuery() {
		try {
			Statement sqlStatement = this.synchronousConnections.get(0).createStatement();
			//Drop the join indices for input query
			for (String b: this.dropQueryIndexStatements) {
				sqlStatement.addBatch(b);
			}
			//Clear the database tables built for the query
			Collection<String> clearTablesSQLExpressions = this.builder.createTruncateTableStatements(this.currentQuery.getBody().getAtoms(), 
					this.RelationNamesToRelationObjects);
			for (String b: clearTablesSQLExpressions) {
				sqlStatement.addBatch(b);
			}
			sqlStatement.executeBatch();
		} catch (SQLException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
		this.clearedLastQuery = true;
	}

	/**
	 * TOCOMMENT: 
	 * @param queries
	 * @return
	 */
	public <Q extends Evaluatable>  List<Match> answerQueries(Queue<Triple<Q, String, LinkedHashMap<String, Variable>>> queries) {

		List<Match> result = new LinkedList<>();

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

	@Override
	public Collection<Atom> getFacts() {
		// TODO How to return the facts? Query The database?
		throw new RuntimeException("getFacts() is unimplemented in DatabaseInstance.java");
	}
	/**
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}
