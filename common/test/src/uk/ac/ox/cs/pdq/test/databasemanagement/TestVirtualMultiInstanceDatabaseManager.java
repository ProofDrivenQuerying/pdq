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
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
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
 * XYZ in test names refer to - MySql - Postgres - Memory
 * 
 * @author Gabor
 *
 */
public class TestVirtualMultiInstanceDatabaseManager extends PdqTest {

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

		LogicalDatabaseInstance manager = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(parameters),1);
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
		manager.addFacts(facts);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts.size(), getFacts.size());
		
		
		LogicalDatabaseInstance manager2 = manager.clone(2);
		// new instance
		Atom a3 = Atom.create(R, new Term[] { TypedConstant.create(129), TypedConstant.create(139), TypedConstant.create(149) });
		facts.add(a3);
		
		// some these facts exists already with different mappings, one of them is new.
		manager2.addFacts(facts);
		getFacts = manager2.getFactsFromPhysicalDatabase();
		Assert.assertTrue(facts.size() == getFacts.size() && facts.containsAll(getFacts));

		// Test duplicated storage - stored data should not change when we add the same
		// set twice
		manager2.addFacts(facts);
		getFacts = manager2.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts.size(), getFacts.size());

		
		// DELETE
		manager2.deleteFacts(facts);
		getFacts = manager2.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		
		
		manager = manager2.clone(1); // switching to "itself" instanceId1
		
		facts.remove(a3);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertTrue(facts.size() == getFacts.size() && facts.containsAll(getFacts));
		getFacts = manager.getCachedFacts();
		Assert.assertTrue(facts.size() == getFacts.size() && facts.containsAll(getFacts));

		manager.deleteFacts(facts);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		
		manager.dropDatabase();
		manager.shutdown();
	}

	/**
	 * Tests the basic functions of the VirtualDatabaseManager, using first a single
	 * table with Int attributes, then repeats the same test with String attributes.
	 * 
	 * This case test the MySQL driver.
	 */
	@Test
	public void virtualDatabaseCreationMySql() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		virtualDatabaseCreationInt(DatabaseParameters.MySql);
		virtualDatabaseCreationString(DatabaseParameters.MySql);
		largeTableQueryDifferenceEGD(DatabaseParameters.MySql);
	}

	/**
	 * Tests the basic functions of the VirtualDatabaseManager, using first a single
	 * table with Int attributes, then repeats the same test with String attributes.
	 * 
	 * This case test the Postgres driver.
	 */
	@Test
	public void virtualDatabaseCreationPostgres() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		virtualDatabaseCreationInt(DatabaseParameters.Postgres);
		virtualDatabaseCreationString(DatabaseParameters.Postgres);
		largeTableQueryDifferenceEGD(DatabaseParameters.Postgres);
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
	private void virtualDatabaseCreationInt(DatabaseParameters parameters) throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// create a simple manager
		LogicalDatabaseInstance manager = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(parameters),1);
		
		int instanceID1 = manager.getDatabaseInstanceID();
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R }));
		Atom[] facts = new Atom[] { Atom.create(this.R, new Term[] { TypedConstant.create(1), TypedConstant.create(10), TypedConstant.create(100) }) };
		manager.addFacts(Arrays.asList(facts));
		// Assert.assertEquals(/*1*/0,manager.getCachedFacts().size());
		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());

		// create a child of the previous manager, forming a virtual database instance
		// over the original one.
		manager = manager.clone(instanceID1 + 1);
		int instanceID2 = manager.getDatabaseInstanceID();
		Assert.assertNotEquals(instanceID1, instanceID2);

		Assert.assertEquals(0, manager.getCachedFacts().size());
		Atom[] facts2 = new Atom[] { Atom.create(this.R, new Term[] { TypedConstant.create(2), TypedConstant.create(20), TypedConstant.create(200) }) };
		manager.addFacts(Arrays.asList(facts2));
		Assert.assertEquals(1, manager.getCachedFacts().size());
		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());

		// Normal query
		Atom a1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { x, y, z }, a1);
		List<Match> answer = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
		Assert.assertEquals(1, answer.size());

		// switching back to first instance
		manager = manager.clone(instanceID1);
		
		Assert.assertArrayEquals(facts, manager.getCachedFacts().toArray(new Atom[manager.getCachedFacts().size()]));
		// Typed untyped difference
		Collection<Atom> physicalData = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts.length, physicalData.size());
		if (parameters.getDatabaseDriver().contains("memory")) {
			// the memory DB will give back the TypedConstants
			Assert.assertEquals(facts[0], physicalData.iterator().next());
		}

		// attempt to delete none existing fact
		manager.deleteFacts(Arrays.asList(facts2));
		Collection<Atom> getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(1, getFacts.size());

		manager = manager.clone(instanceID2);
		
		// delete
		getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(1, getFacts.size());
		manager.deleteFacts(Arrays.asList(facts2));
		getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		
		manager.dropDatabase();
		manager.shutdown();
	}

	/**
	 * Same as the virtualDatabaseCreationInt but the tables have String constants
	 * 
	 */
	private void virtualDatabaseCreationString(DatabaseParameters parameters) throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		SimpleDateFormat sdfmt1 = new SimpleDateFormat("yyyy-MM-dd");
		Date dDate = null;
		try {
			dDate = sdfmt1.parse("2017-09-21");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// create a simple manager
		LogicalDatabaseInstance manager = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(parameters),1);
		
		int instanceID1 = manager.getDatabaseInstanceID();
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R_s }));
		Atom[] facts = new Atom[] {
				Atom.create(this.R_s, new Term[] { TypedConstant.create("A1"), TypedConstant.create(dDate), TypedConstant.create(100)}) };
		manager.addFacts(Arrays.asList(facts));
		// Assert.assertEquals(/*1*/0,manager.getCachedFacts().size());
		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());

		// create a child of the previous manager, forming a virtual database instance
		// over the original one.
		manager = manager.clone(instanceID1);
		manager = manager.clone(instanceID1 + 1);
		int instanceID2 = manager.getDatabaseInstanceID();
		Assert.assertNotEquals(instanceID1, instanceID2);

		Assert.assertEquals(0, manager.getCachedFacts().size());
		Atom[] facts2 = new Atom[] {
				Atom.create(this.R, new Term[] { TypedConstant.create("B2"), TypedConstant.create("B20"), TypedConstant.create("B200") }) };
		manager.addFacts(Arrays.asList(facts2));
		Assert.assertEquals(1, manager.getCachedFacts().size());
		Assert.assertEquals(1, manager.getFactsFromPhysicalDatabase().size());

		// Normal query
		Atom a1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { x, y, z }, a1);
		List<Match> answer = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
		Assert.assertEquals(1, answer.size());

		// switching back to first instance
		manager = manager.clone(instanceID1);
		Assert.assertArrayEquals(facts, manager.getCachedFacts().toArray(new Atom[manager.getCachedFacts().size()]));
		// Typed untyped difference
		Collection<Atom> physicalData = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts.length, physicalData.size());
		Atom[] expectedResult = new Atom[] {
				Atom.create(this.R_s, new Term[] { TypedConstant.create("A1"), TypedConstant.create(dDate), TypedConstant.create(100) }) };
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

		manager = manager.clone(instanceID2);
		// delete
		getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(1, getFacts.size());
		manager.deleteFacts(Arrays.asList(facts2));
		getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		
		manager.dropDatabase();
		manager.shutdown();
	}
	/**
	 * In this test: Left query: exists[x,y](R(x,y,z) & S(x,y)) Right
	 * query:exists[x,y,z,res2](R(x,y,z) & (S(x,y) & T(res1,res2,z)))
	 * 
	 * The result should be all facts that only satisfy the first query, but not the
	 * second one.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	private void largeTableQueryDifferenceEGD(DatabaseParameters parameters) throws DatabaseException {
		LogicalDatabaseInstance manager = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(parameters),1);
		
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R, S, T }));
		List<Atom> facts = new ArrayList<>();

		// add some disjoint test data
		for (int i = 0; i < 100; i++) {
			Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + i), TypedConstant.create(20000 + i), TypedConstant.create(30000 + i) });
			if (i < 90) {
				Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(30000 + i), TypedConstant.create(20000 + i), TypedConstant.create(90000 + i) });
				facts.add(c1);
			}
			facts.add(a1);
		}

		// add test record that represents a not active dependency
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(14), TypedConstant.create(15) });
		Atom b1 = Atom.create(this.S, new Term[] { TypedConstant.create(13), TypedConstant.create(14) });
		Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(16), TypedConstant.create(17), TypedConstant.create(15) });
		facts.add(a1);
		facts.add(b1);
		facts.add(c1);
		// add test record that represents an active dependency
		Atom a2 = Atom.create(this.R, new Term[] { TypedConstant.create(113), TypedConstant.create(114), TypedConstant.create(115) });
		Atom b2 = Atom.create(this.S, new Term[] { TypedConstant.create(113), TypedConstant.create(114) });
		Atom c2 = Atom.create(this.T, new Term[] { TypedConstant.create(116), TypedConstant.create(117), TypedConstant.create(215) });
		facts.add(a2);
		facts.add(b2);
		facts.add(c2);
		manager.addFacts(facts);

		// form queries
		Atom q1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom q2 = Atom.create(this.S, new Term[] { Variable.create("x"), Variable.create("y") });
		Atom q3 = Atom.create(this.T, new Term[] { Variable.create("res1"), Variable.create("res2"), Variable.create("z") });
		ConjunctiveQuery left = ConjunctiveQuery.create(new Variable[] { z }, Conjunction.create(q1, q2));

		ConjunctiveQuery right = ConjunctiveQuery.create(new Variable[] { Variable.create("res1") }, (Conjunction) Conjunction.of(q1, q2, q3));
		// check left and right queries
		List<Match> leftFacts = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { left }));
		Assert.assertEquals(2, leftFacts.size());
		List<Match> rightFacts = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { right }));
		Assert.assertEquals(1, rightFacts.size());
		Assert.assertNull(rightFacts.get(0).getMapping().get(Variable.create("z")));

		List<Match> diffFacts = manager.answerQueryDifferences(left, right);
		System.out.println(diffFacts);
		Assert.assertEquals(1, diffFacts.size());
		Assert.assertTrue(diffFacts.get(0).getMapping().containsKey(Variable.create("z")));

		if (parameters.getDatabaseDriver().contains("memory")) {
			Assert.assertEquals(TypedConstant.create(115), diffFacts.get(0).getMapping().get(Variable.create("z")));
		} else {
			Assert.assertEquals(UntypedConstant.create("115"), diffFacts.get(0).getMapping().get(Variable.create("z")));
		}
		manager.dropDatabase();
		manager.shutdown();
		
	}

}
