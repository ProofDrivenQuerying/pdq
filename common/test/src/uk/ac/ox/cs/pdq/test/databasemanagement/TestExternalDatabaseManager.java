package uk.ac.ox.cs.pdq.test.databasemanagement;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * tests the creation and basic usages of a database. simpleDatabaseCreationXYZ
 * test: - create database - create tables - add facts, - retrieve facts -
 * delete facts virtualDatabaseCreationXYZ test: - same as above but checking
 * the cache functions as well.
 * 
 * XYZ in test names refer to - Derby - MySql - Postgres - Memory
 * 
 * @author Gabor
 *
 */
public class TestExternalDatabaseManager extends PdqTest {

	/**
	 * tests the database manager creating a database for a single relation that
	 * contains string attributes. No queries, just add and get facts. Uses Derby
	 * database provider
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void simpleDatabaseCreationDerby() throws DatabaseException {
		simpleDatabaseCreation(DatabaseParameters.Derby);
	}

	/**
	 * tests the database manager creating a database for a single relation that
	 * contains string attributes. No queries, just add and get facts. Uses MySQL
	 * database provider
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void simpleDatabaseCreationMySql() throws DatabaseException {
		simpleDatabaseCreation(DatabaseParameters.MySql);
	}

	/**
	 * tests the database manager creating a database for a single relation that
	 * contains string attributes. No queries, just add and get facts. Uses Postgres
	 * database provider
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void simpleDatabaseCreationPostgres() throws DatabaseException {
		simpleDatabaseCreation(DatabaseParameters.Postgres);
	}

	/**
	 * tests the database manager creating a database for a single relation that
	 * contains string attributes. No queries, just add and get facts.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	private void simpleDatabaseCreation(DatabaseParameters parameters) throws DatabaseException {
		Relation R = Relation.create("R", new Attribute[] { a_s, b_s, c_s }, new AccessMethod[] { this.method0, this.method2 });

		ExternalDatabaseManager manager = new ExternalDatabaseManager(parameters);
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R }));
		// ADD facts
		Atom a1 = Atom.create(R, new Term[] { UntypedConstant.create("12"), UntypedConstant.create("13"), UntypedConstant.create("14") });
		Atom a2 = Atom.create(R, new Term[] { TypedConstant.create(12), TypedConstant.create(13), TypedConstant.create(14) });
		List<Atom> facts = new ArrayList<>();
		facts.add(a1);
		facts.add(a2);
		manager.addFacts(facts);
		Collection<Atom> getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertTrue(facts.size() == getFacts.size() && facts.containsAll(getFacts));

		// Test duplicated storage - stored data should not change when we add the same
		// set twice
		try {
			manager.addFacts(facts);
			Assert.fail("Should have thrown error for insering duplicates");
		} catch (Exception e) {}
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts.size(), getFacts.size());

		// DELETE
		manager.deleteFacts(facts);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		manager.shutdown();
	}

	/**
	 * Tests the basic functions of the VirtualDatabaseManager, using first a single
	 * table with Int attributes, then repeats the same test with String attributes.
	 * 
	 * This case test the Derby driver.
	 */
	@Test
	public void externalDatabaseCreationDerby() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		externalDatabaseCreationInt(DatabaseParameters.Derby);
		externalDatabaseCreationString(DatabaseParameters.Derby);
	}
	
	/**
	 * Tests the basic functions of the VirtualDatabaseManager, using first a single
	 * table with Int attributes, then repeats the same test with String attributes.
	 * 
	 * This case test the MySQL driver.
	 */
	@Test
	public void externalDatabaseCreationMySql() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		externalDatabaseCreationInt(DatabaseParameters.MySql);
		externalDatabaseCreationString(DatabaseParameters.MySql);
	}

	/**
	 * Tests the basic functions of the VirtualDatabaseManager, using first a single
	 * table with Int attributes, then repeats the same test with String attributes.
	 * 
	 * This case test the Postgres driver.
	 */
	@Test
	public void externalDatabaseCreationPostgres() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		externalDatabaseCreationInt(DatabaseParameters.Postgres);
		externalDatabaseCreationString(DatabaseParameters.Postgres);
	}


	/**
	 * Tests the VirtualMultiInstanceDatabaseManager by creating 2 instances and
	 * adding facts to each, checking if we get back only the facts that we added to
	 * the current instance.
	 * 
	 * Uses a schema with only one relation that has integer attributes.
	 * 
	 * Also tests a basic query: []R(x,y,z) that should retrieve all data from the
	 * table in the given database instance.
	 */
	private void externalDatabaseCreationInt(DatabaseParameters parameters) throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// create a simple manager
		ExternalDatabaseManager manager = new ExternalDatabaseManager(parameters);
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R }));
		Atom[] facts = new Atom[] { Atom.create(this.R, new Term[] { TypedConstant.create(1), TypedConstant.create(10), TypedConstant.create(100) }) };
		manager.addFacts(Arrays.asList(facts));
		
		Assert.assertEquals(1,manager.getCachedFacts().size()); // this will do the same as the next line, since this database manager have no cache.
		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());
		
		Atom[] facts2 = new Atom[] { Atom.create(this.R, new Term[] { TypedConstant.create(2), TypedConstant.create(20), TypedConstant.create(200) }) };
		manager.addFacts(Arrays.asList(facts2));
		Assert.assertEquals(2, manager.getCachedFacts().size());
		Assert.assertEquals(2, manager.getFactsFromPhysicalDatabase().size());

		// Normal query
		Atom a1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { x, y, z }, a1);
		List<Match> answer = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
		Assert.assertEquals(2, answer.size());

		// Typed untyped difference
		Collection<Atom> physicalData = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(2, physicalData.size());
		
		// attempt to delete one existing fact
		manager.deleteFacts(Arrays.asList(facts2));
		Collection<Atom> getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(1, getFacts.size());

		// delete the other
		manager.deleteFacts(Arrays.asList(facts));
		getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		manager.shutdown();
	}

	/**
	 * Same as the virtualDatabaseCreationInt but the tables have String constants
	 * 
	 */
	private void externalDatabaseCreationString(DatabaseParameters parameters) throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		SimpleDateFormat sdfmt1 = new SimpleDateFormat("yyyy-MM-dd");
		Date dDate = null;
		try {
			dDate = sdfmt1.parse("2017-09-21");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// create a simple manager
		DatabaseParameters p = (DatabaseParameters) parameters.clone();
		p.setProperty("database.isvirtual", Boolean.TRUE.toString());
		ExternalDatabaseManager manager = new ExternalDatabaseManager(p);
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R_s }));
		Atom[] facts = new Atom[] {
				Atom.create(this.R_s, new Term[] { TypedConstant.create("A1"), TypedConstant.create(dDate), TypedConstant.create(100), TypedConstant.create(13) }) };
		manager.addFacts(Arrays.asList(facts));
		// Assert.assertEquals(/*1*/0,manager.getCachedFacts().size());
		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());

		Atom[] facts2 = new Atom[] {
				Atom.create(this.R, new Term[] { TypedConstant.create("B2"), TypedConstant.create("B20"), TypedConstant.create("B200"), TypedConstant.create(14) }) };
		manager.addFacts(Arrays.asList(facts2));
		Assert.assertEquals(2, manager.getCachedFacts().size());
		Assert.assertEquals(2, manager.getFactsFromPhysicalDatabase().size());

		// Normal query
		Atom a1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z"), Variable.create("i") });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { x, y, z }, a1);
		List<Match> answer = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
		Assert.assertEquals(2, answer.size());

		// Typed untyped difference
		Collection<Atom> physicalData = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(2, physicalData.size());
		Atom[] expectedResult = new Atom[] {
				Atom.create(this.R_s, new Term[] { TypedConstant.create("A1"), TypedConstant.create(dDate), TypedConstant.create(100), UntypedConstant.create("13") }) };
		if (parameters.getDatabaseDriver().contains("memory")) {
			// the memory DB will give back the TypedConstants
			Assert.assertEquals(facts[0], physicalData.iterator().next());
		} else {
			// Database storage can't map integers into typed or untyped, so it will be
			// untyped.
			Assert.assertEquals(expectedResult[0], physicalData.iterator().next());
		}

		// attempt to delete none existing fact
		manager.deleteFacts(Arrays.asList(facts2));
		Collection<Atom> getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(1, getFacts.size());

		manager.dropDatabase();
		try {
			manager.getFactsFromPhysicalDatabase();
			Assert.fail("Should have thrown exception when read from a dropped database.");
		} catch (Exception e) {
		}
		try {
			manager.getCachedFacts().size();
			Assert.fail("Should have thrown exception when read from a dropped database.");
		} catch (Exception e) {
		}		
		manager.shutdown();
	}

}
