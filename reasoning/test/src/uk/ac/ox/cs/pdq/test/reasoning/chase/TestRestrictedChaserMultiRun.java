package uk.ac.ox.cs.pdq.test.reasoning.chase;

import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Schema;
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
	public void testSingleThreadPostgres() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.setup();
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}

	@Test
	public void testMultiThreadPostgres() throws Exception {

		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(createConnection(trc.schema));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}

	@Test
	public void testLongRunningMultiThreadPostgres() throws Exception {

		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(createConnection(trc.schema));
			trc.test_reasonUntilTermination1();
			trc.tearDown();

		}
	}

	private DatabaseManager createConnection(Schema s) {
		try {
			InternalDatabaseManager dm = new InternalDatabaseManager();
			dm.initialiseDatabaseForSchema(s);
			return dm;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
