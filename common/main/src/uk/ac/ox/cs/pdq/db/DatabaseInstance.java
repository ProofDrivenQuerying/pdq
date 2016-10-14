package uk.ac.ox.cs.pdq.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
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
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.Parameters.EnumParameterValue;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismUtility;
import uk.ac.ox.cs.pdq.db.sql.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.ExecuteSQLQueryThread;
import uk.ac.ox.cs.pdq.db.sql.ExecuteSynchronousSQLUpdateThread;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Variable;
/**
 * 
 * A database instance is a set of facts stored in an RDBMS. 
 * This objects provides basic functionalities, for storing, indexing querying and deleting a database instance.
 * @author George K
 *
 */
public class DatabaseInstance implements Instance {


	private static Logger log = Logger.getLogger(DatabaseInstance.class);

	/** The schema relations */
	protected List<Relation> relations = null;

	/**  Maps of the string representation of a constant to the constant. */
	protected Map<String, TypedConstant<?>> constants = null;

	/** Number of parallel threads. **/
	protected int synchronousThreadsNumber = 1;

	protected final long timeout = 3600000;
	protected final TimeUnit unit = TimeUnit.MILLISECONDS;
	protected final static int insertCacheSize = 1000; 


	/** A datqabase instance can be associated to a current query, for the join positions of which, indices are created.. */
	Evaluatable currentQuery = null;

	/** True if previous query indices were cleared. */
	private boolean clearedLastQuery = true;
	
	protected ReasoningParameters resParams;

	protected Set<String> existingIndices =  new LinkedHashSet<String>();

	/** Statemenets that drop the query indices. */
	Set<String> dropQueryIndexStatements = Sets.newLinkedHashSet();
	//------------------------------------------------------//

	public Schema schema;

	protected List<Connection> connections;

	protected SQLStatementBuilder builder;

	protected Map<String, DatabaseRelation> relationNamesToRelationObjects;

	protected DatabaseConnection databaseConnection;

	protected DatabaseConnection getDatabaseConnection() {
		return databaseConnection;
	}

	public DatabaseInstance(DatabaseConnection databaseConnection) throws SQLException
	{
		connections = databaseConnection.synchronousConnections;
		builder = databaseConnection.getSQLStatementBuilder();
		relationNamesToRelationObjects = databaseConnection.getRelationNamesToRelationObjects();
		synchronousThreadsNumber = databaseConnection.synchronousThreadsNumber;
		constants = databaseConnection.getSchema().getConstants();
		this.databaseConnection = databaseConnection;
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
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(),e);
				throw new RuntimeException(e);
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

	@Override
	public void addFacts(Collection<Atom> facts) {
		Queue<String> queries = new ConcurrentLinkedQueue<>();

		if(this.builder instanceof DerbyStatementBuilder) {
			queries.addAll(this.builder.createInsertStatements(facts, this.relationNamesToRelationObjects));
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
					queries.add(this.builder.createBulkInsertStatement(predicate, subList, this.relationNamesToRelationObjects));
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
				threads.add(new ExecuteSynchronousSQLUpdateThread(queries, this.connections.get(j)));
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
						throw new RuntimeException(e1);
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
				queries.add(this.builder.createBulkDeleteStatement(predicate, subList, this.relationNamesToRelationObjects));
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
			
			
			DatabaseConnection dbconn= new DatabaseConnection(resParams, schema);
			DatabaseInstance clone = new DatabaseInstance(dbconn);
			clone.resParams = this.resParams;
			return clone;
		} catch (SQLException e) {
			log.error(e.getMessage(),e);
			return null;
		}
	}
	public void addQuery(Query<?> query) {
		if(!this.clearedLastQuery)
			throw new RuntimeException("Method clearQuery should have been called in order to clear previous query's tables from the database.");
		this.clearedLastQuery = false;
		try {
			Statement sqlStatement = this.connections.get(0).createStatement();

			//Create statements that set up or drop the indices for the joins in the body of the input query
			Set<String> joinIndexes = Sets.newLinkedHashSet();
			Pair<Collection<String>, Collection<String>> dropAndCreateStms = 
					this.builder.setupIndices(true, this.relationNamesToRelationObjects, query, this.existingIndices);
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
			Statement sqlStatement = this.connections.get(0).createStatement();
			//Drop the join indices for input query
			for (String b: this.dropQueryIndexStatements) {
				sqlStatement.addBatch(b);
			}
			//Clear the database tables built for the query
			Collection<String> clearTablesSQLExpressions = this.builder.createTruncateTableStatements(this.currentQuery.getBody().getAtoms(), 
					this.relationNamesToRelationObjects);
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
	 * 
	 * @param queries A queue of triples, representing a query.bEach triple holds, 
	 * - the query or the constraint we want to detect homomorphisms for
	 * - the SQL query expression we will execute over the database
	 * - a map of projected variables
	 * @return matches of the queries
	 */
	protected <Q extends Evaluatable>  List<Match> answerQueries(Queue<Triple<Q, String, LinkedHashMap<String, Variable>>> queries) {

		List<Match> result = new LinkedList<>();

		//Run the SQL query statements in multiple threads
		ExecutorService executorService = null;
		try {

			//Create a pool of threads to run in parallel
			executorService = Executors.newFixedThreadPool(this.synchronousThreadsNumber);
			List<Callable<List<Match>>> threads = new ArrayList<>();
			for(int j = 0; j < this.synchronousThreadsNumber; ++j) {
				//Create the threads that will run the database queries
				threads.add(new ExecuteSQLQueryThread<Q>(queries, this.constants, this.connections.get(j)));
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
	//used for debugging purposes
	public LinkedHashSet<String> getAllFactsFromDB() throws SQLException {
		LinkedHashSet<String> results = new LinkedHashSet<String>();
		Statement sqlStatement = this.connections.get(0).createStatement();
		for(String query:this.builder.createGetAllTuplesStatement(this.relationNamesToRelationObjects))
		{
			try {
				ResultSet resultSet = sqlStatement.executeQuery(query);
				while (resultSet.next()) {
					int f = 1;
					results.add(resultSet.getString(f));
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		return results;
	}

	//@Override
	public void close() throws Exception {
		//is this the right thing to do?
		this.databaseConnection.close();
	}
}
