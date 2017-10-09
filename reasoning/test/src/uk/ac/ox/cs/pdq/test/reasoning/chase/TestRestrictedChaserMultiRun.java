package uk.ac.ox.cs.pdq.test.reasoning.chase;

import org.junit.Test;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Runs the test_reasonUntilTermination1 test from TestRestrictedChaser with
 * different database connections. Tests multithreaded connections and all 3
 * default databases.
 * 
 * @author Gabor
 *
 */
public class TestRestrictedChaserMultiRun extends PdqTest {
	private static final int REPEAT = 50;

	@Test
	public void testSingleThreadDerby() throws Exception {
		DatabaseChaseInstance.resetFacts();			
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.setup();
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}

	@Test
	public void testMultiThreadDerby() throws Exception {
		DatabaseChaseInstance.resetFacts();			
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(DatabaseParameters.Derby, trc.schema, 10));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}

	@Test
	public void testSingleThreadMySQL() throws Exception {
		DatabaseChaseInstance.resetFacts();			
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(DatabaseParameters.MySql, trc.schema, 1));
		try {
			trc.test_reasonUntilTermination1();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		trc.tearDown();
	}

	@Test
	public void testMultiThreadMySQL() throws Exception {
		DatabaseChaseInstance.resetFacts();			
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(DatabaseParameters.MySql, trc.schema, 10));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}

	@Test
	public void testSingleThreadPostgres() throws Exception {
		DatabaseChaseInstance.resetFacts();			
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(DatabaseParameters.Postgres, trc.schema, 1));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}

	@Test
	public void testMultiThreadPostgres() throws Exception {
		DatabaseChaseInstance.resetFacts();			
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(DatabaseParameters.Postgres, trc.schema, 10));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testLongRunningMultiThreadMySql() throws Exception {
		DatabaseChaseInstance.resetFacts();			
		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(new DatabaseConnection(DatabaseParameters.MySql, trc.schema, 10));
			trc.test_reasonUntilTermination1();
			trc.tearDown();
			DatabaseChaseInstance.resetFacts();			
		}
	}
	
	@Test
	public void testLongRunningMultiThreadPostgres() throws Exception {
		DatabaseChaseInstance.resetFacts();			
		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(new DatabaseConnection(DatabaseParameters.Postgres, trc.schema, 10));
			trc.test_reasonUntilTermination1();
			trc.tearDown();
			DatabaseChaseInstance.resetFacts();			
		}
	}
	
	@Test
	public void testLongRunningMultiThreadDerby() throws Exception {
		DatabaseChaseInstance.resetFacts();			
		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(new DatabaseConnection(DatabaseParameters.Derby, trc.schema, 10));
			trc.test_reasonUntilTermination1();
			trc.tearDown();
			DatabaseChaseInstance.resetFacts();			
		}
	}

}
