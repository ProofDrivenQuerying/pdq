package uk.ac.ox.cs.pdq.test.reasoning.chase.state;

import org.junit.Test;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Runs the TestChaseStep test with a multiThreaded db connection.
 * @author Gabor
 * 
 */
public class TestChaseStepMultiRun extends PdqTest {
	private static final int REPEAT = 35;

	@Test
	public void testSingleThreadDerby() throws Exception {
		TestChaseSteps tcs = new TestChaseSteps();
		tcs.setup();
		tcs.test_chaseStep();
		tcs.tearDown();
	}

	@Test
	public void testMultiThreadDerby() throws Exception {
		TestChaseSteps tcs = new TestChaseSteps();
		tcs.setupMocks();
		tcs.setConnection(new DatabaseConnection(DatabaseParameters.Derby, tcs.schema, 10));
		tcs.test_chaseStep();
		tcs.tearDown();
	}

	@Test
	public void testSingleThreadPostgres() throws Exception {
		TestChaseSteps tcs = new TestChaseSteps();
		tcs.setupMocks();
		tcs.setConnection(new DatabaseConnection(DatabaseParameters.Postgres, tcs.schema, 1));
		tcs.test_chaseStep();
		tcs.tearDown();
	}

	@Test
	public void testMultiThreadPostgres() throws Exception {
		TestChaseSteps tcs = new TestChaseSteps();
		tcs.setupMocks();
		tcs.setConnection(new DatabaseConnection(DatabaseParameters.Postgres, tcs.schema, 10));
		tcs.test_chaseStep();
		tcs.tearDown();
	}

	@Test
	public void testSingleThreadMySql() throws Exception {
		TestChaseSteps tcs = new TestChaseSteps();
		tcs.setupMocks();
		tcs.setConnection(new DatabaseConnection(DatabaseParameters.MySql, tcs.schema, 1));
		tcs.test_chaseStep();
		tcs.tearDown();
	}

	@Test
	public void testMultiThreadMySql() throws Exception {
		TestChaseSteps tcs = new TestChaseSteps();
		tcs.setupMocks();
		tcs.setConnection(new DatabaseConnection(DatabaseParameters.MySql, tcs.schema, 10));
		tcs.test_chaseStep();
		tcs.tearDown();
	}

	@Test
	public void testLongRunningMultiThreadMySql() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestChaseSteps tcs = new TestChaseSteps();
			tcs.setupMocks();
			tcs.setConnection(new DatabaseConnection(DatabaseParameters.MySql, tcs.schema, 10));
			tcs.test_chaseStepInit();
			for (int j = 0; j < REPEAT; j++) {
				tcs.test_chaseStepAddFacts();
				tcs.test_chaseStepMain(j);
				tcs.test_chaseStepDeleteFacts();
			}
			tcs.tearDown();
		}
	}

	@Test
	public void testLongRunningMultiThreadPostgres() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestChaseSteps tcs = new TestChaseSteps();
			tcs.setupMocks();
			tcs.setConnection(new DatabaseConnection(DatabaseParameters.Postgres, tcs.schema, 10));
			tcs.test_chaseStepInit();
			for (int j = 0; j < REPEAT; j++) {
				tcs.test_chaseStepAddFacts();
				tcs.test_chaseStepMain(j);
				tcs.test_chaseStepDeleteFacts();
			}
			tcs.tearDown();
		}
	}

	@Test
	public void testLongRunningMultiThreadDerby() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestChaseSteps tcs = new TestChaseSteps();
			tcs.setupMocks();
			tcs.setConnection(new DatabaseConnection(DatabaseParameters.Derby, tcs.schema, 10));
			tcs.test_chaseStepInit();
			for (int j = 0; j < REPEAT; j++) {
				tcs.test_chaseStepAddFacts();
				tcs.test_chaseStepMain(j);
				tcs.test_chaseStepDeleteFacts();
			}
			tcs.tearDown();
		}
	}

}
