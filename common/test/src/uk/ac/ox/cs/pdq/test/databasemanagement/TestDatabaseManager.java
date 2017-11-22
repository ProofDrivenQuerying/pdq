package uk.ac.ox.cs.pdq.test.databasemanagement;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * tests the creation and basic usages of a database.
 * 
 * @author Gabor
 *
 */
public class TestDatabaseManager extends PdqTest {

	@Test
	public void simpleDatabaseCreationDerby() throws DatabaseException {
		simpleDatabaseCreation(DatabaseParameters.Derby);
	}

	@Test
	public void simpleDatabaseCreationMySql() throws DatabaseException {
		simpleDatabaseCreation(DatabaseParameters.MySql);
	}

	@Test
	public void simpleDatabaseCreationPostgres() throws DatabaseException {
		simpleDatabaseCreation(DatabaseParameters.Postgres);
	}

	//@Test
	public void simpleDatabaseCreatioMemory() throws DatabaseException {
		simpleDatabaseCreation(DatabaseParameters.Memory);
	}

	private void simpleDatabaseCreation(DatabaseParameters parameters) throws DatabaseException {
		DatabaseManager manager = new DatabaseManager(parameters);
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R, S, T }));
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(12), TypedConstant.create(13), TypedConstant.create(14) });
		List<Atom> facts = new ArrayList<>();
		// ADD
		facts.add(a1);
		manager.addFacts(facts);
		Collection<Atom> getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts, getFacts);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts.size(), getFacts.size());
		
		// Test duplicated storage - stored data should not change when we add the same set twice
		manager.addFacts(facts);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts.size(), getFacts.size());
		
		// DELETE
		manager.deleteFacts(facts);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		manager.shutdown();
	}

	//@Test
	public void virtualDatabaseCreationDerby() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		virtualDatabaseCreationInt(DatabaseParameters.Derby);
		virtualDatabaseCreationString(DatabaseParameters.Derby);
	}

	//@Test
	public void virtualDatabaseCreationMySql() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		virtualDatabaseCreationInt(DatabaseParameters.MySql);
		virtualDatabaseCreationString(DatabaseParameters.MySql);
	}

	//@Test
	public void virtualDatabaseCreationPostgres() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		virtualDatabaseCreationInt(DatabaseParameters.Postgres);
		virtualDatabaseCreationString(DatabaseParameters.Postgres);
	}

	//@Test
	public void virtualDatabaseCreationMemory() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		virtualDatabaseCreationInt(DatabaseParameters.Memory);
		virtualDatabaseCreationString(DatabaseParameters.Memory);
	}

	private void virtualDatabaseCreationInt(DatabaseParameters parameters) throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// create a simple manager
