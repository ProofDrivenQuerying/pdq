package uk.ac.ox.cs.pdq.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
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

import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.sql.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.ExecuteSQLQueryThread;
import uk.ac.ox.cs.pdq.db.sql.ExecuteSynchronousSQLUpdateThread;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.LimitReachedException;
import uk.ac.ox.cs.pdq.util.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.util.Utility;
/**
 * 
 * A database instance is a set of facts stored in an RDBMS. 
 * This object provides basic functionalities, for storing, indexing querying and deleting a database instance.
 *
 */
public abstract class DatabaseInstance implements Instance {
	protected static Logger log = Logger.getLogger(DatabaseInstance.class);
	
	/** Number of parallel threads. **/
	protected final long timeout = 3600000;
	protected final TimeUnit unit = TimeUnit.MILLISECONDS;
	protected final static int insertCacheSize = 1000; 

	/** A datqabase instance can be associated to a current query, for the join positions of which, indices are created.. */
	ConjunctiveQuery currentQuery = null;

	/** True if previous query indices were cleared. */
	private boolean clearedLastQuery = true;

	protected Set<String> existingIndices =  new LinkedHashSet<String>();

	/** Statements that drop the query indices. */
	Set<String> dropQueryIndexStatements = Sets.newLinkedHashSet();
	//------------------------------------------------------//

	protected DatabaseConnection databaseConnection;

	public DatabaseInstance(DatabaseConnection databaseConnection) {
		this.databaseConnection = databaseConnection;
	}

	public void addFacts(Collection<Atom> facts) {
		Queue<String> queries = new ConcurrentLinkedQueue<>();
		if(this.databaseConnection.getSQLStatementBuilder() instanceof DerbyStatementBuilder) {
			queries.addAll(this.databaseConnection.getSQLStatementBuilder().createInsertStatements(facts, this.databaseConnection.getRelationNamesToRelationObjects()));
		}
		else {
			Map<Predicate, List<Atom>> clusters = Utility.clusterAtomsWithSamePredicateName(facts);
			//Find the total number of tuples that will be inserted in the database
			int totalTuples = facts.size();
			int tuplesPerThread;
			if(totalTuples < this.databaseConnection.getNumberOfSynchronousConnections()) {
				tuplesPerThread = totalTuples;
			}
			else {
				tuplesPerThread = (int) Math.ceil(totalTuples / this.databaseConnection.getNumberOfSynchronousConnections());
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
					queries.add(this.databaseConnection.getSQLStatementBuilder().createBulkInsertStatement(predicate, subList));
					subList.clear();
				}
			}
			clusters.clear();
		}
		executeQueries(queries);		
	}


	public void executeQueries(Queue<String> queries) {		
		ExecutorService executorService = null;
		try {
			//Create a pool of threads to run in parallel
			executorService = Executors.newFixedThreadPool(this.databaseConnection.getNumberOfSynchronousConnections());
			List<Callable<Boolean>> threads = new ArrayList<>();
			for(int j = 0; j < this.databaseConnection.getNumberOfSynchronousConnections(); ++j) {
				//Create the threads that will run the database update statements
				threads.add(new ExecuteSynchronousSQLUpdateThread(queries, this.getDatabaseConnection().getSynchronousConnections(j)));
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

	public void deleteFacts(Collection<Atom> facts) {
		Queue<String> queries = new ConcurrentLinkedQueue<>();
		Map<Predicate, List<Atom>> clusters = Utility.clusterAtomsWithSamePredicateName(facts);

		//Find the total number of tuples that will be deleted from the database
		int totalTuples = facts.size();
		int tuplesPerThread;
		if(totalTuples < this.databaseConnection.getNumberOfSynchronousConnections()) {
			tuplesPerThread = totalTuples;
		}
		else {
			tuplesPerThread = (int) Math.ceil(totalTuples / this.databaseConnection.getNumberOfSynchronousConnections());
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
				queries.add(this.databaseConnection.getSQLStatementBuilder().createBulkDeleteStatement(predicate, subList, this.databaseConnection.getRelationNamesToRelationObjects()));
				subList.clear();
			}
		}
		executeQueries(queries);
	}

	public List<Match> answerQuery(ConjunctiveQuery q) {
		throw new UnsupportedOperationException("Method not implemented yet - use answerQueries()");
	}

//	public DatabaseInstance clone() {
//		try {
//			DatabaseConnection dbconn= new DatabaseConnection(this.databaseConnection.getDatabaseParameters(), this.databaseConnection.getSchema());
//			DatabaseInstance clone = new DatabaseInstance(dbconn);
//			return clone;
//		} catch (SQLException e) {
//			log.error(e.getMessage(),e);
//			return null;
//		}
//	}
	
	public void addQuery(ConjunctiveQuery query) {
		if(!this.clearedLastQuery)
			throw new RuntimeException("Method clearQuery should have been called in order to clear previous query's tables from the database.");
		this.clearedLastQuery = false;
		try {
			Statement sqlStatement = this.getDatabaseConnection().getSynchronousConnections(0).createStatement();
			//Create statements that set up or drop the indices for the joins in the body of the input query
			Set<String> joinIndexes = Sets.newLinkedHashSet();
			Pair<Collection<String>, Collection<String>> dropAndCreateStms = 
					this.databaseConnection.getSQLStatementBuilder().setupIndices(true, this.databaseConnection.getRelationNamesToRelationObjects(), query, this.existingIndices);
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
			Statement sqlStatement = this.getDatabaseConnection().getSynchronousConnections(0).createStatement();
			//Drop the join indices for input query
			for (String b: this.dropQueryIndexStatements) {
				sqlStatement.addBatch(b);
			}
			//Clear the database tables built for the query
			Collection<String> clearTablesSQLExpressions = this.databaseConnection.getSQLStatementBuilder().createTruncateTableStatements(this.currentQuery.getAtoms(), 
					this.databaseConnection.getRelationNamesToRelationObjects());
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
	protected List<Match> answerQueries(Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries) {
		List<Match> result = new LinkedList<>();
		//Run the SQL query statements in multiple threads
		ExecutorService executorService = null;
		try {
			//Create a pool of threads to run in parallel
			executorService = Executors.newFixedThreadPool(this.databaseConnection.getNumberOfSynchronousConnections());
			List<Callable<List<Match>>> threads = new ArrayList<>();
			for(int j = 0; j < this.databaseConnection.getNumberOfSynchronousConnections(); ++j) {
				//Create the threads that will run the database queries
				threads.add(new ExecuteSQLQueryThread(queries, this.databaseConnection.getSchema().getConstants(), this.getDatabaseConnection().getSynchronousConnections(j)));
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
	
	protected DatabaseConnection getDatabaseConnection() {
		return this.databaseConnection;
	}

	public void close() throws Exception {
		//is this the right thing to do?
		this.databaseConnection.close();
	}
}
