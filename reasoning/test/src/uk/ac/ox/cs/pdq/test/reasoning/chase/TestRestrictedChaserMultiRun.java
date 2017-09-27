package uk.ac.ox.cs.pdq.test.reasoning.chase;

import org.junit.Test;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;

/**
 * @author Gabor
 *
 */
public class TestRestrictedChaserMultiRun {
	private static final int REPEAT = 50;
	
	private DatabaseParameters getMySqlDBParams() {
		DatabaseParameters mySqlDbParam = DatabaseParameters.Derby;
		mySqlDbParam.setConnectionUrl("jdbc:mysql://localhost/");
		mySqlDbParam.setDatabaseDriver("com.mysql.jdbc.Driver");
		mySqlDbParam.setDatabaseName("test_get_triggers");
		mySqlDbParam.setDatabaseUser("root");
		mySqlDbParam.setDatabasePassword("root");
		return mySqlDbParam;
	}
	private DatabaseParameters getPostgresDBParams() {
		DatabaseParameters postgresDbParam = DatabaseParameters.Derby;
		postgresDbParam.setConnectionUrl("jdbc:postgresql://localhost/");
		postgresDbParam.setDatabaseDriver("org.postgresql.Driver");
		postgresDbParam.setDatabaseName("test_get_triggers");
		postgresDbParam.setDatabaseUser("postgres");
		postgresDbParam.setDatabasePassword("root");
		return postgresDbParam;
	}
	@Test
	public void testSingleThreadDerby() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.setup();
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testMultiThreadDerby() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(DatabaseParameters.Derby, trc.schema, 10));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testSingleThreadMySQL() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(getMySqlDBParams(), trc.schema, 1));
		try {
			trc.test_reasonUntilTermination1();
		}catch(Throwable t) {
			t.printStackTrace();
		}
		trc.tearDown();
	}
	
	@Test
	public void testMultiThreadMySQL() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(getMySqlDBParams(), trc.schema, 10));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testSingleThreadPostgres() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(getPostgresDBParams(), trc.schema, 1));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testMultiThreadPostgres() throws Exception {
		TestRestrictedChaser trc = new TestRestrictedChaser();
		trc.createSchema();
		trc.setup(new DatabaseConnection(getPostgresDBParams(), trc.schema, 10));
		trc.test_reasonUntilTermination1();
		trc.tearDown();
	}
	
	@Test
	public void testLongRunningMultiThreadMySql() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(new DatabaseConnection(getMySqlDBParams(), trc.schema, 1));
			trc.test_reasonUntilTermination1();
			trc.tearDown();
		}
	}
	
	@Test
	public void testLongRunningMultiThreadPostgres() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(new DatabaseConnection(getPostgresDBParams(), trc.schema, 1));
			trc.test_reasonUntilTermination1();
			trc.tearDown();
		}
	}
	
	@Test
	public void testLongRunningMultiThreadDerby() throws Exception {
		for (int i = 0; i < REPEAT; i++) {
			TestRestrictedChaser trc = new TestRestrictedChaser();
			trc.createSchema();
			trc.setup(new DatabaseConnection(DatabaseParameters.Derby, trc.schema, 1));
			trc.test_reasonUntilTermination1();
			trc.tearDown();
		}
	}


}
