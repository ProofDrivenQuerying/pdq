package uk.ac.ox.cs.pdq.test.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.sql.DerbyStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.ExecuteSynchronousSQLUpdateThread;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.LimitReachedException;
import uk.ac.ox.cs.pdq.util.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.util.Utility;

/** 
 * Class-level unit test for SQLStatementBuilder and it's specialised sub classes like MySql and Postgres statement builders.
 * @author Gabor
 *
 */
public class SQLStatementBuilderTest extends PdqTest {
	private final boolean print = false;
	protected final static int insertCacheSize = 1000;
	private int repeat = 100;
	
	protected final long timeout = 3600000;
	private final String[][] EXPECTED_RESULTS = new String[][] {
		new String[] {"k1","k2","k3","k4","k5","k6"},
		new String[] {"c","c","c","c","c","c"},
		new String[] {"c1","c2","c3","c4","John","Michael"},
	};
	private DatabaseConnection dcMySql;
	private DatabaseConnection dcPostgresSql;
	private DatabaseConnection dcDerby;
	@Before
	public void setup() throws Exception {
		super.setup();
		dcMySql = new DatabaseConnection(DatabaseParameters.MySql, this.testSchema1);
		dcPostgresSql = new DatabaseConnection(DatabaseParameters.Postgres, this.testSchema1);
		dcDerby = new DatabaseConnection(DatabaseParameters.Derby, this.testSchema1);
		test_derbyAddFacts();
		test_mySqlAddFacts();
		test_PostgresAddFacts();
		deleteAllFacts();
	}
	
	@Test 
	public void test_derbyAddFacts() throws SQLException {	
		Collection<Atom> facts = createTestFacts();
		DatabaseConnection dc = dcDerby;
		addFacts(facts,dc);
		
		Statement sqlStatement = dc.getSynchronousConnections(0).createStatement();
		ResultSet rs = sqlStatement.executeQuery("select * from "+dc.getDatabaseParameters().getDatabaseName()+".R1");
		if (print) System.out.println("querying SELECT * FROM  "+dc.getDatabaseParameters().getDatabaseName()+".R1");
		checkTestFacts(rs,print);
	}
	public void deleteAllFacts() throws SQLException {	
		Statement sqlStatement = dcDerby.getSynchronousConnections(0).createStatement();
		sqlStatement.executeUpdate("delete from  "+dcDerby.getDatabaseParameters().getDatabaseName()+".R1");
		sqlStatement = dcMySql.getSynchronousConnections(0).createStatement();
		sqlStatement.executeUpdate("delete from "+dcMySql.getDatabaseParameters().getDatabaseName()+".R1");
		sqlStatement = dcPostgresSql.getSynchronousConnections(0).createStatement();
		sqlStatement.executeUpdate("delete from  "+dcPostgresSql.getDatabaseParameters().getDatabaseName()+".R1");
	}
	
	@Test 
	public void test_derbyAddFacts100() throws SQLException {
		Collection<Atom> facts = createTestFacts();
		DatabaseConnection dc = dcDerby;
		for (int i = 0; i < repeat; i++) {
			addFacts(facts,dc);
		}
		
		Statement sqlStatement = dc.getSynchronousConnections(0).createStatement();
		ResultSet rs = sqlStatement.executeQuery("select * from "+dc.getDatabaseParameters().getDatabaseName()+".R1");
		if (print) System.out.println("querying SELECT * FROM  "+dc.getDatabaseParameters().getDatabaseName()+".R1");
  		Assert.assertEquals(6*repeat, checkTestFacts(rs,print));
	}
	
	@Test 
	public void test_mySqlAddFacts() throws SQLException {	
		Collection<Atom> facts = createTestFacts();
		DatabaseConnection dc = dcMySql;
		addFacts(facts,dc);
		
		Statement sqlStatement = dc.getSynchronousConnections(0).createStatement();
		ResultSet rs = sqlStatement.executeQuery("select * from R1");
		if (print) System.out.println("querying SELECT * FROM R1");
		checkTestFacts(rs,print);
	}
	@Test 
	public void test_mySqlAddFacts100() throws SQLException {	
		Collection<Atom> facts = createTestFacts();
		DatabaseConnection dc = dcMySql;
		for (int i = 0; i < repeat; i++) {
			addFacts(facts,dc);
		}
		Statement sqlStatement = dc.getSynchronousConnections(0).createStatement();
		ResultSet rs = sqlStatement.executeQuery("select * from R1");
		if (print) System.out.println("querying SELECT * FROM R1");
		Assert.assertEquals(6*repeat, checkTestFacts(rs,print));
	}
	
	@Test 
	public void test_PostgresAddFacts() throws SQLException {	
		Collection<Atom> facts = createTestFacts();
		DatabaseConnection dc = dcPostgresSql;
		addFacts(facts,dc);
		
		Statement sqlStatement = dc.getSynchronousConnections(0).createStatement();
		ResultSet rs = sqlStatement.executeQuery("select * from R1");
		if (print) System.out.println("querying SELECT * FROM R1");
		Assert.assertEquals(6, checkTestFacts(rs,print));
	}
	@Test 
	public void test_PostgresAddFacts2() throws SQLException {	
		Collection<Atom> facts = createTestFacts();
		DatabaseConnection dc = dcPostgresSql;
		addFacts(facts,dc);
		
		Statement sqlStatement = dc.getSynchronousConnections(0).createStatement();
		ResultSet rs = sqlStatement.executeQuery("select * from R1");
		if (print) System.out.println("querying SELECT * FROM R1");
		Assert.assertEquals(6, checkTestFacts(rs,print));
	}
	
