package uk.ac.ox.cs.pdq.test.reasoning.chase.state;

import org.junit.Test;

import org.junit.Assert;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Runs the TestChaseStep test with a multiThreaded db connection.
 * @author Gabor
 * 
 */
public class TestChaseStepMultiRun extends PdqTest {
	private static final int REPEAT = 35;

	@Test
	public void testMultiThreadPostgres() throws Exception {
		TestChaseSteps tcs = new TestChaseSteps();
		tcs.setupMocks();
		tcs.setConnection(createConnection(DatabaseParameters.Postgres, tcs.schema));
		tcs.test_chaseStep();
		tcs.tearDown();
	}

	@Test
	public void testLongRunningMultiThreadPostgres() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestChaseSteps tcs = new TestChaseSteps();
			tcs.setupMocks();
			tcs.setConnection(createConnection(DatabaseParameters.Postgres, tcs.schema));
			tcs.test_chaseStepInit();
			for (int j = 0; j < REPEAT; j++) {
				tcs.test_chaseStepAddFacts();
				tcs.test_chaseStepMain(j);
				tcs.test_chaseStepDeleteFacts();
			}
			tcs.tearDown();
		}
	}

	private DatabaseManager createConnection(DatabaseParameters parameters, Schema schema) {
		try {
			ExternalDatabaseManager edm = new ExternalDatabaseManager(parameters);
			LogicalDatabaseInstance vmidm;
			vmidm = new LogicalDatabaseInstance(new MultiInstanceFactCache(), edm, 1);
			vmidm.initialiseDatabaseForSchema(schema);
			return vmidm;
		} catch (DatabaseException e) {
			e.printStackTrace();
			Assert.fail("Database Creation failed");
			return null;
		}
	}

}
