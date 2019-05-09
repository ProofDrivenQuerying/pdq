package uk.ac.ox.cs.pdq.test.reasoning;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.reasoning.UserQueryExecutor;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * @author gabor tests the UserQueryExecutor in both internal db mode and
 *         external db mode.
 */
public class UserQueryExecutorTest extends PdqTest {

	public UserQueryExecutorTest() {
	}

	@Test
	public void internalTest() throws DatabaseException, IOException {
		DatabaseManager db = new InternalDatabaseManager();
		test(db);
	}

	@Test
	public void ExternalTest() throws DatabaseException, IOException {
		DatabaseManager db = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		test(db);
	}

	private void test(DatabaseManager db) throws DatabaseException, IOException {
		TestScenario ts = super.getScenario1();
		db.initialiseDatabaseForSchema(ts.getSchema());
		db.addFacts(ts.getExampleAtoms1());

		UserQueryExecutor executor = new UserQueryExecutor(db);
		File outputFile = new File("outputFile.csv").getAbsoluteFile();
		outputFile.delete();
		executor.executeQuery(ts.getQuery(), outputFile);
		Assert.assertTrue(outputFile.exists());
		Assert.assertEquals(404, outputFile.length());

	}

	@Test
	public void internalTestCAFiltering() throws DatabaseException, IOException {
		DatabaseManager db = new InternalDatabaseManager();
		testCAFiltering(db);
	}

	@Test
	public void ExternalTestCAFiltering() throws DatabaseException, IOException {
		DatabaseManager db = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		testCAFiltering(db);
	}

	private void testCAFiltering(DatabaseManager db) throws DatabaseException, IOException {
		TestScenario ts = super.getScenario1();
		db.initialiseDatabaseForSchema(ts.getSchema());
		db.addFacts(ts.getExampleAtoms1());
		db.addFacts(Arrays.asList(new Atom[] { Atom.create(ts.getSchema().getRelation(2),
				new Term[] { TypedConstant.create(12), TypedConstant.create(31), UntypedConstant.create("k100") }) }));

		UserQueryExecutor executor = new UserQueryExecutor(db);
		File outputFile = new File("outputFile.csv").getAbsoluteFile();
		outputFile.delete();
		executor.executeQuery(ts.getQuery(), outputFile);
		Assert.assertTrue(outputFile.exists());
		Assert.assertEquals(404, outputFile.length());
	}
	
	@After
	public void tearDown() {
		File outputFile = new File("outputFile.csv").getAbsoluteFile();
		outputFile.delete();
	}
}