	@Test 
	public void test_PostgresAddFacts100() throws SQLException {	

		Collection<Atom> facts = createTestFacts();
		DatabaseConnection dc = dcPostgresSql;
		for (int i = 0; i < repeat; i++) {
			addFacts(facts,dc);
		}
		Statement sqlStatement = dc.getSynchronousConnections(0).createStatement();
		ResultSet rs = sqlStatement.executeQuery("select * from R1");
		if (print) System.out.println("querying SELECT * FROM R1");
		Assert.assertEquals(6*repeat, checkTestFacts(rs,print));
	}
	

	private int checkTestFacts(ResultSet rs,boolean print) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		if (print) {
			for (int i = 1; i <= columnsNumber; i++) {
				System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println("");
		}
		int resultsRowCount = 0;
		while (rs.next()) {
			resultsRowCount++;
			String columnValue = rs.getString(1);
			if (print) System.out.print(columnValue + "\t");
			int expectedIndex = -1;
			for (int i = 0; i < EXPECTED_RESULTS[0].length; i++) {
				if (EXPECTED_RESULTS[0][i].equals(columnValue)) {
					Assert.assertEquals(-1, expectedIndex);// we should not find two match
					expectedIndex = i;
				}
			}
			Assert.assertNotEquals(-1, expectedIndex);// we should have found one match
			for (int i = 2; i <= columnsNumber-1; i++) {
				columnValue = rs.getString(i);
				if (print) System.out.print(columnValue + "\t");
				Assert.assertEquals(EXPECTED_RESULTS[i - 1][expectedIndex], columnValue);
			}
			if (print) System.out.println("");
		}
		return resultsRowCount;
	}

	private Collection<Atom> createTestFacts() {
		int instanceid = 0;
		Atom f20 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k1"), UntypedConstant.create("c"),UntypedConstant.create("c1"), TypedConstant.create(instanceid++)});

		Atom f21 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k2"), UntypedConstant.create("c"),UntypedConstant.create("c2"), TypedConstant.create(instanceid++)});

		Atom f22 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k3"), UntypedConstant.create("c"),UntypedConstant.create("c3"), TypedConstant.create(instanceid++)});

		Atom f23 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k4"), UntypedConstant.create("c"),UntypedConstant.create("c4"), TypedConstant.create(instanceid++)});

		Atom f24 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k5"), UntypedConstant.create("c"),TypedConstant.create(new String("John")), TypedConstant.create(instanceid++)});

		Atom f25 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k6"), UntypedConstant.create("c"),TypedConstant.create(new String("Michael")), TypedConstant.create(instanceid++)});
		return Lists.newArrayList(f20,f21,f22,f23,f24,f25);
	}

	/**
	 * Copied from DatabaseInstance
	 */
	private void addFacts(Collection<Atom> facts, DatabaseConnection databaseConnection) {
		Queue<String> queries = new ConcurrentLinkedQueue<>();
		if(databaseConnection.getSQLStatementBuilder() instanceof DerbyStatementBuilder) {
			queries.addAll(databaseConnection.getSQLStatementBuilder().createInsertStatements(facts, databaseConnection.getRelationNamesToDatabaseTables()));
		}
		else {
			Map<Predicate, List<Atom>> clusters = Utility.clusterAtomsWithSamePredicateName(facts);
			//Find the total number of tuples that will be inserted in the database
			int totalTuples = facts.size();
			int tuplesPerThread;
			if(totalTuples < databaseConnection.getNumberOfSynchronousConnections()) {
				tuplesPerThread = totalTuples;
			}
			else {
				tuplesPerThread = (int) Math.ceil(totalTuples / databaseConnection.getNumberOfSynchronousConnections());
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
					queries.add(databaseConnection.getSQLStatementBuilder().createBulkInsertStatement(predicate, subList));
					subList.clear();
				}
			}
			clusters.clear();
		}
		executeQueries(queries,databaseConnection);		
	}
	private void executeQueries(Queue<String> queries,DatabaseConnection databaseConnection) {		
		ExecutorService executorService = null;
		try {
			//Create a pool of threads to run in parallel
			executorService = Executors.newFixedThreadPool(databaseConnection.getNumberOfSynchronousConnections());
			List<Callable<Boolean>> threads = new ArrayList<>();
			for(int j = 0; j < databaseConnection.getNumberOfSynchronousConnections(); ++j) {
				//Create the threads that will run the database update statements
				threads.add(new ExecuteSynchronousSQLUpdateThread(queries, databaseConnection.getSynchronousConnections(j)));
			}
			long start = System.currentTimeMillis();
			try {
				for(Future<Boolean> output:executorService.invokeAll(threads, this.timeout, TimeUnit.MILLISECONDS)){
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
	
}
