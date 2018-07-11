package uk.ac.ox.cs.pdq.test.reasoning.chase.state;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Runs the TestChaseStep test REPEAT times, using the default 10 connections in the database manager.
 * @author Gabor
 * 
 */
public class TestChaseStepMultiRun extends PdqTest {
	private static final int REPEAT = 10;
	private boolean useInternal = true;

	/** Executes the TestChaseSteps's test once.
	 * @throws Exception
	 */
	@Test
	public void testMultiThreadPostgres() throws Exception {
		TestChaseSteps tcs = new TestChaseSteps();
		tcs.setupMocks();
		tcs.setConnection(createConnection(tcs.schema));
		tcs.test_chaseStep();
		tcs.tearDown();
	}

	/** Executes the TestChaseSteps's test REPEAT times, In each loop it adds and deletes facts REPEAT times. 
	 * @throws Exception
	 */
	@Test
	public void testLongRunningMultiThreadPostgres() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestChaseSteps tcs = new TestChaseSteps();
			tcs.setupMocks();
			tcs.setConnection(createConnection(tcs.schema));
			tcs.test_chaseStepInit();
			for (int j = 0; j < REPEAT; j++) {
				tcs.test_chaseStepAddFacts();
				tcs.test_chaseStepMain(j);
				tcs.test_chaseStepDeleteFacts();
			}
			tcs.tearDown();
		}
	}

	/** Creates a default connection to the external database manager.
	 * @param parameters
	 * @param schema
	 * @return
	 */
	private DatabaseManager createConnection(Schema schema) {
		if (useInternal ) {
			try {
				InternalDatabaseManager idm = new InternalDatabaseManager();
				idm.initialiseDatabaseForSchema(schema);
				return idm;
			} catch (DatabaseException e) {
				e.printStackTrace();
				Assert.fail("Database Creation failed");
				return null;
			}
		} else
			return createPostgresConnection(schema);
	}
	private DatabaseManager createPostgresConnection(Schema schema) {
		try {
			ExternalDatabaseManager edm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
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
