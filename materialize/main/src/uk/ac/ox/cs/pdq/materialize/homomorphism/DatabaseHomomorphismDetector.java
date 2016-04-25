package uk.ac.ox.cs.pdq.materialize.homomorphism;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.materialize.sqlstatement.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.materialize.utility.Match;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

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
public class DatabaseHomomorphismDetector implements HomomorphismDetector {

	/** Logger. */
	private static Logger log = Logger.getLogger(DatabaseHomomorphismDetector.class);

	/**  Maps of the string representation of a constant to the constant. */
	protected final Map<String, TypedConstant<?>> constants;

	/**  Creates SQL statements to detect homomorphisms or add/delete facts in a database. */
	protected final SQLStatementBuilder builder;

	/** Map schema relation to database tables. */
	protected final Map<String, DatabaseRelation> toDatabaseTables;

	/** The open connections. */
	protected List<Connection> openConnections = new ArrayList<>();

	/** Number of parallel threads. **/
	protected final int synchronousThreads = 5;

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
	public DatabaseHomomorphismDetector(
			String driver, 
			String url, 
			String database,
			String username, 
			String password,
			SQLStatementBuilder builder,
			Schema schema,
			Map<String, DatabaseRelation> toDatabaseTables
			) throws SQLException {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.database = database;
		this.builder = builder;
		this.constants = schema.getConstants();
		this.toDatabaseTables = toDatabaseTables;
		
		for(int i = 0; i < this.synchronousThreads; ++i) {
			this.synchronousConnections.add(HomomorphismUtility.getConnection(this.driver, this.url, this.database, this.username, this.password));
		}
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
	protected DatabaseHomomorphismDetector(
			String driver, 
			String url, 
			String database,
			String username, 
			String password,
			SQLStatementBuilder builder,
			Map<String, TypedConstant<?>> constants,
			Map<String, DatabaseRelation> toDatabaseRelations
			) throws SQLException {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.database = database;
		this.builder = builder;
		this.toDatabaseTables = toDatabaseRelations;
		this.constants = constants;
		
		for(int i = 0; i < this.synchronousThreads; ++i) {
			this.synchronousConnections.add(HomomorphismUtility.getConnection(this.driver, this.url, this.database, this.username, this.password));
		}
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		for(Connection con:this.synchronousConnections) {
			con.close();
		}
		for(Connection con:this.openConnections) {
			con.close();
		}
	}

	/**
	 * Clone.
	 *
	 * @return DBHomomorphismManager
	 * @see uk.ac.ox.cs.pdq.homomorphism.HomomorphismDetector#clone()
	 */
	@Override
	public DatabaseHomomorphismDetector clone() {
		try {
			DatabaseHomomorphismDetector clone = new DatabaseHomomorphismDetector(
					this.driver, this.url, this.database, this.username, this.password,
					this.builder.clone(), 
					this.constants,
					this.toDatabaseTables);
			this.openConnections.addAll(this.synchronousConnections);
			return clone;
		} catch (SQLException e) {
			log.error(e.getMessage(),e);
			return null;
		}
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
			Q s = HomomorphismUtility.convert(source, this.toDatabaseTables, constraints);
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
			executorService = Executors.newFixedThreadPool(this.synchronousThreads);
			List<Callable<List<Match>>> threads = new ArrayList<>();
			for(int j = 0; j < this.synchronousThreads; ++j) {
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


}