//		DatabaseParameters p = (DatabaseParameters) parameters.clone();
//		p.setProperty("database.isvirtual", Boolean.TRUE.toString());
//		OLD_DatabaseManager manager = OLD_DatabaseManager.create(p);
//		String instanceID1 = manager.getDatabaseInstanceID();
//		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R, S, T }));
//		Atom[] facts = new Atom[] { Atom.create(this.R, new Term[] { TypedConstant.create(1), TypedConstant.create(10), TypedConstant.create(100) }) };
//		manager.addFacts(Arrays.asList(facts));
//		// Assert.assertEquals(/*1*/0,manager.getCachedFacts().size());
//		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());
//
//		// create a child of the previous manager, forming a virtual database instance
//		// over the original one.
//		manager.setDatabaseInstanceID(instanceID1 + "B");
//		String instanceID2 = manager.getDatabaseInstanceID();
//		Assert.assertNotEquals(instanceID1, instanceID2);
//
//		Assert.assertEquals(0, manager.getCachedFacts().size());
//		Atom[] facts2 = new Atom[] { Atom.create(this.R, new Term[] { TypedConstant.create(2), TypedConstant.create(20), TypedConstant.create(200) }) };
//		manager.addFacts(Arrays.asList(facts2));
//		Assert.assertEquals(1, manager.getCachedFacts().size());
//		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());
//
//		// Normal query
//		Atom a1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
//		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { x, y, z }, a1);
//		List<Match> answer = manager.answerQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
//		Assert.assertEquals(1, answer.size());
//
//		// switching back to first instance
//		manager.setDatabaseInstanceID(instanceID1);
//		Assert.assertArrayEquals(facts, manager.getCachedFacts().toArray(new Atom[manager.getCachedFacts().size()]));
//		// Typed untyped difference
//		Collection<Atom> physicalData = manager.getFactsFromPhysicalDatabase();
//		Assert.assertEquals(facts.length, physicalData.size());
//		if (parameters.getDatabaseDriver().contains("memory")) {
//			// the memory DB will give back the TypedConstants
//			Assert.assertEquals(facts[0], physicalData.iterator().next());
//		}
//
//		// attempt to delete none existing fact
//		manager.deleteFacts(Arrays.asList(facts2));
//		Collection<Atom> getFacts = manager.getCachedFacts();
//		Assert.assertNotNull(getFacts);
//		Assert.assertEquals(1, getFacts.size());
//
//		manager.setDatabaseInstanceID(instanceID2);
//		// delete
//		getFacts = manager.getCachedFacts();
//		Assert.assertNotNull(getFacts);
//		Assert.assertEquals(1, getFacts.size());
//		manager.deleteFacts(Arrays.asList(facts2));
//		getFacts = manager.getCachedFacts();
//		Assert.assertNotNull(getFacts);
//		Assert.assertEquals(0, getFacts.size());
//		getFacts = manager.getFactsFromPhysicalDatabase();
//		Assert.assertNotNull(getFacts);
//		Assert.assertEquals(0, getFacts.size());
//		manager.shutdown(true);
	}

	/**
	 * Same as the virtualDatabaseCreationInt but the tables have String constants
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 * @throws NoSuchMethodException  
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private void virtualDatabaseCreationString(DatabaseParameters parameters) throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		SimpleDateFormat sdfmt1 = new SimpleDateFormat("yyyy-MM-dd");
//		Date dDate = null;
//		try {
//			dDate = sdfmt1.parse("2017-09-21");
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//
//		// create a simple manager
//		DatabaseParameters p = (DatabaseParameters) parameters.clone();
//		p.setProperty("database.isvirtual", Boolean.TRUE.toString());
//		OLD_DatabaseManager manager = OLD_DatabaseManager.create(p);
//		String instanceID1 = manager.getDatabaseInstanceID();
//		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R_s, S_s, T_s }));
//		Atom[] facts = new Atom[] {
//				Atom.create(this.R_s, new Term[] { TypedConstant.create("A1"), TypedConstant.create(dDate), TypedConstant.create(100), TypedConstant.create(13) }) };
//		manager.addFacts(Arrays.asList(facts));
//		// Assert.assertEquals(/*1*/0,manager.getCachedFacts().size());
//		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());
//
//		// create a child of the previous manager, forming a virtual database instance
//		// over the original one.
//		manager.setDatabaseInstanceID(instanceID1);
//		manager.setDatabaseInstanceID(instanceID1 + "B");
//		String instanceID2 = manager.getDatabaseInstanceID();
//		Assert.assertNotEquals(instanceID1, instanceID2);
//
//		Assert.assertEquals(0, manager.getCachedFacts().size());
//		Atom[] facts2 = new Atom[] {
//				Atom.create(this.R, new Term[] { TypedConstant.create("B2"), TypedConstant.create("B20"), TypedConstant.create("B200"), TypedConstant.create(14) }) };
//		manager.addFacts(Arrays.asList(facts2));
//		Assert.assertEquals(1, manager.getCachedFacts().size());
//		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());
//
//		// Normal query
//		Atom a1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z"), Variable.create("i") });
//		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { x, y, z }, a1);
//		List<Match> answer = manager.answerQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
//		Assert.assertEquals(1, answer.size());
//
//		// switching back to first instance
//		manager.setDatabaseInstanceID(instanceID1);
//		Assert.assertArrayEquals(facts, manager.getCachedFacts().toArray(new Atom[manager.getCachedFacts().size()]));
//		// Typed untyped difference
//		Collection<Atom> physicalData = manager.getFactsFromPhysicalDatabase();
//		Assert.assertEquals(facts.length, physicalData.size());
//		Atom[] expectedResult = new Atom[] {
//				Atom.create(this.R_s, new Term[] { TypedConstant.create("A1"), TypedConstant.create(dDate), TypedConstant.create(100), UntypedConstant.create("13") }) };
//		if (parameters.getDatabaseDriver().contains("memory")) {
//			// the memory DB will give back the TypedConstants
//			Assert.assertEquals(facts[0], physicalData.iterator().next());
//		} else {
//			// Database storage can't map integers into typed or untyped, so it will be
//			// untyped.
//			Assert.assertEquals(expectedResult[0], physicalData.iterator().next());
//		}
//
//		// attempt to delete none existing fact
//		manager.deleteFacts(Arrays.asList(facts2));
//		Collection<Atom> getFacts = manager.getCachedFacts();
//		Assert.assertNotNull(getFacts);
//		Assert.assertEquals(1, getFacts.size());
//
//		manager.setDatabaseInstanceID(instanceID2);
//		// delete
//		getFacts = manager.getCachedFacts();
//		Assert.assertNotNull(getFacts);
//		Assert.assertEquals(1, getFacts.size());
//		manager.deleteFacts(Arrays.asList(facts2));
//		getFacts = manager.getCachedFacts();
//		Assert.assertNotNull(getFacts);
//		Assert.assertEquals(0, getFacts.size());
//		getFacts = manager.getFactsFromPhysicalDatabase();
//		Assert.assertNotNull(getFacts);
//		Assert.assertEquals(0, getFacts.size());
//		manager.shutdown(true);
	}

}
